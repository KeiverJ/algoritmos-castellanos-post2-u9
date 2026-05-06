package edu.udes.algoritmos.u9.benchmarks;

import edu.udes.algoritmos.u9.CircuitBreaker;
import edu.udes.algoritmos.u9.MetricsRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
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
 * Benchmark de ejecucion exitosa del CircuitBreaker en estado CLOSED.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
public class CircuitBreakerBenchmark {

    private CircuitBreaker<String> cb;

    @Setup(Level.Trial)
    public void setup() {
        MetricsRegistry metrics = new MetricsRegistry(new SimpleMeterRegistry());
        cb = new CircuitBreaker<>(5, 3, 1000, metrics);
    }

    @Benchmark
    public String closedSuccess() {
        return cb.execute(() -> "ok");
    }
}
