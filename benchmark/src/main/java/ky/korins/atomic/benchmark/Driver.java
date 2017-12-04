package ky.korins.atomic.benchmark;

import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

public class Driver {
    public static void main(String[] args) throws RunnerException {
        for (int threads = 1; threads < 32; threads <<= 1) {
            Options opt = new OptionsBuilder()
                    .forks(1)
                    .threads(threads)
                    .mode(Mode.AverageTime)
                    .timeUnit(TimeUnit.NANOSECONDS)
                    .build();

            new Runner(opt).run();
        }
    }
}
