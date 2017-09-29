package com.austinv11.introverted.mapping;

import java.lang.annotation.*;

/**
 * This represents a field which should be serialized when a packet is sent over the wire.
 *
 * NOTE: The order of the fields DO matter!
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Serialized {

    /**
     * If the field represents a number with no decimal points, whether the number should be considered unsigned.
     *
     * @return True if the number is unsigned, false if signed.
     */
    boolean unsigned() default false;
}
