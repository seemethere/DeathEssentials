package com.github.seemethere.DeathEssentials.utils.module;

import com.github.seemethere.DeathEssentials.ModularPlugin;

public interface ModuleBase {

    /**
     * Enables module
     */
    abstract void enableModule(ModularPlugin plugin, String name);

    /**
     * Disables module
     */
    abstract void disableModule();
}
