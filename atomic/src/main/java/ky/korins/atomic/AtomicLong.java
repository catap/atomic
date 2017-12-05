package ky.korins.atomic;

import sun.misc.Unsafe;

import java.util.function.LongBinaryOperator;
import java.util.function.LongUnaryOperator;

public class AtomicLong extends Number implements java.io.Serializable {
    private static final long serialVersionUID = -7198750120564725673L;

    private final long backoffInterval;

    private static final Unsafe unsafe = Java9Unsafe.getUnsafe();

    private static final long offset;

    static {
        try {
            offset = unsafe.objectFieldOffset(AtomicLong.class.getDeclaredField("value"));
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    private volatile long value;

    public AtomicLong(long initialValue, long backoffInterval) {
        if (backoffInterval < 1) {
            throw new IllegalArgumentException("Backoff interval should be great than 0");
        }
        this.value = initialValue;
        this.backoffInterval = backoffInterval;
    }

    public AtomicLong(long initialValue) {
        this.value = initialValue;
        backoffInterval = 1L;
    }

    public AtomicLong() {
        backoffInterval = 1L;
    }

    public long get() {
        return value;
    }

    public void set(long update) {
        value = update;
    }

    public final void lazySet(long update) {
        unsafe.putOrderedLong(this, offset, update);
    }

    public boolean compareAndSet(long expect, long update) {
        if (unsafe.compareAndSwapLong(this, offset, expect, update)) {
            return true;
        }
        unsafe.park(false, backoffInterval);
        return false;
    }

    public boolean weakCompareAndSet(long expect, long update) {
        return unsafe.compareAndSwapLong(this, offset, expect, update);
    }

    public final long getAndSet(long newValue) {
        long current;
        do {
            current = value;
        } while (!compareAndSet(current, newValue));
        return current;
    }

    public final long getAndAdd(long delta) {
        long current;
        do {
            current = value;
        } while (!compareAndSet(current, current + delta));
        return current;
    }

    public final long addAndGet(long delta) {
        return getAndAdd(delta) + delta;
    }

    public final long incrementAndGet() {
        return addAndGet(+1L);
    }

    public final long decrementAndGet() {
        return addAndGet(-1L);
    }

    public final long getAndIncrement() {
        return getAndAdd(+1L);
    }

    public final long getAndDecrement() {
        return getAndAdd(-1L);
    }

    public final long getAndUpdate(LongUnaryOperator updateFunction) {
        long prev, next;
        do {
            prev = value;
            next = updateFunction.applyAsLong(prev);
        } while (!compareAndSet(prev, next));
        return prev;
    }

    public final long updateAndGet(LongUnaryOperator updateFunction) {
        long prev, next;
        do {
            prev = value;
            next = updateFunction.applyAsLong(prev);
        } while (!compareAndSet(prev, next));
        return next;
    }

    public final long getAndAccumulate(long x, LongBinaryOperator accumulatorFunction) {
        long prev, next;
        do {
            prev = value;
            next = accumulatorFunction.applyAsLong(prev, x);
        } while (!compareAndSet(prev, next));
        return prev;
    }

    public final long accumulateAndGet(long x, LongBinaryOperator accumulatorFunction) {
        long prev, next;
        do {
            prev = value;
            next = accumulatorFunction.applyAsLong(prev, x);
        } while (!compareAndSet(prev, next));
        return next;
    }

    public String toString() {
        return Long.toString(value);
    }

    public int intValue() {
        return (int)value;
    }

    public long longValue() {
        return value;
    }

    public float floatValue() {
        return (float)value;
    }

    public double doubleValue() {
        return (double)value;
    }
}
