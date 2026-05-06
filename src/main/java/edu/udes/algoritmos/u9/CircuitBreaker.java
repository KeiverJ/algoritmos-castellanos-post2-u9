package edu.udes.algoritmos.u9;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementa un circuit breaker con estados CLOSED, OPEN y HALF_OPEN.
 * Thread-safety: usa atomics para transiciones y contadores.
 *
 * @param <T> tipo de resultado de la operacion protegida
 */
public class CircuitBreaker<T> {
    public enum State { CLOSED, OPEN, HALF_OPEN }

    private static final Logger log = LoggerFactory.getLogger(CircuitBreaker.class);

    private final int failureThreshold;
    private final int successThreshold;
    private final long resetTimeoutMs;
    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private final AtomicInteger failures = new AtomicInteger(0);
    private final AtomicInteger halfOpenOks = new AtomicInteger(0);
    private final AtomicInteger openTransitions = new AtomicInteger(0);
    private volatile long openedAt = 0;
    private final MetricsRegistry metrics;

    /**
     * Crea el circuit breaker con umbrales y tiempo de reset.
     *
     * @param failureThreshold fallos consecutivos para abrir el circuito
     * @param successThreshold exitos en HALF_OPEN para cerrar el circuito
     * @param resetTimeoutMs tiempo en ms para intentar recuperacion
     * @param metrics registro de metricas
     */
    public CircuitBreaker(int failureThreshold, int successThreshold, long resetTimeoutMs, MetricsRegistry metrics) {
        if (failureThreshold <= 0) {
            throw new IllegalArgumentException("failureThreshold debe ser mayor a cero");
        }
        if (successThreshold <= 0) {
            throw new IllegalArgumentException("successThreshold debe ser mayor a cero");
        }
        if (resetTimeoutMs <= 0) {
            throw new IllegalArgumentException("resetTimeoutMs debe ser mayor a cero");
        }
        this.failureThreshold = failureThreshold;
        this.successThreshold = successThreshold;
        this.resetTimeoutMs = resetTimeoutMs;
        this.metrics = Objects.requireNonNull(metrics, "metrics es obligatorio");
        this.metrics.setCircuitBreakerState(0);
    }

    /**
     * Ejecuta una operacion protegida por el circuit breaker.
     *
     * @param operation operacion a ejecutar
     * @return resultado de la operacion
     */
    public T execute(Supplier<T> operation) {
        Objects.requireNonNull(operation, "operation es obligatorio");
        State current = evaluateState();
        if (current == State.OPEN) {
            throw new CircuitOpenException("Circuit breaker OPEN - fast fail");
        }
        try {
            T result = operation.get();
            onSuccess();
            return result;
        } catch (Exception e) {
            onFailure();
            throw e;
        }
    }

    /**
     * Retorna el estado actual del circuit breaker.
     *
     * @return estado actual
     */
    public State getState() {
        return state.get();
    }

    int getOpenTransitions() {
        return openTransitions.get();
    }

    private State evaluateState() {
        if (state.get() == State.OPEN
            && System.currentTimeMillis() - openedAt >= resetTimeoutMs
            && state.compareAndSet(State.OPEN, State.HALF_OPEN)) {
            halfOpenOks.set(0);
            metrics.setCircuitBreakerState(2);
            log.info("Circuit breaker -> HALF_OPEN");
        }
        return state.get();
    }

    private void onSuccess() {
        if (state.get() == State.HALF_OPEN) {
            if (halfOpenOks.incrementAndGet() >= successThreshold) {
                state.set(State.CLOSED);
                failures.set(0);
                metrics.setCircuitBreakerState(0);
                log.info("Circuit breaker -> CLOSED (recuperado)");
            }
        } else {
            failures.set(0);
        }
    }

    private void onFailure() {
        if (failures.incrementAndGet() >= failureThreshold
            && (state.compareAndSet(State.CLOSED, State.OPEN)
            || state.compareAndSet(State.HALF_OPEN, State.OPEN))) {
            openedAt = System.currentTimeMillis();
            openTransitions.incrementAndGet();
            metrics.setCircuitBreakerState(1);
            log.warn("Circuit breaker -> OPEN (fallos: {})", failures.get());
        }
    }
}
