package ky.korins.atomic;

import sun.misc.Unsafe;

import java.util.function.LongBinaryOperator;
import java.util.function.LongUnaryOperator;

public class AtomicLongArray implements java.io.Serializable {
    private static final long serialVersionUID = -800117181216365580L;

    private final long backoffInterval;

    private static final Unsafe unsafe = Java9Unsafe.getUnsafe();

    private static final int base = unsafe.arrayBaseOffset(long[].class);

    private final long[] array;

    static {
        int scale = unsafe.arrayIndexScale(long[].class);
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

    public AtomicLongArray(int length) {
        array = new long[length];
        backoffInterval = 1;
    }

    public AtomicLongArray(int length, long backoffInterval) {
        if (backoffInterval < 1) {
            throw new IllegalArgumentException("Backoff interval should be great than 0");
        }
        this.array = new long[length];
        this.backoffInterval = backoffInterval;
    }

    public AtomicLongArray(long[] array) {
        this.array = array.clone();
        backoffInterval = 1;
    }

    public AtomicLongArray(long[] array, long backoffInterval) {
        if (backoffInterval < 1) {
            throw new IllegalArgumentException("Backoff interval should be great than 0");
        }
        this.array = array.clone();
        this.backoffInterval = backoffInterval;
    }

    public final int length() {
        return array.length;
    }

    public final long get(int i) {
        return getRaw(checkedByteOffset(i));
    }

    private long getRaw(long offset) {
        return unsafe.getLongVolatile(array, offset);
    }

    public final void set(int i, long newValue) {
        unsafe.putLongVolatile(array, checkedByteOffset(i), newValue);
    }

    public final void lazySet(int i, long newValue) {
        unsafe.putOrderedLong(array, checkedByteOffset(i), newValue);
    }

    public final long getAndSet(int i, long newValue) {
        long current;
        do {
            current = get(i);
        } while (!compareAndSet(i, current, newValue));
        return current;
    }

    public final boolean compareAndSet(int i, long expect, long update) {
        return compareAndSetRaw(checkedByteOffset(i), expect, update);
    }

    private boolean compareAndSetRaw(long offset, long expect, long update) {
        if (unsafe.compareAndSwapLong(array, offset, expect, update)) {
            return true;
        }
        unsafe.park(false, backoffInterval);
        return false;
    }

    public final boolean weakCompareAndSet(int i, long expect, long update) {
        return unsafe.compareAndSwapLong(array, i, expect, update);
    }

    public final long getAndIncrement(int i) {
        return getAndAdd(i, 1);
    }

    public final long getAndDecrement(int i) {
        return getAndAdd(i, -1);
    }

    public final long getAndAdd(int i, long delta) {
        long current;
        do {
            current = get(i);
        } while (!compareAndSet(i, current, current + delta));
        return current;
    }

    public final long incrementAndGet(int i) {
        return getAndAdd(i, 1) + 1;
    }

    public final long decrementAndGet(int i) {
        return getAndAdd(i, -1) - 1;
    }

    public long addAndGet(int i, long delta) {
        return getAndAdd(i, delta) + delta;
    }

    public final long getAndUpdate(int i, LongUnaryOperator updateFunction) {
        long offset = checkedByteOffset(i);
        long prev, next;
        do {
            prev = getRaw(offset);
            next = updateFunction.applyAsLong(prev);
        } while (!compareAndSetRaw(offset, prev, next));
        return prev;
    }

    public final long updateAndGet(int i, LongUnaryOperator updateFunction) {
        long offset = checkedByteOffset(i);
        long prev, next;
        do {
            prev = getRaw(offset);
            next = updateFunction.applyAsLong(prev);
        } while (!compareAndSetRaw(offset, prev, next));
        return next;
    }

    public final long getAndAccumulate(int i, long x,
                                       LongBinaryOperator accumulatorFunction) {
        long offset = checkedByteOffset(i);
        long prev, next;
        do {
            prev = getRaw(offset);
            next = accumulatorFunction.applyAsLong(prev, x);
        } while (!compareAndSetRaw(offset, prev, next));
        return prev;
    }

    public final long accumulateAndGet(int i, long x,
                                       LongBinaryOperator accumulatorFunction) {
        long offset = checkedByteOffset(i);
        long prev, next;
        do {
            prev = getRaw(offset);
            next = accumulatorFunction.applyAsLong(prev, x);
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
