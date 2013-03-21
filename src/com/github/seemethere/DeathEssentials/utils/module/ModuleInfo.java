package com.github.seemethere.DeathEssentials.utils.module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModuleInfo {
    /**
     * @return Name of module
     */
    String name();

    /**
     * @return Description of module
     */
    String description() default "";

    /**
     * @return True/False of WorldGuard usage
     */
    boolean WorldGuard() default false;

    /**
     * @return True/False of Chat usage
     */
    boolean Chat() default false;

    /**
     * @return True/False of Permissions usage
     */
    boolean Permissions() default false;

    /**
     * @return True/False of Economy usage
     */
    boolean Economy() default false;

    /**
     * @return True/False if this module can be disabled
     */
    boolean NoDisable() default false;
}
