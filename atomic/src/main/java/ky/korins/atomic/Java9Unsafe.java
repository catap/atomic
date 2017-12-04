package ky.korins.atomic;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

class Java9Unsafe {
    static Unsafe getUnsafe() {
        try {
            Field theUnsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            return  (Unsafe) theUnsafeField.get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new Error(e);
        }
    }
}
