package edu.udes.algoritmos.u9;

/**
 * Excepcion lanzada cuando se alcanza el limite de solicitudes.
 */
public class RateLimitException extends RuntimeException {
    /**
     * Crea la excepcion con un mensaje descriptivo.
     *
     * @param message mensaje de error
     */
    public RateLimitException(String message) {
        super(message);
    }
}
