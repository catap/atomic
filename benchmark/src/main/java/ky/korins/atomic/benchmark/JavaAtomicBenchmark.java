package ky.korins.atomic.benchmark;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.atomic.AtomicLong;

@State(Scope.Benchmark)
public class JavaAtomicBenchmark {
    AtomicLong atomicLong = new AtomicLong(0L);
    AtomicLong secondAtomicLong = new AtomicLong(0L);

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
    public void CAS_Loop() {
        for (int i = 0; i < batchSize; i++) {
            long old;
            do {
                old = atomicLong.get();
            } while (!atomicLong.compareAndSet(old, old + 1L));
        }
    }

    @Benchmark
    @OperationsPerInvocation(batchSize)
    public void Sub_CAS_Loops() {
        for (int i = 0; i < batchSize; i++) {
            while (true) {
                final long old = atomicLong.get();
                if (atomicLong.compareAndSet(old, old + i % 2)) {
                    final long secondOld = secondAtomicLong.get();
                    if (secondAtomicLong.compareAndSet(secondOld, secondOld + 1L)) {
                        break;
                    }
                }
            }
        }
    }
}
