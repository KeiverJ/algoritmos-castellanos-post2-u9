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

    /**
     * Crea un resultado exitoso.
     *
     * @param transactionId identificador de la transaccion
     * @return resultado exitoso
     */
    public static PaymentResult success(String transactionId) {
        return new PaymentResult(true, transactionId, "OK");
    }

    /**
     * Crea un resultado fallido.
     *
     * @param message mensaje de error
     * @return resultado fallido
     */
    public static PaymentResult failure(String message) {
        return new PaymentResult(false, null, message);
    }

    /**
     * Indica si el pago fue exitoso.
     *
     * @return true si fue exitoso
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Retorna el identificador de la transaccion.
     *
     * @return id de transaccion o null si fallo
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * Retorna el mensaje asociado al resultado.
     *
     * @return mensaje de estado
     */
    public String getMessage() {
        return message;
    }
}
