package com.erezbiox1.CommandsAPI;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by Erezbiox1 on 31/03/2017.
 * (C) 2016 Erez Rotem All Rights Reserved.
 */

@SuppressWarnings("unused")
@Retention(RUNTIME)
@Target(METHOD)
public @interface Command {

    String name() default ""; // Command main name.

    String permission() default ""; // Command would only run if the sender has this permission.
    String arguments() default ""; // Command would only run if the arguments are met, with the exception of the wildcards symbol ( * ).

    String permissionError() default "default"; // Insufficient Permission Error.
    String argumentsError() default "default"; // Invalid arguments Error.
    String playerError() default "default";  // Sender must be a player Error.

}
