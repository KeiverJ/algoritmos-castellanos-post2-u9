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

    public PaymentResult processPayment(PaymentRequest req) {
        if (!rateLimiter.tryAcquire()) {
            throw new RateLimitException("Limite de solicitudes alcanzado");
        }
        return observable.execute("processPayment", () -> cb.execute(() -> gateway.charge(req)));
    }
}
