package com.github.seemethere.DeathEssentials.modules;

import com.github.seemethere.DeathEssentials.DeathEssentialsPlugin;
import com.github.seemethere.DeathEssentials.utils.commands.CMD;
import com.github.seemethere.DeathEssentials.utils.commands.CallInfo;
import com.github.seemethere.DeathEssentials.utils.configuration.ConfigManager;
import com.github.seemethere.DeathEssentials.utils.module.ModuleBase;
import com.github.seemethere.DeathEssentials.utils.module.ModuleInfo;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.logging.Logger;

/**
 * @author seemethere
 */
@ModuleInfo(name = "TestModule",
        description = "Module used to test everything",
        WorldGuard = true,
        Economy = true,
        HasConfig = true,
        version = 2.0)
public class TestModule implements ModuleBase, Listener {
    private static boolean status = false;
    private static ConfigManager configAcc;
    private static FileConfiguration config;
    private static String MODULE_NAME;
    private static Logger logger;

    public boolean isEnabled() {
        return status;
    }

    public void enableModule(DeathEssentialsPlugin plugin, String name) {
        status = true;
        logger = plugin.getLogger();
        configAcc = plugin.getModuleConfigManager(this);
        config = configAcc.getConfig();
        MODULE_NAME = "[" + name + "] ";
    }

    public void disableModule() {
        configAcc = null;
        MODULE_NAME = null;
        config = null;
        logger = null;
        status = false;
    }

    @CMD(command = "testing",
            description = "Simple test command",
            AllowConsole = true,
            aliases = "debugger, commandtest")
    public void cmd_test(CallInfo call) {
        call.reply("&e%s&7testing", MODULE_NAME);
    }

    @CMD.SUB(parent = "testing",
            name = "debug",
            AllowConsole = true)
    public void sub_debug(CallInfo call) {
        call.reply("&e%s&fdebugging", MODULE_NAME);
    }

    @CMD.SUB(parent = "testing",
            name = "config",
            AllowConsole = true)
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        event.getPlayer().sendMessage("YOU'VE BROKEN A BLOCK!");
    }
}
