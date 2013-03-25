package com.github.seemethere.DeathEssentials.utils.module;

import com.github.seemethere.DeathEssentials.DeathEssentialsPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.logging.Logger;

public final class ModuleDependencies {
    private static DeathEssentialsPlugin plugin;
    private static Logger logger;
    private static RegisteredServiceProvider<Economy> economy = null;
    private static RegisteredServiceProvider<Permission> permission = null;
    private static RegisteredServiceProvider<Chat> chat = null;


    public ModuleDependencies(DeathEssentialsPlugin plugin) {
        ModuleDependencies.plugin = plugin;
        logger = plugin.getLogger();
        economy = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        permission = plugin.getServer().getServicesManager().getRegistration(Permission.class);
        chat = plugin.getServer().getServicesManager().getRegistration(Chat.class);
    }

    /**
     * For ModuleManager use, checks dependencies to see if they're all there
     *
     * @param info ModuleInfo
     * @return Check for Dependencies
     */
    public static boolean error(ModuleInfo info) {
        String MODULE_NAME = "[" + info.name() + "] " + info.name() + " ";
        if (info.Economy())
            if (Economy() == null) {
                logger.severe(MODULE_NAME + "could not find an Economy interface. Unplugging module!");
                return true;
            }
        if (info.Chat())
            if (Chat() == null) {
                logger.severe(MODULE_NAME + "could not find a Chat interface. Unplugging module!");
                return true;
            }
        if (info.Permissions())
            if (Permission() == null) {
                logger.severe(MODULE_NAME + "could not find a Permissions interface. Unplugging module!");
                return true;
            }
        if (info.WorldGuard())
            if (WorldGuard() == null) {
                logger.severe(MODULE_NAME + "could not find WorldGuard interface. Unplugging module!");
                return true;
            }
        return false;
    }

    /**
     * For module use, returns null if no economy
     *
     * @return Economy plugin from Vault
     */
    public static Economy Economy() {
        if (economy != null)
            return economy.getProvider();
        return null;
    }

    /**
     * For module use, returns null if no permissions
     *
     * @return Permission plugin from Vault
     */
    public static Permission Permission() {
        if (permission != null)
            return permission.getProvider();
        return null;
    }

    /**
     * For module use, returns null if no chat
     *
     * @return Chat plugin from Vault
     */
    public static Chat Chat() {
        if (chat != null)
            return chat.getProvider();
        return null;
    }

    /**
     * For Module use, returns null if no WorldGuard
     *
     * @return WorldGuardPlugin
     */
    public static WorldGuardPlugin WorldGuard() {
        //Set up WorldGuard
        if (plugin.getServer().getPluginManager().getPlugin("WorldGuard") instanceof WorldGuardPlugin) {
            return (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        }
        return null;
    }
}
