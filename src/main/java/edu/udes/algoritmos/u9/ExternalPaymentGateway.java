package edu.udes.algoritmos.u9;

/**
 * Abstraccion de un gateway de pagos externo.
 */
public interface ExternalPaymentGateway {
    /**
     * Ejecuta el cobro del pago.
     *
     * @param request solicitud de pago
     * @return resultado del pago
     */
    PaymentResult charge(PaymentRequest request);
}
