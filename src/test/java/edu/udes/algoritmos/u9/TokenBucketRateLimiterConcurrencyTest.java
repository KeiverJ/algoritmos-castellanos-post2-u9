package edu.udes.algoritmos.u9;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class TokenBucketRateLimiterConcurrencyTest {

    @Test
    void rateLimiterDoesNotExceedTheoreticalMaxUnderConcurrency() throws InterruptedException {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(5000);

        int threads = 20;
        int attemptsPerThread = 500;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger granted = new AtomicInteger(0);
        AtomicInteger interrupted = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                try {
                    start.await();
                    for (int j = 0; j < attemptsPerThread; j++) {
                        if (limiter.tryAcquire()) {
                            granted.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    interrupted.incrementAndGet();
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            }).start();
        }

        start.countDown();
        done.await(5, TimeUnit.SECONDS);

        long elapsedMs = System.currentTimeMillis() - startTime;
        long maxAllowed = 5000 + (elapsedMs * limiter.getTokensPerMs());

        assertTrue(granted.get() <= maxAllowed);
        assertEquals(0, interrupted.get());
    }
}
