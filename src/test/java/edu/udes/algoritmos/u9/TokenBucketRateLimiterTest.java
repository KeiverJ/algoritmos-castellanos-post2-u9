package edu.udes.algoritmos.u9;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import org.junit.jupiter.api.Test;

class TokenBucketRateLimiterTest {

    @Test
    void respectsCapacityPerSecond() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(100000);

        assertTrue(limiter.tryAcquire(100000));
        assertFalse(limiter.tryAcquire(100000));
    }

    @Test
    void refillsOverTime() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(1000);

        assertTrue(limiter.tryAcquire(1000));
        assertFalse(limiter.tryAcquire(1000));

        waitForMillis(20);

        assertTrue(limiter.tryAcquire(1));
    }

    private void waitForMillis(long millis) {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(millis);
        while (System.nanoTime() < deadline) {
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
        }
    }
}
