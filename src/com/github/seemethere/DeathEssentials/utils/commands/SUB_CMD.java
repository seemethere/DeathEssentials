package com.github.seemethere.DeathEssentials.utils.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SUB_CMD {
    /**
     * @return Parent of subcommand
     */
    String parent();

    /**
     * @return Name of subcommand
     */
    String name();

    /**
     * @return Usage warning
     */
    String usage() default "&3ERROR: &4Invalid usage!";

    /**
     * @return Minimum number of arguments
     */
    int min() default 0;

    /**
     * @return Maximum number of arguments. -1 indicates infinite
     */
    int max() default -1;

    /**
     * @return Full description of subcommand
     */
    String description() default "";

    /**
     * @return Permission associated with the subcommmand
     */
    String permission() default "";

    /**
     * @return Permission error message
     */
    String permissionsMessage() default "&3ERROR: &4Insufficient permissions!";

    /**
     * @return Allow Console or not
     */
    boolean AllowConsole() default false;
}
