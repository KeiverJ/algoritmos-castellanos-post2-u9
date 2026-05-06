package edu.udes.algoritmos.u9;

import java.util.Objects;

/**
 * Servicio de pagos que integra observabilidad y resiliencia.
 */
public class PaymentService {
    private final CircuitBreaker<PaymentResult> cb;
    private final TokenBucketRateLimiter rateLimiter;
    private final ObservableOperation observable;
    private final ExternalPaymentGateway gateway;

    /**
     * Crea el servicio de pagos con sus dependencias.
     *
     * @param cb circuit breaker
     * @param rateLimiter limitador de tasa
     * @param observable operacion observable
     * @param gateway gateway externo
     */
    public PaymentService(
        CircuitBreaker<PaymentResult> cb,
        TokenBucketRateLimiter rateLimiter,
        ObservableOperation observable,
        ExternalPaymentGateway gateway
    ) {
        this.cb = Objects.requireNonNull(cb, "cb es obligatorio");
        this.rateLimiter = Objects.requireNonNull(rateLimiter, "rateLimiter es obligatorio");
        this.observable = Objects.requireNonNull(observable, "observable es obligatorio");
        this.gateway = Objects.requireNonNull(gateway, "gateway es obligatorio");
    }

    /**
     * Procesa el pago aplicando rate limiting, observabilidad y circuit breaker.
     *
     * @param req solicitud de pago
     * @return resultado del pago
     * @throws RateLimitException cuando se supera el limite
     * @throws CircuitOpenException cuando el circuito esta OPEN
     */
    public PaymentResult processPayment(PaymentRequest req) {
        if (!rateLimiter.tryAcquire()) {
            throw new RateLimitException("Limite de solicitudes alcanzado");
        }
        return observable.execute("processPayment", () -> cb.execute(() -> gateway.charge(req)));
    }
}
