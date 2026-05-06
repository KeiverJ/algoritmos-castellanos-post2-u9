package edu.udes.algoritmos.u9;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

class MetricsRegistryTest {

    @Test
    void errorRateIsCalculatedCorrectly() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        MetricsRegistry metrics = new MetricsRegistry(registry);

        metrics.recordSuccess(10);
        metrics.recordSuccess(5);
        metrics.recordFailure(7);
        metrics.recordFailure(9);
        metrics.recordFailure(4);

        assertEquals(0.6, metrics.getErrorRate(), 0.0001);
        assertEquals(0, metrics.getCircuitBreakerState());
    }
}
