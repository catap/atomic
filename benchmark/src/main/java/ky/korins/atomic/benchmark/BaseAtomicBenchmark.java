package ky.korins.atomic.benchmark;

import org.openjdk.jmh.annotations.AuxCounters;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

abstract public class BaseAtomicBenchmark {
    protected final static int batchSize = 10000;

    @State(Scope.Thread)
    @AuxCounters(AuxCounters.Type.EVENTS)
    public static class Counter {
        long casSuccess = 0L;
        long casTotal = 0L;

        public double successRate() {
            return (double) casSuccess / (double) casTotal;
        }
    }
}
