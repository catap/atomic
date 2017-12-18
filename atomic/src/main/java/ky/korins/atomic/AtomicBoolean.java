package ky.korins.atomic;

import sun.misc.Unsafe;

import java.util.concurrent.locks.LockSupport;

public class AtomicBoolean implements java.io.Serializable {

    private static final Unsafe unsafe = Java9Unsafe.getUnsafe();

    private static final long offset;

    static {
        try {
            offset = unsafe.objectFieldOffset(AtomicBoolean.class.getDeclaredField("value"));
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }

    private volatile int value;

    public AtomicBoolean(boolean initialValue) {
        value = initialValue ? 1 : 0;
    }

    public AtomicBoolean() {
    }

    public final boolean get() {
        return value != 0;
    }

    public final boolean compareAndSet(boolean expect, boolean update) {
        int e = expect ? 1 : 0;
        int u = update ? 1 : 0;
        if (unsafe.compareAndSwapInt(this, offset, e, u)) {
            return true;
        }
        LockSupport.parkNanos(1);
        return false;
    }

    public boolean weakCompareAndSet(boolean expect, boolean update) {
        int e = expect ? 1 : 0;
        int u = update ? 1 : 0;
        return unsafe.compareAndSwapInt(this, offset, e, u);
    }

    public final void set(boolean newValue) {
        value = newValue ? 1 : 0;
    }

    public final void lazySet(boolean newValue) {
        int v = newValue ? 1 : 0;
        unsafe.putOrderedInt(this, offset, v);
    }

    public final boolean getAndSet(boolean newValue) {
        boolean prev;
        do {
            prev = get();
        } while (!compareAndSet(prev, newValue));
        return prev;
    }

    public String toString() {
        return Boolean.toString(get());
    }
}