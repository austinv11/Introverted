package com.austinv11.introverted.mapping;

import com.austinv11.introverted.networking.Packet;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Common abstraction for different methods of reflecting within Java. This is meant as a utility to allow for platform
 * specific optimizations.
 */
public interface Reflector {

    /**
     * Gets the singleton implementation of {@link Reflector}.
     *
     * @param overrideIsUnsafeAvailable Setting this to true forces {@link sun.misc.Unsafe} usage, false forces
     *                                  plain Java reelection usage.
     * @return The (possibly cached) implementation of {@link Reflector}.
     */
    static Reflector instance(boolean overrideIsUnsafeAvailable) {
        return overrideIsUnsafeAvailable ? UnsafeReflector.instance() : ReflectionReflector.instance();
    }

    /**
     * Gets the singleton implementation of {@link Reflector}. This will automatically attempt to find the best
     * implementation for the current platform.
     *
     * @return The (possibly cached) implementation of {@link Reflector}.
     */
    static Reflector instance() {
        return instance(UnsafeReflector.isUnsafeAvailable());
    }

    /**
     * Gets the value of a field.
     *
     * @param clazz The class to get the value from.
     * @param obj The instance containing the value.
     * @param name The name of the field to get the value from.
     * @return The value of the field.
     */
    <T> T get(Class<?> clazz, Object obj, String name);

    /**
     * Gets the fields present in a class with the {@link Serialized} annotation.
     *
     * @param clazz The class to scan for fields.
     * @return The fields found.
     */
    default List<Field> getSerializedFields(Class<?> clazz) {
        List<Field> fieldList = new ArrayList<>();
        while (!clazz.equals(Packet.class) && !clazz.equals(Object.class)) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Serialized.class))
                    fieldList.add(field);
            }
            clazz = clazz.getSuperclass();
        }
        fieldList.sort(Comparator.comparingInt(f -> f.getAnnotation(Serialized.class).value()));
        return fieldList;
    }

    /**
     * Gets a {@link Field} instance for a field name.
     *
     * @param clazz The class to search for the field from.
     * @param name The name of the field to search for.
     * @return The field found.
     */
    default Field getField(Class<?> clazz, String name) {
        while (!clazz.equals(Object.class)) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
            }
            finally {
                clazz = clazz.getSuperclass();
            }
        }

        return null;
    }

    /**
     * This puts a value into a field.
     *
     * @param clazz The class the field belongs to.
     * @param obj The specific instance to insert the value of the field into.
     * @param name The name of the field to mutate.
     * @param value The new value for the field.
     */
    <T> void put(Class<?> clazz, Object obj, String name, T value);

    /**
     * This attempts to generate a new primitive with its default value from a primitive (or wrapper) class..
     *
     * @param type The primitive type.
     * @return The primitive value.
     */
    default <T> T instantiatePrimitive(Class<T> type) {
        if (int.class.equals(type) || Integer.class.equals(type)) {
            return (T)(Integer) 0;
        } else if (long.class.equals(type) || Long.class.equals(type)) {
            return (T)(Long) 0L;
        } else if (double.class.equals(type) || Double.class.equals(type)) {
            return (T)(Double) 0D;
        } else if (void.class.equals(type) || Void.class.equals(type)) {
            return null;
        } else if (float.class.equals(type) || Float.class.equals(type)) {
            return (T)(Float) 0F;
        } else if (byte.class.equals(type) || Byte.class.equals(type)) {
            return (T)(Byte)(byte) 0;
        } else if (char.class.equals(type) || Character.class.equals(type)) {
            return (T)(Character)(char) 0;
        } else if (boolean.class.equals(type) || Boolean.class.equals(type)) {
            return (T)(Boolean) false;
        } else if (short.class.equals(type) || Short.class.equals(type)) {
            return (T)(Short)(short) 0;
        }

        if (type.isArray()) {
            return (T) Array.newInstance(type.getComponentType(), 0);
        }

        return null;
    }

    /**
     * Instantiates a new instance of the provided class.
     *
     * @param type The class to instantiate.
     * @return The new instance of the class.
     */
    <T> T instantiate(Class<T> type);
}

class UnsafeReflector implements Reflector {

    private static volatile UnsafeReflector INSTANCE;
    private static volatile int unsafeCheckState = -1;

    private final sun.misc.Unsafe unsafe; //Need to use fully qualified name to prevent imports when this is unavailable

    static boolean isUnsafeAvailable() {
        if (unsafeCheckState == -1) {
            try {
                Class<?> clazz = Class.forName("sun.misc.Unsafe");
                clazz.getDeclaredField("theUnsafe");
                unsafeCheckState = 1;
            } catch (Throwable t) {
                unsafeCheckState = 0;
            }
        }

        return unsafeCheckState == 1;
    }

    static UnsafeReflector instance() {
        if (INSTANCE == null)
            try {
                INSTANCE = new UnsafeReflector();
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Cannot instantiate!", e);
            }

        return INSTANCE;
    }

    private UnsafeReflector() throws NoSuchFieldException, IllegalAccessException {
        Field theUnsafe = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        unsafe = (sun.misc.Unsafe) theUnsafe.get(null);
    }

