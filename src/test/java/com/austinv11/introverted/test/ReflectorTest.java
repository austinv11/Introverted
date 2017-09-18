package com.austinv11.introverted.test;

import com.austinv11.introverted.mapping.Reflector;
import com.austinv11.introverted.mapping.Serialized;
import org.junit.Test;

import static org.junit.Assert.*;

public class ReflectorTest {

    @Test
    public void testInstantiation() {
        assertNotNull(Reflector.instance());
        assertNotNull(Reflector.instance(false)); //Not forcing unsafe because the running machine might actually not have it available
    }

    @Test
    public void testFieldList() {
        assertEquals(Reflector.instance().getFields(TestClass.class, Serialized.class).size(), 2);
    }

    @Test
    public void testFieldGet() {
        assertNotNull(Reflector.instance().getField(TestClass.class, "serialized1"));
    }

    @Test
    public void testGet() {
        assertEquals(Reflector.instance().get(TestClass.class, new TestClass(), "serialized1"), "hi1");
    }

    @Test
    public void testSet() {
        TestClass testClass = new TestClass();
        Reflector.instance().put(testClass.getClass(), testClass, "serialized1", "haha!");
        assertEquals(testClass.serialized1, "haha!");
    }

    @Test
    public void testInstantiate() {
        assertNotNull(Reflector.instance().instantiate(TestClass.class));
    }

    private static class TestClass {

        private String nonSerialized = "hi";
        @Serialized
        private String serialized1 = "hi1";
        @Serialized
        private String serialized2 = "hi2";

        public TestClass() {}
    }
}
