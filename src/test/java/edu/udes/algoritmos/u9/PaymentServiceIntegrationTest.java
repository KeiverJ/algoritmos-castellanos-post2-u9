package edu.udes.algoritmos.u9;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class PaymentServiceIntegrationTest {

    @Test
    void integrationActivatesCircuitBreakerAndLogsRequestId() {
        MetricsRegistry metrics = new MetricsRegistry(new SimpleMeterRegistry());
        ObservableOperation observable = new ObservableOperation(metrics);
        CircuitBreaker<PaymentResult> cb = new CircuitBreaker<>(3, 1, 50, metrics);
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(100000);

        AtomicInteger attempts = new AtomicInteger(0);
        ExternalPaymentGateway gateway = req -> {
            int call = attempts.incrementAndGet();
            if (call <= 3) {
                throw new RuntimeException("timeout");
            }
            return PaymentResult.success("txn-001");
        };

        PaymentService service = new PaymentService(cb, limiter, observable, gateway);
        PaymentRequest request = new PaymentRequest("cust-1", "COP", 1000);

        Logger logger = (Logger) LoggerFactory.getLogger(CircuitBreaker.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        for (int i = 0; i < 3; i++) {
            assertThrows(RuntimeException.class, () -> service.processPayment(request));
        }

        assertEquals(CircuitBreaker.State.OPEN, cb.getState());
        assertThrows(CircuitOpenException.class, () -> service.processPayment(request));

        waitForMillis(70);

        PaymentResult result = service.processPayment(request);
        assertTrue(result.isSuccess());

        List<ILoggingEvent> events = appender.list;
        assertTrue(events.stream().anyMatch(event -> event.getFormattedMessage().contains("Circuit breaker -> OPEN")));

        ILoggingEvent openEvent = events.stream()
            .filter(event -> event.getFormattedMessage().contains("Circuit breaker -> OPEN"))
            .findFirst()
            .orElseThrow();
        Map<String, String> mdc = openEvent.getMDCPropertyMap();
        assertNotNull(mdc.get("requestId"));
        assertEquals("processPayment", mdc.get("operation"));

        logger.detachAppender(appender);
        appender.stop();
    }

    private void waitForMillis(long millis) {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(millis);
        while (System.nanoTime() < deadline) {
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
        }
    }
}
