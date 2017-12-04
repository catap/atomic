package ky.korins.atomic;

import sun.misc.Unsafe;

public class AtomicBoolean implements java.io.Serializable {

    private final long backoffInterval;

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

    public AtomicBoolean(boolean initialValue, long backoffInterval) {
        if (backoffInterval < 1) {
            throw new IllegalArgumentException("Backoff interval should be great than 0");
        }
        this.value = initialValue ? 1 : 0;
        this.backoffInterval = backoffInterval;
    }

    public AtomicBoolean(boolean initialValue) {
        value = initialValue ? 1 : 0;
        backoffInterval = 1L;
    }

    public AtomicBoolean() {
        backoffInterval = 1L;
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
        unsafe.park(false, backoffInterval);
        return false;
    }

    public boolean weakCompareAndSet(boolean expect, boolean update) {
        return compareAndSet(expect, update);
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