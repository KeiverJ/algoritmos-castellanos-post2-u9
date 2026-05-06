package edu.udes.algoritmos.u9;

/**
 * Excepcion lanzada cuando se alcanza el limite de solicitudes.
 */
public class RateLimitException extends RuntimeException {
    public RateLimitException(String message) {
        super(message);
    }
}
