package edu.udes.algoritmos.u9;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

class ObservableOperationTest {

    @Test
    void executeAddsMdcAndClearsContext() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        MetricsRegistry metrics = new MetricsRegistry(registry);
        ObservableOperation observable = new ObservableOperation(metrics);

        Logger logger = (Logger) LoggerFactory.getLogger(ObservableOperation.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        String result = observable.execute("testOperation", () -> "ok");

        assertEquals("ok", result);
        assertTrue(MDC.getCopyOfContextMap() == null || MDC.getCopyOfContextMap().isEmpty());

        List<ILoggingEvent> events = appender.list;
        assertFalse(events.isEmpty());
        for (ILoggingEvent event : events) {
            Map<String, String> mdc = event.getMDCPropertyMap();
            assertNotNull(mdc.get("requestId"));
            assertEquals(8, mdc.get("requestId").length());
            assertEquals("testOperation", mdc.get("operation"));
        }

        logger.detachAppender(appender);
        appender.stop();
    }
}
