package com.austinv11.introverted.networking;

import com.austinv11.introverted.common.Introverted;
import org.apache.commons.lang3.ClassUtils;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This class allows for both reading and writing of introverted packets.
 */
public class PacketBuffer {

    private static final int INITIAL_SIZE = 16;
    private static final int SIZE_EXPANSION_FACTOR = 2;

    private static final byte BOOLEAN = 0;
    private static final byte U_INT = 1;
    private static final byte INT = 2;
    private static final byte U_LONG = 3;
    private static final byte LONG = 4;
    private static final byte CHAR = 5;
    private static final byte DECIMAL = 6;
    private static final byte ARRAY = 7;
    private static final byte STR = 8;
    private static final byte NIL = 9;
    private static final byte MAP = 10;

    private static final int MASK = 0xff;

    private volatile byte[] buf;
    private volatile int pointer = 0;

    /**
     * Creates a buffer with preset contents and a non-zero pointer.
     *
     * @param buf The buffer.
     * @param startPointer The pointer to start at.
     */
    public PacketBuffer(byte[] buf, int startPointer) {
        this.buf = buf;
        this.pointer = startPointer;
    }

    /**
     * Wraps a byte array with a PacketBuffer instance.
     *
     * @param buf The buffer to wrap.
     */
    public PacketBuffer(byte[] buf) {
        this(buf, 0);
    }

    /**
     * Creates a new PacketBuffer without any contents.
     */
    public PacketBuffer() {
        this.buf = new byte[INITIAL_SIZE];
    }

    private void expand() {
        buf = Arrays.copyOf(buf, buf.length * SIZE_EXPANSION_FACTOR);
    }

    private synchronized void safePut(byte... bytes) {
        if (pointer > 1 && pointer < 6) //We wanna skip the size bytes as that has its own mechanism
            skipMeta();

        while (size() + bytes.length > buf.length)
            expand();

        for (int i = 0; i < bytes.length; i++)
            buf[pointer++] = bytes[i];
    }

    private synchronized byte next() {
        return buf[pointer++];
    }

    private byte[] nextBytes(int length) {
        byte[] toReturn = new byte[length];
        for (int i = 0; i < length; i++)
            toReturn[i] = next();
        return toReturn;
    }

    private synchronized byte peek(int pos) {
        return buf[pos];
    }

    private byte peek() {
        return peek(pointer);
    }

    private synchronized void move(int amount) {
        pointer += amount;
    }

    private synchronized void skipMeta() {
        if (pointer < 6)
            pointer = 6;
    }

    private void assertByte(byte shouldEqual) {
        if (next() != shouldEqual)
            throw new IllegalStateException(String.format("Expected %d at position %d, got %d instead!", shouldEqual, pointer, peek()));
    }

    /**
     * This injects the value of {@link Introverted#VERSION} onto the buffer.
     *
     * @return The current buffer instance.
     */
    public PacketBuffer putVersion() {
        assertPosition(0);
        safePut(Introverted.VERSION);
        return this;
    }

    /**
     * This gets the Introverted version value from the buffer.
     *
     * @return The version number.
     */
    public byte getVersion() {
        return peek(0);
    }

    /**
     * This injects an opcode onto the buffer.
     *
     * @param op The opcode to inject.
     * @return The current buffer instance.
     */
    public PacketBuffer putOp(PacketType op) {
        assertPosition(1);
        safePut((byte) op.ordinal());
        return this;
    }

    /**
     * Gets the op represented in the current buffer.
     *
     * @return The op stored.
     */
    public PacketType getOp() {
        return PacketType.values()[peek(1)];
    }

    /**
     * Appends a boolean onto the buffer.
     *
     * @param bool The value to append.
     * @return The current buffer instance.
     */
    public PacketBuffer putBoolean(boolean bool) {
        safePut(BOOLEAN, (byte) (bool ? 1 : 0));
        return this;
    }

    /**
     * Gets the next value on the buffer as a boolean.
     *
     * <b>NOTE:</b> This will move the pointer forward if successful.
     *
     * @return The value.
     */
    public boolean getBoolean() {
        skipMeta();
        assertByte(BOOLEAN);
        return next() == 1;
    }

    /**
     * Appends an unsigned int onto the buffer.
     *
     * @param uint The value to append.
     * @return The current buffer instance.
     */
    public PacketBuffer putUInt(long uint) {
        safePut(U_INT,
                (byte) (uint >> 24), (byte) ((uint >> 16) & MASK), (byte) ((uint >> 8) & MASK), (byte) (uint & MASK));
        return this;
    }

