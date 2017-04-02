package com.erezbiox1.CommandsAPI;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by Erezbiox1 on 31/03/2017.
 * (C) 2016 Erez Rotem All Rights Reserved.
 */

@Retention(RUNTIME)
@Target(METHOD)
public @interface Command {

    String name() default "";
    String permission() default "";
    String wildcards() default "";
    boolean player() default false;

    String permissionError() default "";
    String playerError() default "";
    String argumentsError() default "";

}
