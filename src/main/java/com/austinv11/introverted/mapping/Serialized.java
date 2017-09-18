package com.austinv11.introverted.mapping;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Serialized {

    int ordinal() default -1;

    String boundTo() default "";
}
