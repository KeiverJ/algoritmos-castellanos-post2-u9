package edu.udes.algoritmos.u9;

/**
 * Resultado de un pago.
 */
public class PaymentResult {
    private final boolean success;
    private final String transactionId;
    private final String message;

    private PaymentResult(boolean success, String transactionId, String message) {
        this.success = success;
        this.transactionId = transactionId;
        this.message = message;
    }

    public static PaymentResult success(String transactionId) {
        return new PaymentResult(true, transactionId, "OK");
    }

    public static PaymentResult failure(String message) {
        return new PaymentResult(false, null, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getMessage() {
        return message;
    }
}
