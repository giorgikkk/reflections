package com.example.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  name, column name
 *  dateFormat. date format by default:  <code>yyyy-MM-dd HH:mm:ss</code>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Column {
    String name();

    String dateFormat() default "yyyy-MM-dd HH:mm:ss";
}
