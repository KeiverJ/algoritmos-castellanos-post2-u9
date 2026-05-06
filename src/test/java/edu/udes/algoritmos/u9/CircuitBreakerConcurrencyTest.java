package edu.udes.algoritmos.u9;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class CircuitBreakerConcurrencyTest {

    @Test
    void circuitBreakerIsThreadSafe() throws InterruptedException {
        MetricsRegistry metrics = new MetricsRegistry(new SimpleMeterRegistry());
        CircuitBreaker<String> cb = new CircuitBreaker<>(1, 1, 500, metrics);

        int threads = 20;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger openExceptions = new AtomicInteger(0);
        AtomicInteger failures = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                try {
                    start.await();
                    cb.execute(() -> {
                        throw new RuntimeException("fail");
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (CircuitOpenException e) {
                    openExceptions.incrementAndGet();
                } catch (RuntimeException e) {
                    failures.incrementAndGet();
                } finally {
                    done.countDown();
                }
            }).start();
        }

        start.countDown();
        done.await(5, TimeUnit.SECONDS);

        assertEquals(CircuitBreaker.State.OPEN, cb.getState());
        assertEquals(1, cb.getOpenTransitions());
        assertTrue(openExceptions.get() >= 1);
        assertTrue(failures.get() >= 1);
    }
}
