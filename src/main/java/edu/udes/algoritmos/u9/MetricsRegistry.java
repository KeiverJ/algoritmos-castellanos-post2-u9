package edu.udes.algoritmos.u9;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Centraliza las metricas de observabilidad (contadores, temporizadores y gauge).
 */
public class MetricsRegistry {
    private final Counter requestsTotal;
    private final Counter requestsFailed;
    private final Timer requestDuration;
    private final AtomicInteger circuitBreakerState;

    /**
     * Crea el registro de metricas con los contadores y timers requeridos.
     *
     * @param registry registro de Micrometer
     */
    public MetricsRegistry(MeterRegistry registry) {
        Objects.requireNonNull(registry, "registry es obligatorio");
        this.requestsTotal = Counter.builder("app.requests.total")
            .description("Total de solicitudes recibidas")
            .tag("service", "payment")
            .register(registry);
        this.requestsFailed = Counter.builder("app.requests.failed")
            .description("Solicitudes fallidas")
            .tag("service", "payment")
            .register(registry);
        this.requestDuration = Timer.builder("app.request.duration")
            .description("Duracion de solicitudes")
            .publishPercentiles(0.5, 0.90, 0.95, 0.99)
            .register(registry);
        AtomicInteger state = new AtomicInteger(0);
        registry.gauge("app.circuit_breaker.state", state);
        this.circuitBreakerState = state;
    }

    /**
     * Registra una solicitud exitosa con su duracion en milisegundos.
     *
     * @param durationMs duracion de la operacion
     */
    public void recordSuccess(long durationMs) {
        requestsTotal.increment();
        requestDuration.record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Registra una solicitud fallida con su duracion en milisegundos.
     *
     * @param durationMs duracion de la operacion
     */
    public void recordFailure(long durationMs) {
        requestsTotal.increment();
        requestsFailed.increment();
        requestDuration.record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Actualiza el estado del circuit breaker (0=CLOSED, 1=OPEN, 2=HALF_OPEN).
     *
     * @param state estado numerico del circuito
     */
    public void setCircuitBreakerState(int state) {
        circuitBreakerState.set(state);
    }

    /**
     * Calcula la tasa de error como fallas/total.
     *
     * @return tasa de error en rango 0..1
     */
    public double getErrorRate() {
        double total = requestsTotal.count();
        return total == 0 ? 0 : requestsFailed.count() / total;
    }

    /**
     * Retorna el estado actual del circuit breaker reportado por el gauge.
     *
     * @return estado numerico
     */
    public int getCircuitBreakerState() {
        return circuitBreakerState.get();
    }
}
