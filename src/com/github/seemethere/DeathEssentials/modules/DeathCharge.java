package com.github.seemethere.DeathEssentials.modules;

import com.github.seemethere.DeathEssentials.DeathEssentialsPlugin;
import com.github.seemethere.DeathEssentials.utils.commands.CMD;
import com.github.seemethere.DeathEssentials.utils.commands.CallInfo;
import com.github.seemethere.DeathEssentials.utils.commands.SUB_CMD;
import com.github.seemethere.DeathEssentials.utils.commonutils.CustomConfig;
import com.github.seemethere.DeathEssentials.utils.commonutils.RegionUtil;
import com.github.seemethere.DeathEssentials.utils.module.ModuleBase;
import com.github.seemethere.DeathEssentials.utils.module.ModuleDependencies;
import com.github.seemethere.DeathEssentials.utils.module.ModuleInfo;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@ModuleInfo(name = "DeathCharge",
        version = 0.5,
        description = "Charge a player a configurable amount on death\n" +
                "",
        WorldGuard = true,
        Economy = true)
public class DeathCharge implements ModuleBase, Listener {
    private static boolean status = false;
    private Logger logger;
    private String MODULE_NAME;
    private File exclusions_file;
    private YamlConfiguration exclusions;
    private YamlConfiguration config;
    private Economy economy;
    private Map<String, String> excludedRegions;
    private List<String> excludedWorlds;

    public boolean isEnabled() { return status; }

    public void enableModule(DeathEssentialsPlugin plugin, String name) {
        MODULE_NAME = "[" + name + "] ";
        status = true;
        excludedRegions = new HashMap<String, String>();
        excludedWorlds = new ArrayList<String>();
        logger = plugin.getLogger();
        economy = ModuleDependencies.getEconomy();
        // Initiate main config
        CustomConfig customConfig = new CustomConfig(plugin, "DeathCharge.yml", "/DeathCharge", name);
        config = customConfig.getConfig();

        // Get all things associated with extra config
        exclusions_file = new File(customConfig.getModuleFolder(), "Exclusions.yml");
        try {
            exclusions = YamlConfiguration.loadConfiguration(exclusions_file);
        } catch (Throwable t) {
            logger.severe(MODULE_NAME + "Unable to load Exclusions.yml! Exiting...");
            return;
        }

        if (exclusions.getConfigurationSection("ex_regions") != null) {
            Map<String, Object> temp = exclusions.getConfigurationSection("ex_regions").getValues(false);
            for (Map.Entry<String, Object> entry : temp.entrySet())
                excludedRegions.put(entry.getKey(), (String) entry.getValue());
        }

        // Added multi-world support
        excludedWorlds = exclusions.getStringList("ex_worlds");
    }

    public void disableModule() {
        save();
        status = false;
    }

    private void save() {
        exclusions.set("ex_regions", excludedRegions);
        exclusions.set("ex_worlds", excludedWorlds);
        try {
            exclusions.save(exclusions_file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @CMD(command = "deathcharge",
            aliases = "dc",
            description = "Simple about message",
            AllowConsole = true)
    public void cmd_deathcharge(CallInfo call) {
        call.reply("&e%s&aby seemethere!", MODULE_NAME);
    }

    @SUB_CMD(name = "region",
            parent = "deathcharge",
            max = 0,
            permission = "deathcharge.region",
            description = "Toggles Worldguard regions from plugin")
    public void sub_region(CallInfo call) {
        if (RegionUtil.findRegion(call.location()) != null) {
            String id = RegionUtil.findRegion(call.location()).getId().toLowerCase();
            for (String s : excludedRegions.keySet()) {
                if (id.equalsIgnoreCase(s) && excludedRegions.get(s).equalsIgnoreCase(call.world().toString())) {
                    excludedRegions.remove(s);
                    call.reply("&e%s&cRemoved &e%s&c from excluded regions", MODULE_NAME, s);
                    logger.info(MODULE_NAME + "Player '" + call.name() +
                            "' removed region '" + s + "' from excluded regions");
                    return;
                }
            }
            excludedRegions.put(id.toLowerCase(), call.player.getWorld().toString().toLowerCase());
            call.reply("&e%s&aAdded &e%s&a to excluded regions", MODULE_NAME, id);
            logger.info(MODULE_NAME + "Player '" + call.name() + "' added region '" + id + "' to excluded regions");
        } else
            call.reply("&cERROR: &eNo region found!");
    }

    @SUB_CMD(name = "world",
            parent = "deathcharge",
            max = 0,
            permission = "deathcharge.world",
            description = "Toggles whole worlds from the plugin")
    public void sub_world(CallInfo call) {
        if (excludedWorlds.contains(call.world().toString())) {
            excludedWorlds.remove(call.world().toString());
            call.reply("&e%s&cRemoved &e%s&c from excluded worlds", MODULE_NAME, call.world().toString());
            logger.info(MODULE_NAME + "Player '" + call.name() + "' removed world '"
                    + call.world() + "' from excluded worlds");
            return;
        }
        excludedWorlds.add(call.world().toString());
        call.reply("&e%s&aAdded &e%s&a to excluded worlds", MODULE_NAME, call.world().toString());
        logger.info(MODULE_NAME + "Player '" + call.name() + "' added world '" + call.world() + "' to excluded worlds");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player p = event.getEntity();
        //PVP disable
        if (p.getKiller() != null)
            if (!config.getBoolean("pvp"))
                return;
        //Multi-World support
        if (excludedWorlds.contains(p.getWorld().toString()))
            return;
        //Region support
        if (RegionUtil.findRegion(p.getLocation()) != null) {
            String r = RegionUtil.findRegion(p.getLocation()).getId().toLowerCase();
            if (excludedRegions.containsKey(r) && excludedRegions.get(r).equalsIgnoreCase(p.getWorld().toString()))
                return;
        }
        double lost = (config.getDouble("percent") / 100) * economy.getBalance(p.getName());
        economy.withdrawPlayer(p.getName(), lost);
        p.sendMessage(String.format("%sYou have lost %s$%.2f%s on death!",
                ChatColor.YELLOW, ChatColor.RED, lost, ChatColor.YELLOW));
    }
}
