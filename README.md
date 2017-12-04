# Korinsky's Atomic 

It is the fast atomic operations for a multi-threads environment for JVM.

It has similar API with `java.util.concurrent.atomic` but it much faster at the multi-threads environment,
 you can just update imports and enjoy it.

Each time when CAS loop can't update the value, it disables current thread at scheduler for 1 ns,
 to prevent stupid looping when another thread may release the value.

## How to use it?

It is in Maven Central, and can be added to a Maven project as follows

```
        <dependency>
            <groupId>ky.korins</groupId>
            <artifactId>atomic</artifactId>
            <version>1.0</version>
        </dependency>
```


## How fast is it?

It is similar with `j.u.c.atomic` for one concurrent thread and about 6 times faster for 16 concurrent threads

Performance: (numbers mean ns/op at Intel® Core™ i5-2540M @ 2.60GHz)

| Threads               | 1              | 2               | 4                | 8                  | 16                 | 32                  |
|-----------------------|----------------|-----------------|------------------|--------------------|--------------------|---------------------|
| Java.CAS_Loop         | 10.750 ± 0.103 | 104.979 ± 6.496 | 386.237 ± 23.241 |  776.545 ±  46.368 | 1,560.000 ± 90.221 | 3,232.245 ± 164.594 |
| Java.Sub_CAS_Loop     | 13.833 ± 0.125 | 135.536 ± 8.635 | 533.000 ± 30.549 | 1072.505 ± 106.865 | 2,252.839 ± 79.244 | 4,486.006 ± 327.542 |
| Java.increment        |  6.412 ± 0.057 |  42.053 ± 2.426 |  99.352 ±  2.382 |  202.493 ±   6.822 |   411.440 ±  9.364 |   837.842 ±  20.071 |
| Korinsky.CAS_Loop     | 10.800 ± 0.090 |  22.896 ± 0.253 |  51.525 ±  0.632 |  119.672 ±   1.673 |   244.627 ±  2.679 |   493.176 ±  10.523 |
| Korinsky.Sub_CAS_Loop | 10.586 ± 0.107 |  32.219 ± 0.119 |  69.971 ±  0.658 |  168.475 ±   2.231 |   347.888 ±  4.223 |   685.634 ±  22.103 |
| Korinsky.increment    | 13.625 ± 0.116 |  22.549 ± 0.178 |  49.745 ±  0.774 |  123.073 ±   6.986 |   246.427 ±  3.973 |   491.020 ±   8.627 |

## Which operations does it support?

Right now it supports `AtomicLong`, `AtomicInteger`, `AtomicBoolean`, `AtomicLongArray` and `AtomicIntegerArray`
 with Java8 compatibility API.

## Why does Java.increment much faster than This.increment

This atomic implemented all operation includes `getAndAdd*` and `addAndGet*` over CAS-loop,
 Java's `getAndAdd` and `addAndGet*` uses `lock addq` CPU instruction
 or just `i++` where `i` is `volatile` variable that much faster without concurrent threads.

## Why does it add 1ns backoff? Maybe increase it?

We don't need bigger interval because propose of this interval is switching to another thread
 to don't burn CPU cyclen in looping, also if I find the way to decrease it less than 1 ns I will do this.