    /**
     * Gets the next value on the buffer as an unsigned integer.
     *
     * <b>NOTE:</b> This will move the pointer forward if successful.
     *
     * @return The value.
     */
    public long getUInt() {
        skipMeta();
        assertByte(U_INT);
        byte[] data = nextBytes(4);
        long uint = 0;
        uint |= (data[0] & MASK);
        uint <<= 8;
        uint |= (data[1] & MASK);
        uint <<= 8;
        uint |= (data[2] & MASK);
        uint <<= 8;
        uint |= (data[3] & MASK);
        return uint;
    }

    private PacketBuffer _putInt(byte type, int integer) {
        safePut(type,
                (byte) (integer >> 24), (byte) ((integer >> 16) & MASK), (byte) ((integer >> 8) & MASK), (byte) (integer & MASK));
        return this;
    }

    private int _getInt(byte type) {
        skipMeta();
        assertByte(type);
        byte[] data = nextBytes(4);
        int integer = 0;
        integer |= (data[0] & MASK);
        integer <<= 8;
        integer |= (data[1] & MASK);
        integer <<= 8;
        integer |= (data[2] & MASK);
        integer <<= 8;
        integer |= (data[3] & MASK);
        return integer;
    }

    /**
     * Appends an int onto the buffer.
     *
     * @param integer The value to append.
     * @return The current buffer instance.
     */
    public PacketBuffer putInt(int integer) {
       return _putInt(INT, integer);
    }

    /**
     * Gets the next value on the buffer as an integer.
     *
     * <b>NOTE:</b> This will move the pointer forward if successful.
     *
     * @return The value.
     */
    public int getInt() {
        return _getInt(INT);
    }

    private PacketBuffer _putLong(byte type, long longval) {
        safePut(type,
                (byte) (longval >> 56), (byte) ((longval >> 48) & MASK), (byte) ((longval >> 40) & MASK), (byte) (longval >> 32 & MASK),
                (byte) ((longval >> 24) & MASK), (byte) ((longval >> 16) & MASK), (byte) ((longval >> 8) & MASK), (byte) (longval & MASK));
        return this;
    }

    private long _getLong(byte type) {
        skipMeta();
        assertByte(type);
        byte[] data = nextBytes(8);
        long val = 0;
        val |= (data[0] & MASK);
        val <<= 8;
        val |= (data[1] & MASK);
        val <<= 8;
        val |= (data[2] & MASK);
        val <<= 8;
        val |= (data[3] & MASK);
        val <<= 8;
        val |= (data[4] & MASK);
        val <<= 8;
        val |= (data[5] & MASK);
        val <<= 8;
        val |= (data[6] & MASK);
        val <<= 8;
        val |= (data[7] & MASK);
        return val;
    }

    /**
     * Appends an unsigned long onto the buffer.
     *
     * @param ulong The value to append.
     * @return The current buffer instance.
     */
    public PacketBuffer putULong(long ulong) {
        return _putLong(U_LONG, ulong);
    }

    /**
     * Gets the next value on the buffer as an unsigned long.
     *
     * <b>NOTE:</b> This will move the pointer forward if successful.
     *
     * @return The value.
     */
    public long getULong() {
        return _getLong(U_LONG);
    }

    /**
     * Appends a long onto the buffer.
     *
     * @param longVal The value to append.
     * @return The current buffer instance.
     */
    public PacketBuffer putLong(long longVal) {
        return _putLong(LONG, longVal);
    }

    /**
     * Gets the next value on the buffer as a long.
     *
     * <b>NOTE:</b> This will move the pointer forward if successful.
     *
     * @return The value.
     */
    public long getLong() {
        return _getLong(LONG);
    }

    /**
     * Appends a character onto the buffer.
     *
     * @param character The value to append.
     * @return The current buffer instance.
     */
    public PacketBuffer putChar(char character) {
        return _putInt(CHAR, (int) character);
    }

    /**
     * Gets the next value on the buffer as a character.
     *
     * <b>NOTE:</b> This will move the pointer forward if successful.
     *
     * @return The value.
     */
    public char getChar() {
        return (char) _getInt(CHAR);
    }

    /**
     * Appends a decimal value onto the buffer.
     *
     * @param value The value to append.
     * @return The current buffer instance.
     */
    public PacketBuffer putDecimal(double value) {
        return _putLong(DECIMAL, Double.doubleToLongBits(value));
    }

    /**
     * Gets the next value on the buffer as a decimal value.
     *
     * <b>NOTE:</b> This will move the pointer forward if successful.
     *
     * @return The value.
     */
    public double getDecimal() {
        return Double.longBitsToDouble(_getLong(DECIMAL));
    }

