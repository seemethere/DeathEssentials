package com.github.seemethere.DeathEssentials.utils;


import com.github.seemethere.DeathEssentials.DeathEssentialsPlugin;
import com.github.seemethere.DeathEssentials.modules.DeathBan;
import com.github.seemethere.DeathEssentials.modules.DeathCharge;
import com.github.seemethere.DeathEssentials.modules.InternalCommands;
import com.github.seemethere.DeathEssentials.modules.TestModule;
import com.github.seemethere.DeathEssentials.utils.module.ModuleBase;
import com.github.seemethere.DeathEssentials.utils.module.ModuleDependencies;
import com.github.seemethere.DeathEssentials.utils.module.ModuleInfo;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class ModuleManager {
    private DeathEssentialsPlugin plugin;
    private CommandManager commandManager;
    private Map<String, ModuleBase> moduleList;
    private Map<String, Boolean> InitialStatuses;
    private Logger logger;

    public ModuleManager(DeathEssentialsPlugin plugin) {
        this.plugin = plugin;
        logger = plugin.getLogger();
        commandManager = new CommandManager(plugin);
        moduleList = new HashMap<String, ModuleBase>();
        InitialStatuses = new HashMap<String, Boolean>();
        setModuleList();
        setInitialStatus();
    }

    // Since I couldn't do this via reflection it'll have to be done by hand ;n;
    private void setModuleList() {
        addModule(new InternalCommands());
        addModule(new TestModule());
        addModule(new DeathCharge());
        addModule(new DeathBan());
    }

    private void addModule(ModuleBase module) {
        if (module.getClass().isAnnotationPresent(ModuleInfo.class))
            moduleList.put(module.getClass().getAnnotation(ModuleInfo.class).name(), module);
        else
            logger.severe("Class " + module.getClass().toString() + " does not contain a ModuleInfo annotation!");
    }

    private void setInitialStatus() {
        if (plugin.getConfig().getConfigurationSection("modules") != null) {
            Map<String, Object> temp = plugin.getConfig().getConfigurationSection("modules").getValues(false);
            for (Map.Entry<String, Object> entry : temp.entrySet()) {
                if (entry.getValue() instanceof Boolean && findModule(entry.getKey()) != null)
                    InitialStatuses.put(entry.getKey(), (Boolean) entry.getValue());
                else
                    logger.severe("Invalid config.yml! '" + entry.getKey() +
                            "' does not have a correct value or is an invalid module name!");
            }
        }
    }

    /**
     * @param name Name of module to find
     * @return Module itself or null if none found
     */
    public ModuleBase findModule(String name) {
        for (String s : moduleList.keySet())
            if (s.equalsIgnoreCase(name))
                return moduleList.get(s);
        return null;
    }

    /**
     * @param name Name of module to find info on
     * @return ModuleInfo itself or null if none found
     */
    public ModuleInfo getModuleInfo(String name) {
        if (findModule(name) != null)
            return findModule(name).getClass().getAnnotation(ModuleInfo.class);
        return null;
    }

    /**
     * @return The list of modules
     */
    public Map<String, ModuleBase> getModuleList() {
        return moduleList;
    }

    /**
     * @return The initial status of modules
     */
    public Map<String, Boolean> getInitialStatus() {
        return InitialStatuses;
    }

    /**
     * Plugs a module into the plugin. Different numbers indicate different things
     * <p>
     * 3 = Dependency error
     * 2 = Module not found
     * 1 = Already Enabled
     * 0 = Success
     * </p>
     *
     * @param name Module name
     * @return Int corresponding to success/failure of plug
     */
    public int plugModule(String name) {
        if (findModule(name) != null) {
            ModuleBase module = findModule(name);
            if (module.isEnabled())
                return 1;
            ModuleInfo info = getModuleInfo(name);
            //Check if any dependencies are bad
            if (ModuleDependencies.dependencyError(info))
                return 3;
            //Register any events
            if (module instanceof Listener)
                plugin.getServer().getPluginManager().registerEvents((Listener) module, plugin);
            // Register commands
            try {
                commandManager.register(module.getClass(), module);
            } catch (Exception e) {
                e.printStackTrace();
            }
            module.enableModule(plugin, info.name());
            logger.info("[" + info.name() + "] " + info.name() + " has been enabled!");
            return 0;
        }
        return 2;
    }

    /**
     * Unplugs a module from the plugin. Different numbers indicate different things
     * <p>
     * 3 = Module cannot be disabled
     * 2 = Module not found
     * 1 = Already disabled
     * 0 = Success
     * </p>
     *
     * @param name Module name
     * @return int corresponding to success/failure of plug
     */
    public int unplugModule(String name, boolean last) {
        if (findModule(name) != null) {
            ModuleBase module = findModule(name);
            if (!module.isEnabled())
                return 1;
            ModuleInfo info = getModuleInfo(name);
            if (info.NoDisable() && !last)
                return 3;
            // Unregister events, if any
            if (module instanceof Listener)
                plugin.unregisterEvents((Listener) module);
            // Unregister commands for class
            commandManager.unregister(module.getClass());
            module.disableModule();
            logger.info("[" + info.name() + "] " + info.name() + " has been disabled!");
            return 0;
        }
        return 2;
    }

    //Wrapper makes sure this isn't the last unplug
    public int unplugModule(String name) {
        return this.unplugModule(name, false);
    }
}
