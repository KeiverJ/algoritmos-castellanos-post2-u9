package edu.udes.algoritmos.u9;

/**
 * Abstraccion de un gateway de pagos externo.
 */
public interface ExternalPaymentGateway {
    PaymentResult charge(PaymentRequest request);
}
