package com.austinv11.introverted.test;

import com.austinv11.introverted.networking.PacketBuffer;
import com.austinv11.introverted.networking.PacketType;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class PacketBufferTest {

    private PacketBuffer newBuffer() {
        PacketBuffer buffer = new PacketBuffer();
        buffer.putVersion();
        buffer.putOp(PacketType.DISCOVERY);
        return buffer;
    }

    private PacketBuffer newBuffer(PacketBuffer buffer) {
        return new PacketBuffer(buffer.flush(), 0);
    }

    @Test
    public void testBoolean() {
        PacketBuffer buf = newBuffer().putBoolean(false).putBoolean(true);
        buf.reset();
        assertEquals(buf.getBoolean(), false);
        assertEquals(buf.getBoolean(), true);
    }

    @Test
    public void testUInt() {
        PacketBuffer buf = newBuffer().putUInt(12345);
        buf.reset();
        assertEquals(buf.getUInt(), 12345);
    }

    @Test
    public void testInt() {
        PacketBuffer buf = newBuffer().putInt(12345);
        buf.reset();
        assertEquals(buf.getInt(), 12345);
    }

    @Test
    public void testULong() {
        PacketBuffer buf = newBuffer().putULong(1234567890123L);
        buf.reset();
        assertEquals(buf.getULong(), 1234567890123L);
    }

    @Test
    public void testLong() {
        PacketBuffer buf = newBuffer().putLong(1234567890123L);
        buf.reset();
        assertEquals(buf.getLong(), 1234567890123L);
    }

    @Test
    public void testChar() {
        PacketBuffer buf = newBuffer().putChar('a');
        buf.reset();
        assertEquals(buf.getChar(), 'a');
    }

    @Test
    public void testDecimal() {
        PacketBuffer buf = newBuffer().putDecimal(1.23456D);
        buf.reset();
        assertEquals(buf.getDecimal(), 1.23456D, .000001);
    }

    @Test
    public void testArray() {
        int[] arr1 = new int[]{1,2,3,4};
        String[] arr2 = new String[]{"hello", "world"};
        PacketBuffer buf = newBuffer().putArray(arr1).putArray(arr2);
        buf.reset();
        Object[] testArr = buf.getArray();
        for (int i = 0; i < testArr.length; i++) {
            assertTrue(testArr[i] instanceof Integer);
            assertEquals((Integer) testArr[i], (Integer) arr1[i]);
        }
        Object[] testArr2 = buf.getArray();
        for (int i = 0; i < testArr2.length; i++) {
            assertTrue(testArr2[i] instanceof String);
            assertEquals((String) testArr2[i], arr2[i]);
        }
    }

    @Test
    public void testString() {
        PacketBuffer buf = newBuffer().putStr("hi");
        buf.reset();
        assertEquals(buf.getStr(), "hi");
    }

    @Test
    public void testNil() {
        PacketBuffer buf = newBuffer().putNil();
        buf.reset();
        buf.getNil(); //Returns void so we are just checking that no exception is thrown
    }

    @Test
    public void testMap() {
        Map<String, String> map = new HashMap<>();
        map.put("Hello", "World");
        PacketBuffer buf = newBuffer().putMap(map);
        buf.reset();
        assertEquals(buf.getMap().get("Hello"), "World");
    }

    @Test
    public void testParse() {
        PacketBuffer buf = newBuffer().put("Hello World");
        PacketBuffer buf2 = new PacketBuffer(buf.flush());
        assertEquals(buf2.getStr(), "Hello World");
    }

    @Test(expected = IllegalStateException.class)
    public void testTypeMismatch() {
        PacketBuffer buf = newBuffer().put("hi");
        buf.getInt();
    }
}
