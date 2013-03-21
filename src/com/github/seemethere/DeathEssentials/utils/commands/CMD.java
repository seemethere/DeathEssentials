package com.github.seemethere.DeathEssentials.utils.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CMD {

    /**
     * Primary command name
     *
     * @return Primary name of command
     */
    String command();

    /**
     * Aliases for commands, if any
     *
     * @return aliases
     */
    String aliases() default "";

    /**
     * Usage string for arguments passed to the command
     *
     * @return usage
     */
    String usage() default "&3ERROR: &4Inavlid usage!";

    /**
     * Short description for command's function
     *
     * @return description
     */
    String description();

    /**
     * Full description of command's function
     *
     * @return help
     */
    String[] help() default {};

    /**
     * Permissions, if any, associated with a command
     *
     * @return permissions
     */
    String permission() default "";

    /**
     * Permissions error message
     *
     * @return permissions error message
     */
    String permissionsMessage() default "&3ERROR: &4Insufficient permissions!";

    /**
     * @return Minimum amount of arguments for a command
     */
    int min() default 0;

    /**
     * -1 here indicates an unlimited number of arguments
     *
     * @return Maximum amount of arguments for a command
     */
    int max() default -1;

    /**
     * Indicates whether or not the Console is allowed
     * to run a command
     *
     * @return Can the console run this particular command
     */
    boolean AllowConsole() default true;
}