    private void _startPutArray(int len) {
        _putInt(ARRAY, len);
    }

    /**
     * Appends an array onto the buffer.
     *
     * @param array The value to append.
     * @return The current buffer instance.
     */
    public PacketBuffer putArray(boolean[] array) {
        _startPutArray(array.length);
        for (boolean o : array)
            putBoolean(o);
        return this;
    }

    /**
     * Appends an array onto the buffer.
     *
     * @param array The value to append.
     * @return The current buffer instance.
     */
    public PacketBuffer putArray(int[] array) {
        _startPutArray(array.length);
        for (int o : array)
            putInt(o);
        return this;
    }

    /**
     * Appends an array onto the buffer.
     *
     * @param array The value to append.
     * @return The current buffer instance.
     */
    public PacketBuffer putArray(long[] array) {
        _startPutArray(array.length);
        for (long o : array)
            putLong(o);
        return this;
    }

    /**
     * Appends an array onto the buffer.
     *
     * @param array The value to append.
     * @return The current buffer instance.
     */
    public PacketBuffer putArray(char[] array) {
        _startPutArray(array.length);
        for (char o : array)
            putChar(o);
        return this;
    }

    /**
     * Appends an array onto the buffer.
     *
     * @param array The value to append.
     * @return The current buffer instance.
     */
    public PacketBuffer putArray(double[] array) {
        _startPutArray(array.length);
        for (double o : array)
            putDecimal(o);
        return this;
    }

    /**
     * Appends an array onto the buffer.
     *
     * @param array The value to append.
     * @return The current buffer instance.
     */
    public <T> PacketBuffer putArray(T[] array) {
        _startPutArray(array.length);
        for (T o : array)
            put(o);
        return this;
    }

    /**
     * Appends an array onto the buffer.
     *
     * @param collection The value to append.
     * @return The current buffer instance.
     */
    public <T> PacketBuffer putArray(Collection<T> collection) {
        _startPutArray(collection.size());
        for (T o : collection)
            put(o);
        return this;
    }

    /**
     * Gets the next value on the buffer as an array.
     *
     * <b>NOTE:</b> This will move the pointer forward if successful.
     *
     * @return The value.
     */
    public Object[] getArray() { //Generics + java arrays don't work well together :(
        int len = _getInt(ARRAY);
        Object[] array = new Object[len];
        for (int i = 0; i < len; i++)
            array[i] = getNext();
        return array;
    }

    /**
     * Appends a string onto the buffer.
     *
     * @param string The value to append.
     * @return The current buffer instance.
     */
    public PacketBuffer putStr(String string) {
        byte[] data;
        try {
            data = string.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace(); //Should never happen
            data = new byte[0]; //Because javac
        }
        _putInt(STR, data.length);
        safePut(data);
        return this;
    }

