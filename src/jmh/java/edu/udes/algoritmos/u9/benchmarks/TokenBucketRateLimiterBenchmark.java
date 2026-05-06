package edu.udes.algoritmos.u9.benchmarks;

import edu.udes.algoritmos.u9.TokenBucketRateLimiter;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Benchmark de adquisicion de tokens en el rate limiter.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
public class TokenBucketRateLimiterBenchmark {

    private TokenBucketRateLimiter limiter;

    @Setup(Level.Trial)
    public void setup() {
        limiter = new TokenBucketRateLimiter(1_000_000);
    }

    @Benchmark
    public boolean tryAcquire() {
        return limiter.tryAcquire();
    }
}
