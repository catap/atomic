package ky.korins.atomic.benchmark;

import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

public class Driver {
    public static void main(String[] args) throws RunnerException {
        for (int threads = 1; threads < 32; threads <<= 1) {
            Options opt = new OptionsBuilder()
                    .forks(10)
                    .threads(threads)
                    .warmupIterations(10)
                    .measurementIterations(20)
                    .mode(Mode.AverageTime)
                    .timeUnit(TimeUnit.NANOSECONDS)
                    .include("ky.korins.atomic.benchmark")
                    .resultFormat(ResultFormatType.CSV)
                    .result("atomic_" + threads + ".csv")
                    .build();

            new Runner(opt).run();
        }
    }
}
