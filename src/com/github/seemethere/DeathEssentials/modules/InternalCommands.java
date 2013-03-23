package com.github.seemethere.DeathEssentials.modules;

import com.github.seemethere.DeathEssentials.DeathEssentialsPlugin;
import com.github.seemethere.DeathEssentials.utils.commands.CMD;
import com.github.seemethere.DeathEssentials.utils.commands.CallInfo;
import com.github.seemethere.DeathEssentials.utils.module.ModuleBase;
import com.github.seemethere.DeathEssentials.utils.module.ModuleInfo;
import org.bukkit.ChatColor;

import java.util.Map;

@ModuleInfo(name = "InternalCommands",
        description = "Internal commands to control modules",
        NoDisable = true)
public class InternalCommands implements ModuleBase {
    //Permission node for admin commands
    private static final String ADMIN_PERM = "deathessentials.admin";
    private static final String PLUGIN_NAME = ChatColor.GRAY + "[DeathEssentials] " + ChatColor.AQUA;
    private static boolean status = false;
    private DeathEssentialsPlugin plugin;

    public boolean isEnabled() {
        return status;
    }

    public void enableModule(DeathEssentialsPlugin plugin, String name) {
        status = true;
        this.plugin = plugin;
    }

    public void disableModule() {
        plugin = null;
        status = false;
    }

    @CMD(command = "deathessentials",
            aliases = "de, death, module",
            description = "Gives version and author",
            AllowConsole = true)
    public void cmd_internal(CallInfo call) {
        call.reply("%sv%.1f &7by seemethere", PLUGIN_NAME, plugin.getVersion());
    }

    @CMD.SUB(parent = "deathessentials",
            name = "enable",
            min = 1,
            max = 1,
            description = "Enables modules",
            permission = ADMIN_PERM,
            AllowConsole = true)
    public void sub_enable(CallInfo call) {
        switch (plugin.getModuleManager().plugModule(call.args[1])) {
            case 3:
                call.reply("%s%s&4 had a dependency error! See console for details!", PLUGIN_NAME, call.args[1]);
                break;
            case 2:
                call.reply("%s%s &4not found!", PLUGIN_NAME, call.args[1]);
                break;
            case 1:
                call.reply("%s%s&4 is already enabled!",
                        PLUGIN_NAME, call.args[1]);
                break;
            case 0:
                call.reply("%s%s&a has been enabled!", PLUGIN_NAME, call.args[1]);
                break;
        }
    }

    @CMD.SUB(parent = "deathessentials",
            name = "disable",
            min = 1,
            max = 1,
            description = "Enables modules",
            permission = ADMIN_PERM,
            AllowConsole = true)
    public void sub_disable(CallInfo call) {
        switch (plugin.getModuleManager().unplugModule(call.args[1])) {
            case 3:
                call.reply("%s%s&4 cannot be disabled!", PLUGIN_NAME, call.args[1]);
                break;
            case 2:
                call.reply("%s%s &4not found!", PLUGIN_NAME, call.args[1]);
                break;
            case 1:
                call.reply("%s%s&4 is already disabled!",
                        PLUGIN_NAME, call.args[1]);
                break;
            case 0:
                call.reply("%s%s&a has been disabled!", PLUGIN_NAME, call.args[1]);
                break;
        }
    }

    @CMD.SUB(parent = "deathessentials",
            name = "reload",
            min = 1,
            max = 1,
            description = "Reloads a module",
            permission = ADMIN_PERM,
            AllowConsole = true)
    public void sub_reload(CallInfo call) {
        if (plugin.getModuleManager().findModule(call.args[1]) != null) {
            if (plugin.getModuleManager().findModule(call.args[1]).isEnabled()) {
                plugin.getModuleManager().unplugModule(call.args[1]);
                plugin.getModuleManager().plugModule(call.args[1]);
                call.reply("%s%s &ahas been reloaded!", PLUGIN_NAME, call.args[1]);
            } else
                call.reply("%s%s was not enabled and could not be reloaded!", PLUGIN_NAME, call.args[1]);
        } else
            call.reply("%s%s &4not found!", PLUGIN_NAME, call.args[1]);
    }

    @CMD.SUB(parent = "deathessentials",
            name = "info",
            min = 1,
            max = 1,
            description = "Disables modules",
            permission = ADMIN_PERM,
            AllowConsole = true)
    public void sub_info(CallInfo call) {
        if (plugin.getModuleManager().findModule(call.args[1]) != null) {
            ModuleInfo moduleinfo = plugin.getModuleManager().getModuleInfo(call.args[1]);
            boolean enabled = plugin.getModuleManager().findModule(call.args[1]).isEnabled();
            call.reply("    &7-=-=- %s%s&7 -=-=-", enabled ? ChatColor.GREEN : ChatColor.RED, moduleinfo.name());
            if (moduleinfo.NoDisable())
                call.reply("&4Cannot be disabled");
            call.reply("&3Description: \n&6%s", moduleinfo.description());
            // Collect all dependencies in a string
            String dependencies = (moduleinfo.Chat() ? "    - Chat \n" : "")
                    + (moduleinfo.Economy() ? "    - Economy \n" : "")
                    + (moduleinfo.Permissions() ? "   - Permissions \n" : "")
                    + (moduleinfo.WorldGuard() ? "    - WorldGuard" : "");
            call.reply("&3Dependencies: \n&6%s", dependencies.equalsIgnoreCase("") ? "    -None!" : dependencies);
        } else
            call.reply("%s%s &4not found!", PLUGIN_NAME, call.args[1]);
    }

    @CMD.SUB(parent = "deathessentials",
            name = "list",
            max = 1,
            description = "Lists all modules",
            permission = ADMIN_PERM,
            AllowConsole = true)
    public void sub_list(CallInfo call) {
        call.reply("    &7-=-=- &6Modules &7-=-=-");
        Map<String, ModuleBase> modules = plugin.getModuleList();
        for (String s : modules.keySet()) {
            call.reply("&7%s: %5s", s,
                    modules.get(s).isEnabled() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled");
        }
    }
}
