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
     * Indicates whether or not the Console is allowed
     * to run a command
     *
     * @return Can the console run this particular command
     */
    boolean AllowConsole() default true;

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface SUB {
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
}
