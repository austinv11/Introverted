package com.austinv11.introverted.mapping;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Serialized {

    boolean unsigned() default false;
}
