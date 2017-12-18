package ky.korins.atomic.benchmark;

import ky.korins.atomic.AtomicLong;
import org.openjdk.jmh.annotations.*;

@State(Scope.Benchmark)
public class KorinskyAtomicBenchmark {
    AtomicLong atomicLong = new AtomicLong();

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
    public void CAS_Loop(BaseAtomicBenchmark.Counter counter) {
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
