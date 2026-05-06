package edu.udes.algoritmos.u9;

/**
 * Excepcion lanzada cuando el circuit breaker esta en estado OPEN.
 */
public class CircuitOpenException extends RuntimeException {
    public CircuitOpenException(String message) {
        super(message);
    }
}
