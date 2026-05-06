package edu.udes.algoritmos.u9;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import org.junit.jupiter.api.Test;

class CircuitBreakerTest {

    @Test
    void opensAfterConsecutiveFailures() {
        MetricsRegistry metrics = new MetricsRegistry(new SimpleMeterRegistry());
        CircuitBreaker<String> cb = new CircuitBreaker<>(3, 2, 50, metrics);

        for (int i = 0; i < 3; i++) {
            assertThrows(RuntimeException.class, () -> cb.execute(() -> {
                throw new RuntimeException("fail");
            }));
        }

        assertEquals(CircuitBreaker.State.OPEN, cb.getState());
        assertEquals(1, cb.getOpenTransitions());
    }

    @Test
    void openStateFastFailsWithoutCallingOperation() {
        MetricsRegistry metrics = new MetricsRegistry(new SimpleMeterRegistry());
        CircuitBreaker<String> cb = new CircuitBreaker<>(1, 1, 100, metrics);
        assertThrows(RuntimeException.class, () -> cb.execute(() -> {
            throw new RuntimeException("fail");
        }));

        AtomicInteger calls = new AtomicInteger(0);
        assertThrows(CircuitOpenException.class, () -> cb.execute(() -> {
            calls.incrementAndGet();
            return "ok";
        }));

        assertEquals(0, calls.get());
    }

    @Test
    void transitionsFromOpenToHalfOpenToClosed() {
        MetricsRegistry metrics = new MetricsRegistry(new SimpleMeterRegistry());
        CircuitBreaker<String> cb = new CircuitBreaker<>(2, 2, 50, metrics);

        assertThrows(RuntimeException.class, () -> cb.execute(() -> {
            throw new RuntimeException("fail");
        }));
        assertThrows(RuntimeException.class, () -> cb.execute(() -> {
            throw new RuntimeException("fail");
        }));

        assertEquals(CircuitBreaker.State.OPEN, cb.getState());

        waitForMillis(70);

        cb.execute(() -> "ok");
        assertEquals(CircuitBreaker.State.HALF_OPEN, cb.getState());

        cb.execute(() -> "ok");
        assertEquals(CircuitBreaker.State.CLOSED, cb.getState());
    }

    private void waitForMillis(long millis) {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(millis);
        while (System.nanoTime() < deadline) {
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
        }
    }
}
