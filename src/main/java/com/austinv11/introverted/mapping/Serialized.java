package com.austinv11.introverted.mapping;

import java.lang.annotation.*;

/**
 * This represents a field which should be serialized when a packet is sent over the wire.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Serialized {

    /**
     * The index of the field relative to others.
     *
     * @return The index.
     */
    int value();

    /**
     * If the field represents a number with no decimal points, whether the number should be considered unsigned.
     *
     * @return True if the number is unsigned, false if signed.
     */
    boolean unsigned() default false;
}
