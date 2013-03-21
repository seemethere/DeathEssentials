package com.github.seemethere.DeathEssentials.modules;

import com.github.seemethere.DeathEssentials.DeathEssentialsPlugin;
import com.github.seemethere.DeathEssentials.utils.commands.CMD;
import com.github.seemethere.DeathEssentials.utils.commands.CallInfo;
import com.github.seemethere.DeathEssentials.utils.commands.SUB_CMD;
import com.github.seemethere.DeathEssentials.utils.module.ModuleDE;
import com.github.seemethere.DeathEssentials.utils.module.ModuleInfo;
import org.bukkit.ChatColor;

import java.util.Map;

@ModuleInfo(name = "InternalCommands",
        description = "Internal commands to control modules",
        NoDisable = true)
public class InternalCommands implements ModuleDE {
    //Permission node for admin commands
    private static final String ADMIN_PERM = "deathessentials.admin";
    private static boolean status = false;
    private static String PLUGIN_NAME = ChatColor.GRAY + "[DeathEssentials] " + ChatColor.AQUA;
    private DeathEssentialsPlugin plugin;

    public boolean isEnabled() { return status; }

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
    public void cmd_internal(CallInfo info) {
        info.reply("%sv%.1f &7by seemethere", PLUGIN_NAME, plugin.getVersion());
    }

    @SUB_CMD(parent = "deathessentials",
            name = "enable",
            min = 1,
            max = 1,
            description = "Enables modules",
            permission = ADMIN_PERM,
            AllowConsole = true)
    public void sub_enable(CallInfo info) {
        switch (plugin.getModuleManager().plugModule(info.args[1])) {
            case 3:
                info.reply("%s%s&4 had a dependency error! See console for details!", PLUGIN_NAME, info.args[1]);
                break;
            case 2:
                info.reply("%s%s &4not found!", PLUGIN_NAME, info.args[1]);
                break;
            case 1:
                info.reply("%s%s&4 is already enabled!",
                        PLUGIN_NAME, info.args[1]);
                break;
            case 0:
                info.reply("%s%s&a has been enabled!", PLUGIN_NAME, info.args[1]);
                break;
        }
    }

    @SUB_CMD(parent = "deathessentials",
            name = "disable",
            min = 1,
            max = 1,
            description = "Enables modules",
            permission = ADMIN_PERM,
            AllowConsole = true)
    public void sub_disable(CallInfo info) {
        switch (plugin.getModuleManager().unplugModule(info.args[1])) {
            case 3:
                info.reply("%s%s&4 cannot be disabled!", PLUGIN_NAME, info.args[1]);
                break;
            case 2:
                info.reply("%s%s &4not found!", PLUGIN_NAME, info.args[1]);
                break;
            case 1:
                info.reply("%s%s&4 is already disabled!",
                        PLUGIN_NAME, info.args[1]);
                break;
            case 0:
                info.reply("%s%s&a has been disabled!", PLUGIN_NAME, info.args[1]);
                break;
        }
    }

    @SUB_CMD(parent = "deathessentials",
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
            call.reply("&7-=-=- %s%s&7 -=-=-", enabled ? ChatColor.GREEN : ChatColor.RED, moduleinfo.name());
            if (moduleinfo.NoDisable())
                call.reply("&4Cannot be disabled");
            call.reply("&3Description: \n&6%s", moduleinfo.description());
            // Collect all dependencies in a string
            String dependencies = (moduleinfo.Chat() ? "    - Chat \n" : "")
                    + (moduleinfo.Economy() ? "    - Economy \n" : "")
                    + (moduleinfo.Permissions() ? "   - Permissions \n" : "")
                    + (moduleinfo.WorldGuard() ? "    - WorldGuard" : "");
            call.reply("&3Dependencies: \n&6%s", dependencies.equalsIgnoreCase("") ? "None!" : dependencies);
        }
    }

    @SUB_CMD(parent = "deathessentials",
            name = "list",
            max = 1,
            description = "Lists all modules",
            permission = ADMIN_PERM,
            AllowConsole = true)
    public void sub_list(CallInfo info) {
        info.reply("&7-=-=- &6Modules &7-=-=-");
        Map<String, ModuleDE> modules = plugin.getModuleList();
        for (String s : modules.keySet()) {
            info.reply("&7%s: %5s", s,
                    modules.get(s).isEnabled() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled");
        }
    }
}