    /**
     * Gets the next value on the buffer as a string.
     *
     * <b>NOTE:</b> This will move the pointer forward if successful.
     *
     * @return The value.
     */
    public String getStr() {
        int len = _getInt(STR);
        byte[] bytes = nextBytes(len);
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace(); //Should never happen
            return "RUH OH"; //Because javac
        }
    }

    /**
     * Appends a nil value onto the buffer.
     *
     * @return The current buffer instance.
     */
    public PacketBuffer putNil() {
        safePut(NIL);
        return this;
    }

    /**
     * Gets the next value on the buffer as a nil.
     *
     * <b>NOTE:</b> This will move the pointer forward if successful.
     *
     * @return The value.
     */
    public void getNil() {
        skipMeta();
        assertByte(NIL);
        next();
    }

    /**
     * Appends an map onto the buffer.
     *
     * @param map The value to append.
     * @return The current buffer instance.
     */
    public <K, V> PacketBuffer putMap(Map<K, V> map) {
        _putInt(MAP, map.size());
        for (Map.Entry<K, V> entry : map.entrySet()) {
            put(entry.getKey());
            put(entry.getValue());
        }
        return this;
    }

    /**
     * Gets the next value on the buffer as a map and inserts it into the provided mutable map.
     *
     * <b>NOTE:</b> This will move the pointer forward if successful.
     *
     * @return The value.
     */
    public <K, V> Map<K, V> getMap(Map<K, V> mutableMap) {
        int len = _getInt(MAP);
        for (int i = 0; i < len; i++)
            mutableMap.put((K) getNext(), (V)getNext());
        return mutableMap;
    }

    /**
     * Gets the next value on the buffer as a map.
     *
     * <b>NOTE:</b> This will move the pointer forward if successful.
     *
     * @return The value.
     */
    public Map getMap() {
        return getMap(new HashMap<>());
    }

    /**
     * Appends an arbitrary object onto the buffer.
     *
     * @param object The value to append.
     * @return The current buffer instance.
     */
    public PacketBuffer put(Object object) {
        return put(object, false);
    }
    /**
     * Appends an arbitrary object onto the buffer.
     *
     * @param object The value to append.
     * @param preferUnsigned When true, this will use unsigned types when possible.
     * @return The current buffer instance.
     */
    public PacketBuffer put(Object object, boolean preferUnsigned) {
        if (object == null)
            putNil();
        else if (ClassUtils.isPrimitiveOrWrapper(object.getClass())) {
            if (object.getClass().equals(Void.class))
                putNil();
            else if (object.getClass().equals(Byte.class)) {
                if (preferUnsigned) {
                    putUInt((Byte) object);
                } else {
                    putInt((Byte) object);
                }
            }
            else if (object.getClass().equals(Short.class))
                if (preferUnsigned) {
                    putUInt((Short) object);
                } else {
                    putInt((Short) object);
                }
            else if (object.getClass().equals(Integer.class))
                if (preferUnsigned) {
                    putUInt((Integer) object);
                } else {
                    putInt((Integer) object);
                }
            else if (object.getClass().equals(Float.class))
                putDecimal((Float) object);
            else if (object.getClass().equals(Double.class))
                putDecimal((Double) object);
            else if (object.getClass().equals(Character.class))
                putChar((Character) object);
            else if (object.getClass().equals(Boolean.class))
                putBoolean((Boolean) object);
            else if (object.getClass().equals(Long.class))
                if (preferUnsigned) {
                    putULong((Long) object);
                } else {
                    putLong((Long) object);
                }
            else {
                throw new IllegalArgumentException(String.format("Cannot serialize type %s!", object.getClass()));
            }
        } else {
            if (object instanceof String)
                putStr((String) object);
            else if (object.getClass().isArray())
                putArray((Object[]) object);
            else if (object instanceof Map)
                putMap((Map) object);
            else if (object instanceof Collection)
                putArray((Collection) object);
            else {
                throw new IllegalArgumentException(String.format("Cannot serialize type %s!", object.getClass()));
            }
        }
        return this;
    }

    /**
     * Gets the next value on the buffer.
     *
     * <b>NOTE:</b> This will move the pointer forward if successful.
     *
     * @return The value.
     */
    public Object getNext() {
        skipMeta();
        switch (peek()) {
            case BOOLEAN:
                return getBoolean();
            case U_INT:
                return getUInt();
            case INT:
                return getInt();
            case U_LONG:
                return getULong();
            case LONG:
                return getLong();
            case CHAR:
                return getChar();
            case DECIMAL:
                return getDecimal();
            case ARRAY:
                return getArray();
            case STR:
                return getStr();
            case NIL:
                getNil();
                return null;
            case MAP:
                return getMap();
            default:
                throw new IllegalArgumentException(String.format("Cannot deserialize type %d!", peek()));
        }
    }

    private void assertPosition(int position) {
        if (this.pointer != position)
            throw new IllegalStateException(String.format("Pointer expected at position %d, is at %d instead!", position, this.pointer));
    }

    private void skipVersion() {
        movePointer(1);
    }

    /**
     * Moves the pointer to a specified position.
     *
     * @param newPosition The new position that the pointer should point to in the buffer.
     */
    public synchronized void movePointer(int newPosition) {
        pointer = newPosition;
    }

    /**
     * Gets the current size of the buffer.
     *
     * @return The buffer size.
     */
    public int size() {
        return pointer;
    }

    /**
     * Resets the contents of the buffer.
     */
    public void reset() {
        movePointer(0);
    }

    private void updateSize() {
        int size = pointer - 6; //Ignore metadata in the size
        buf[2] = (byte) (size >> 24);
        buf[3] = (byte) ((size >> 16) & MASK);
        buf[4] = (byte) ((size >> 8) & MASK);
        buf[5] = (byte) (size & MASK);
    }

    /**
     * Flushes the contents of this buffer into a byte array and resets the contents.
     *
     * @return The current buffer contents.
     */
    public byte[] flush() {
        updateSize();
        //Don't need to worry about zeroing, the data will be overwritten eventually.
        byte[] toReturn = Arrays.copyOfRange(buf, 0, pointer);
        reset();
        return toReturn;
    }
}
