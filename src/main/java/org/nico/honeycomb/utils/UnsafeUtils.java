package org.nico.honeycomb.utils;

import java.lang.reflect.Field;

import org.nico.honeycomb.connection.HoneycombConnection;

import sun.misc.Unsafe;

@SuppressWarnings("restriction")
public class UnsafeUtils {
    
    public final static Unsafe UNSAFE = getUnsafe();;
    
    public static Unsafe getUnsafe() {
        Unsafe unsafe = null;
        try {
            Field singleoneInstanceField = Unsafe.class.getDeclaredField("theUnsafe");
            singleoneInstanceField.setAccessible(true);
            unsafe = (Unsafe)singleoneInstanceField.get(null);
        }catch(Exception e) {
            e.printStackTrace();
        }
        return unsafe;
    }
    
    public static long getFieldOffset(Class<?> clazz, String fieldName) {
        try {
           return UNSAFE.objectFieldOffset
                (HoneycombConnection.class.getDeclaredField("status"));
        } catch (Exception ex) { throw new Error(ex); }
    }
}
