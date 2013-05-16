package com.github.seemethere.DeathEssentials;

import com.github.seemethere.DeathEssentials.utils.ModuleManager;
import com.github.seemethere.DeathEssentials.utils.configuration.ConfigManager;
import com.github.seemethere.DeathEssentials.utils.module.ModuleBase;
import com.github.seemethere.DeathEssentials.utils.module.ModuleDependencies;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.logging.Logger;

/**
 * @author seemethere
 */
public class DeathEssentialsPlugin extends JavaPlugin {
    private static final double version = 1.0;
    private ModuleManager moduleManager;
    private ModuleDependencies dependencies;
    private Logger logger;

    public void onEnable() {
        this.saveDefaultConfig();
        logger = this.getLogger();
        dependencies = new ModuleDependencies(this);
        moduleManager = new ModuleManager(this);
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
        for (String s : moduleManager.getModuleList().keySet())
            if (moduleManager.getModuleList().get(s).isEnabled())
                moduleManager.unplugModule(s, true);
        logger.info("DeathEssentials has been disabled");
    }

    /**
     * @return ModuleManager
     */
    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    /**
     * @return Dependencies
     */
    public ModuleDependencies getDependencies() {
        return dependencies;
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
