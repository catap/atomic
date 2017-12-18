package ky.korins.atomic.benchmark;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.atomic.AtomicLong;

@State(Scope.Benchmark)
public class JavaAtomicBenchmark extends BaseAtomicBenchmark {
    AtomicLong atomicLong = new AtomicLong(0L);

    final static int batchSize = 10000;

    @Benchmark
    @OperationsPerInvocation(batchSize)
    public void increment() {
        for (int i = 0; i < batchSize; i++) {
            atomicLong.incrementAndGet();
        }
    }

    @Benchmark
    @OperationsPerInvocation(batchSize)
    public void CAS_Loop(Counter counter) {
        for (int i = 0; i < batchSize; i++) {
            while (!atomicLong.compareAndSet(0, Long.MAX_VALUE)) {
                counter.casTotal++;
            }
            counter.casTotal++;
            counter.casSuccess++;
            atomicLong.set(0);
        }
    }
}
