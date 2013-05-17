package com.github.seemethere.DeathEssentials;

import com.github.seemethere.DeathEssentials.modules.*;
import com.github.seemethere.DeathEssentials.utils.configuration.ConfigManager;
import com.github.seemethere.DeathEssentials.utils.module.ModuleBase;
import com.github.seemethere.DeathEssentials.utils.module.ModuleDependencies;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.logging.Logger;

/**
 * @author seemethere
 */
public class ModularPlugin extends JavaPlugin {
    private static final double version = 1.0;
    private ModuleManager moduleManager;
    private Logger logger;

    public void onEnable() {
        this.saveDefaultConfig();
        logger = this.getLogger();
        new ModuleDependencies(this);
        moduleManager = new ModuleManager(this);
        setModuleList();
        // Initial plug of modules
        for (String s : moduleManager.getModuleList().keySet()) {
            if (moduleManager.getInitialStatus().containsKey(s) &&
                    moduleManager.getInitialStatus().get(s)) {
                moduleManager.plugModule(s);
            }
        }
        //Enable InternalCommands
        moduleManager.plugModule("InternalCommands");
        logger.info("DeathEssentials has been enabled");
    }

    public void onDisable() {
        // Unplugging of modules for safe exit
        for (String s : moduleManager.getModuleList().keySet()) {
            ModuleBase module = moduleManager.findModule(s);
            if (moduleManager.isEnabled(module)) {
                moduleManager.unplugModule(s, true);
            }
        }
        logger.info("DeathEssentials has been disabled");
    }

    protected void setModuleList() {
        addModule(new InternalCommands());
        addModule(new TestModule());
        addModule(new DeathCharge());
        addModule(new DeathBan());
        addModule(new KDR());
    }

    /**
     * For public use, allows a developer to add a module directly from the ModularPlugin base
     *
     * @param module Module to be added
     */
    public void addModule(ModuleBase module) {
        moduleManager.addModule(module);
    }

    /**
     * @return ModuleManager
     */
    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    /**
     * @return List of modules with classes
     */
    public Map<String, ModuleBase> getModuleList() {
        return moduleManager.getModuleList();
    }

    public double getVersion() {
        return version;
    }

    public ConfigManager getModuleConfigManager(ModuleBase module) {
        return this.getModuleManager().getModuleConfigManager(module);
    }

    public ModuleBase findModule(String name) {
        return this.getModuleManager().findModule(name);
    }
}