    @Override
    public <T> T get(Class<?> clazz, Object obj, String name) {
        Field field = getField(clazz, name);
        if (int.class.equals(field.getType())) {
            return (T) (Integer) unsafe.getInt(obj, unsafe.objectFieldOffset(field));
        } else if (long.class.equals(field.getType())) {
            return (T) (Long) unsafe.getLong(obj, unsafe.objectFieldOffset(field));
        } else if (double.class.equals(field.getType())) {
            return (T) (Double) unsafe.getDouble(obj, unsafe.objectFieldOffset(field));
        } else if (void.class.equals(field.getType())) {
            return null;
        } else if (float.class.equals(field.getType())) {
            return (T) (Float) unsafe.getFloat(obj, unsafe.objectFieldOffset(field));
        } else if (byte.class.equals(field.getType())) {
            return (T) (Byte) unsafe.getByte(obj, unsafe.objectFieldOffset(field));
        } else if (char.class.equals(field.getType())) {
            return (T) (Character) unsafe.getChar(obj, unsafe.objectFieldOffset(field));
        } else if (boolean.class.equals(field.getType())) {
            return (T) (Boolean) unsafe.getBoolean(obj, unsafe.objectFieldOffset(field));
        } else if (short.class.equals(field.getType())) {
            return (T) (Short) unsafe.getShort(obj, unsafe.objectFieldOffset(field));
        } else {
            return (T) unsafe.getObject(obj, unsafe.objectFieldOffset(field));
        }
    }

    @Override
    public <T> void put(Class<?> clazz, Object obj, String name, T value) {
        Field field = getField(clazz, name);
        if (int.class.equals(field.getType())) {
            unsafe.putInt(obj, unsafe.objectFieldOffset(field), (Integer) value);
        } else if (long.class.equals(field.getType())) {
            unsafe.putLong(obj, unsafe.objectFieldOffset(field), (Long) value);
        } else if (double.class.equals(field.getType())) {
            unsafe.putDouble(obj, unsafe.objectFieldOffset(field), (Double) value);
        } else if (void.class.equals(field.getType())) {
            //Pass
        } else if (float.class.equals(field.getType())) {
            unsafe.putFloat(obj, unsafe.objectFieldOffset(field), (Float) value);
        } else if (byte.class.equals(field.getType())) {
            unsafe.putByte(obj, unsafe.objectFieldOffset(field), (Byte) value);
        } else if (char.class.equals(field.getType())) {
            unsafe.putChar(obj, unsafe.objectFieldOffset(field), (Character) value);
        } else if (boolean.class.equals(field.getType())) {
            unsafe.putBoolean(obj, unsafe.objectFieldOffset(field), (Boolean) value);
        } else if (short.class.equals(field.getType())) {
            unsafe.putShort(obj, unsafe.objectFieldOffset(field), (Short) value);
        } else {
            unsafe.putObject(obj, unsafe.objectFieldOffset(field), value);
        }
    }

    @Override
    public <T> T instantiate(Class<T> type) {
        if (type.isArray() || type.isPrimitive())
            return instantiatePrimitive(type);

        try {
            return (T) unsafe.allocateInstance(type);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }
}

class ReflectionReflector implements Reflector {

    private static volatile ReflectionReflector INSTANCE;

    static ReflectionReflector instance() {
        if (INSTANCE == null)
            INSTANCE = new ReflectionReflector();

        return INSTANCE;
    }

    @Override
    public <T> T get(Class<?> clazz, Object obj, String name) {
        Field field = getField(clazz, name);
        try {
            field.setAccessible(true);
            if (int.class.equals(field.getType())) {
                return (T) (Integer) field.getInt(obj);
            } else if (long.class.equals(field.getType())) {
                return (T) (Long) field.getLong(obj);
            } else if (double.class.equals(field.getType())) {
                return (T) (Double) field.getDouble(obj);
            } else if (void.class.equals(field.getType())) {
                return null;
            } else if (float.class.equals(field.getType())) {
                return (T) (Float) field.getFloat(obj);
            } else if (byte.class.equals(field.getType())) {
                return (T) (Byte) field.getByte(obj);
            } else if (char.class.equals(field.getType())) {
                return (T) (Character) field.getChar(obj);
            } else if (boolean.class.equals(field.getType())) {
                return (T) (Boolean) field.getBoolean(obj);
            } else if (short.class.equals(field.getType())) {
                return (T) (Short) field.getShort(obj);
            } else {
                return (T) field.get(obj);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> void put(Class<?> clazz, Object obj, String name, T value) {
        Field field = getField(clazz, name);
        try {
            field.setAccessible(true);
            if (int.class.equals(field.getType())) {
                field.setInt(obj, (Integer) value);
            } else if (long.class.equals(field.getType())) {
                field.setLong(obj, (Long) value);
            } else if (double.class.equals(field.getType())) {
                field.setDouble(obj, (Double) value);
            } else if (void.class.equals(field.getType())) {
                //Pass
            } else if (float.class.equals(field.getType())) {
                field.setFloat(obj, (Float) value);
            } else if (byte.class.equals(field.getType())) {
                field.setByte(obj, (Byte) value);
            } else if (char.class.equals(field.getType())) {
                field.setChar(obj, (Character) value);
            } else if (boolean.class.equals(field.getType())) {
                field.setBoolean(obj, (Boolean) value);
            } else if (short.class.equals(field.getType())) {
                field.setShort(obj, (Short) value);
            } else {
                field.set(obj, value);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T instantiate(Class<T> type) {
        try {
            if (type.getConstructors().length == 0)
                return type.newInstance();
            else {
                Constructor<T> constructor = type.getConstructor();
                constructor.setAccessible(true);
                return constructor.newInstance();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
