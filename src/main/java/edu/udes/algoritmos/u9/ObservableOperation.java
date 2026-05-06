package edu.udes.algoritmos.u9;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Ejecuta operaciones con MDC para trazabilidad y metricas basicas.
 */
public class ObservableOperation {
    private static final Logger log = LoggerFactory.getLogger(ObservableOperation.class);
    private final MetricsRegistry metrics;

    /**
     * Crea la operacion observable con registro de metricas.
     *
     * @param metrics registro de metricas
     */
    public ObservableOperation(MetricsRegistry metrics) {
        this.metrics = Objects.requireNonNull(metrics, "metrics es obligatorio");
    }

    /**
     * Ejecuta la operacion adjuntando requestId y operation al MDC.
     *
     * @param operationName nombre de la operacion
     * @param operation codigo a ejecutar
     * @param <T> tipo de retorno
     * @return resultado de la operacion
     */
    public <T> T execute(String operationName, Supplier<T> operation) {
        Objects.requireNonNull(operationName, "operationName es obligatorio");
        Objects.requireNonNull(operation, "operation es obligatorio");

        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("requestId", requestId);
        MDC.put("operation", operationName);
        long start = System.currentTimeMillis();
        try {
            log.info("Iniciando operacion");
            T result = operation.get();
            long elapsed = System.currentTimeMillis() - start;
            metrics.recordSuccess(elapsed);
            log.info("Operacion completada en {}ms", elapsed);
            return result;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            metrics.recordFailure(elapsed);
            log.error("Operacion fallida en {}ms: {}", elapsed, e.getMessage());
            throw e;
        } finally {
            MDC.clear();
        }
    }
}
