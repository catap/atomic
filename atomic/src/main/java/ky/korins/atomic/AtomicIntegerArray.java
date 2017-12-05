package ky.korins.atomic;

import sun.misc.Unsafe;

import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

public class AtomicIntegerArray implements java.io.Serializable {
    private static final long serialVersionUID = -800117181216365580L;

    private final int backoffInterval;

    private static final Unsafe unsafe = Java9Unsafe.getUnsafe();

    private static final int base = unsafe.arrayBaseOffset(int[].class);

    private final int[] array;

    static {
        int scale = unsafe.arrayIndexScale(int[].class);
        if ((scale & (scale - 1)) != 0)
            throw new Error("data type scale not a power of two");
        shift = 31 - Integer.numberOfLeadingZeros(scale);
    }

    private static final int shift;

    private long checkedByteOffset(int i) {
        if (i < 0 || i >= array.length)
            throw new IndexOutOfBoundsException("index " + i);

        return byteOffset(i);
    }

    private static long byteOffset(int i) {
        return ((long) i << shift) + base;
    }

    public AtomicIntegerArray(int length) {
        array = new int[length];
        backoffInterval = 1;
    }

    public AtomicIntegerArray(int length, int backoffInterval) {
        if (backoffInterval < 1) {
            throw new IllegalArgumentException("Backoff interval should be great than 0");
        }
        this.array = new int[length];
        this.backoffInterval = backoffInterval;
    }

    public AtomicIntegerArray(int[] array) {
        this.array = array.clone();
        backoffInterval = 1;
    }

    public AtomicIntegerArray(int[] array, int backoffInterval) {
        if (backoffInterval < 1) {
            throw new IllegalArgumentException("Backoff interval should be great than 0");
        }
        this.array = array.clone();
        this.backoffInterval = backoffInterval;
    }

    public final int length() {
        return array.length;
    }

    public final int get(int i) {
        return getRaw(checkedByteOffset(i));
    }

    private int getRaw(long offset) {
        return unsafe.getIntVolatile(array, offset);
    }

    public final void set(int i, int newValue) {
        unsafe.putIntVolatile(array, checkedByteOffset(i), newValue);
    }

    public final void lazySet(int i, int newValue) {
        unsafe.putOrderedInt(array, checkedByteOffset(i), newValue);
    }

    public final int getAndSet(int i, int newValue) {
        int current;
        do {
            current = get(i);
        } while (!compareAndSet(i, current, newValue));
        return current;
    }

    public final boolean compareAndSet(int i, int expect, int update) {
        return compareAndSetRaw(checkedByteOffset(i), expect, update);
    }

    private boolean compareAndSetRaw(long offset, int expect, int update) {
        if (unsafe.compareAndSwapInt(array, offset, expect, update)) {
            return true;
        }
        unsafe.park(false, backoffInterval);
        return false;
    }

    public final boolean weakCompareAndSet(int i, int expect, int update) {
        return unsafe.compareAndSwapInt(array, checkedByteOffset(i), expect, update);
    }

    public final int getAndIncrement(int i) {
        return getAndAdd(i, 1);
    }

    public final int getAndDecrement(int i) {
        return getAndAdd(i, -1);
    }

    public final int getAndAdd(int i, int delta) {
        int current;
        do {
            current = get(i);
        } while (!compareAndSet(i, current, current + delta));
        return current;
    }

    public final int incrementAndGet(int i) {
        return getAndAdd(i, 1) + 1;
    }

    public final int decrementAndGet(int i) {
        return getAndAdd(i, -1) - 1;
    }

    public int addAndGet(int i, int delta) {
        return getAndAdd(i, delta) + delta;
    }

    public final int getAndUpdate(int i, IntUnaryOperator updateFunction) {
        long offset = checkedByteOffset(i);
        int prev, next;
        do {
            prev = getRaw(offset);
            next = updateFunction.applyAsInt(prev);
        } while (!compareAndSetRaw(offset, prev, next));
        return prev;
    }

    public final int updateAndGet(int i, IntUnaryOperator updateFunction) {
        long offset = checkedByteOffset(i);
        int prev, next;
        do {
            prev = getRaw(offset);
            next = updateFunction.applyAsInt(prev);
        } while (!compareAndSetRaw(offset, prev, next));
        return next;
    }

    public final int getAndAccumulate(int i, int x, IntBinaryOperator accumulatorFunction) {
        long offset = checkedByteOffset(i);
        int prev, next;
        do {
            prev = getRaw(offset);
            next = accumulatorFunction.applyAsInt(prev, x);
        } while (!compareAndSetRaw(offset, prev, next));
        return prev;
    }

    public final int accumulateAndGet(int i, int x, IntBinaryOperator accumulatorFunction) {
        long offset = checkedByteOffset(i);
        int prev, next;
        do {
            prev = getRaw(offset);
            next = accumulatorFunction.applyAsInt(prev, x);
        } while (!compareAndSetRaw(offset, prev, next));
        return next;
    }

    public String toString() {
        int iMax = array.length - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(getRaw(byteOffset(i)));
            if (i == iMax)
                return b.append(']').toString();
            b.append(',').append(' ');
        }
    }


}
