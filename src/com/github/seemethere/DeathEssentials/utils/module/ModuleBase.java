package com.github.seemethere.DeathEssentials.utils.module;

import com.github.seemethere.DeathEssentials.DeathEssentialsPlugin;

public interface ModuleBase {
    /**
     * True = Plugged
     * False = Unplugged
     *
     * @return Status of module.
     */
    boolean isEnabled();

    /**
     * Enables module
     */
    abstract void enableModule(DeathEssentialsPlugin plugin, String name);

    /**
     * Disables module
     */
    abstract void disableModule();
}
