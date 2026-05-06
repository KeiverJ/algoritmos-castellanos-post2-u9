package edu.udes.algoritmos.u9;

/**
 * Excepcion lanzada cuando el circuit breaker esta en estado OPEN.
 */
public class CircuitOpenException extends RuntimeException {
    /**
     * Crea la excepcion con un mensaje descriptivo.
     *
     * @param message mensaje de error
     */
    public CircuitOpenException(String message) {
        super(message);
    }
}
