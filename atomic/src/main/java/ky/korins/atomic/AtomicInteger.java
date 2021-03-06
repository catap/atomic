package ky.korins.atomic;

import sun.misc.Unsafe;

import java.util.concurrent.locks.LockSupport;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

public class AtomicInteger extends Number implements java.io.Serializable {
    private static final long serialVersionUID = -7198750120564725673L;

    private static final Unsafe unsafe = Java9Unsafe.getUnsafe();

    private static final long offset;

    static {
        try {
            offset = unsafe.objectFieldOffset(AtomicInteger.class.getDeclaredField("value"));
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    private volatile int value;

    public AtomicInteger(int initialValue) {
        this.value = initialValue;
    }

    public AtomicInteger() {
    }

    public int get() {
        return value;
    }

    public void set(int update) {
        value = update;
    }

    public final void lazySet(int update) {
        unsafe.putOrderedInt(this, offset, update);
    }

    public boolean compareAndSet(int expect, int update) {
        if (unsafe.compareAndSwapInt(this, offset, expect, update)) {
            return true;
        }
        LockSupport.parkNanos(1);
        return false;
    }

    public boolean weakCompareAndSet(int expect, int update) {
        return unsafe.compareAndSwapInt(this, offset, expect, update);
    }

    public final int getAndSet(int newValue) {
        int current;
        do {
            current = value;
        } while (!compareAndSet(current, newValue));
        return current;
    }

    public final int getAndAdd(int delta) {
        int current;
        do {
            current = value;
        } while (!compareAndSet(current, current + delta));
        return current;
    }

    public final int addAndGet(int delta) {
        return getAndAdd(delta) + delta;
    }

    public final int incrementAndGet() {
        return addAndGet(+1);
    }

    public final int decrementAndGet() {
        return addAndGet(-1);
    }

    public final int getAndIncrement() {
        return getAndAdd(+1);
    }

    public final int getAndDecrement() {
        return getAndAdd(-1);
    }

    public final int getAndUpdate(IntUnaryOperator updateFunction) {
        int prev, next;
        do {
            prev = value;
            next = updateFunction.applyAsInt(prev);
        } while (!compareAndSet(prev, next));
        return prev;
    }

    public final int updateAndGet(IntUnaryOperator updateFunction) {
        int prev, next;
        do {
            prev = value;
            next = updateFunction.applyAsInt(prev);
        } while (!compareAndSet(prev, next));
        return next;
    }

    public final int getAndAccumulate(int x, IntBinaryOperator accumulatorFunction) {
        int prev, next;
        do {
            prev = value;
            next = accumulatorFunction.applyAsInt(prev, x);
        } while (!compareAndSet(prev, next));
        return prev;
    }

    public final int accumulateAndGet(int x, IntBinaryOperator accumulatorFunction) {
        int prev, next;
        do {
            prev = value;
            next = accumulatorFunction.applyAsInt(prev, x);
        } while (!compareAndSet(prev, next));
        return next;
    }

    public String toString() {
        return Integer.toString(value);
    }

    public int intValue() {
        return value;
    }

    public long longValue() {
        return (long)value;
    }

    public float floatValue() {
        return (float)value;
    }

    public double doubleValue() {
        return (double)value;
    }
}
