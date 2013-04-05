package com.github.seemethere.DeathEssentials.modules;

import com.github.seemethere.DeathEssentials.DeathEssentialsPlugin;
import com.github.seemethere.DeathEssentials.utils.commands.CMD;
import com.github.seemethere.DeathEssentials.utils.commands.CallInfo;
import com.github.seemethere.DeathEssentials.utils.commonutils.RegionUtil;
import com.github.seemethere.DeathEssentials.utils.configuration.ConfigManager;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@ModuleInfo(name = "DeathCharge",
        version = 0.6,
        description = "Charge a player a configurable amount on death\n" +
                "Ability to mark WorldGuard regions / Minecraft worlds\n" +
                "as excluded from the module.\n" +
                "Also comes with a permissions based bypass: 'deathcharge.bypass'",
        WorldGuard = true,
        Economy = true,
        HasConfig = true)
public class DeathCharge implements ModuleBase, Listener {
    private static boolean status = false;
    private DeathEssentialsPlugin plugin;
    private Logger logger;
    private String MODULE_NAME;
    private String deathMessage;
    private YamlConfiguration exclusions;
    private YamlConfiguration config;
    private Economy economy;
    private Map<String, String> excludedRegions;
    private boolean isPercent = false;
    private double charge;
    private List<String> excludedWorlds;

    public boolean isEnabled() {
        return status;
    }

    public void enableModule(DeathEssentialsPlugin plugin, String name) {
        MODULE_NAME = "[" + name + "] ";
        this.plugin = plugin;
        status = true;
        excludedRegions = new HashMap<String, String>();
        excludedWorlds = new ArrayList<String>();
        logger = plugin.getLogger();
        economy = ModuleDependencies.Economy();
        // Initiate main config
        ConfigManager configManager = plugin.getModuleConfigManager(this);
        config = configManager.getConfig();
        deathMessage = config.getString("deathMessage");
        // Check if what we're dealing with is a percent or not
        String configCharge = "5%";
        if (config.getString("amount") != null) {
            try {
                configCharge = config.getString("amount");
                charge = Double.parseDouble(configCharge.replace("%", ""));
            } catch (NumberFormatException e) {
                logger.severe(MODULE_NAME + "DeathCharge.yml contains an invalid 'amount' value!");
                logger.severe(MODULE_NAME + "Reverting to the default charge of 5%");
            }
        }
        if (configCharge.contains("%"))
            isPercent = true;
        // Get all things associated with extra config
        exclusions = configManager.getModuleConfig("Exclusions.yml");
        if (exclusions.getConfigurationSection("ex_regions") != null) {
            Map<String, Object> temp = exclusions.getConfigurationSection("ex_regions").getValues(false);
            for (Map.Entry<String, Object> entry : temp.entrySet())
                excludedRegions.put(entry.getKey(), (String) entry.getValue());
        }

        // Added multi-world support
        excludedWorlds = exclusions.getStringList("ex_worlds");
    }

    public void disableModule() {
        if (exclusions != null) {
            exclusions.set("ex_regions", excludedRegions);
            exclusions.set("ex_worlds", excludedWorlds);
            plugin.getModuleConfigManager(this).saveModuleConfig(exclusions);
        }
        status = false;
        isPercent = false;
    }

    @CMD(command = "deathcharge",
            aliases = "dc",
            description = "Simple about message",
            AllowConsole = true)
    public void cmd_deathcharge(CallInfo call) {
        call.reply("&e%s&aby seemethere!", MODULE_NAME);
    }

    @CMD.SUB(name = "region",
            parent = "deathcharge",
            max = 0,
            permission = "deathcharge.region",
            description = "Toggles Worldguard regions from plugin")
    public void sub_region(CallInfo call) {
        if (RegionUtil.getRegionAt(call.location()) != null) {
            String id = RegionUtil.getRegionAt(call.location()).getId().toLowerCase();
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

    @CMD.SUB(name = "world",
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
        // Permissions bypass
        if (p.hasPermission("deathcharge.bypass"))
            return;
        //PVP disable
        if (p.getKiller() != null)
            if (!config.getBoolean("pvp"))
                return;
        //Multi-World support
        if (excludedWorlds.contains(p.getWorld().toString()))
            return;
        //Region support
        if (RegionUtil.getRegionAt(p.getLocation()) != null) {
            String r = RegionUtil.getRegionAt(p.getLocation()).getId().toLowerCase();
            if (excludedRegions.containsKey(r) && excludedRegions.get(r).equalsIgnoreCase(p.getWorld().toString()))
                return;
        }
        double lost = -1;
        // Add support for amounts / percentages
        if (isPercent)
            lost = (charge / 100) * economy.getBalance(p.getName());
            // They have enough money for the charge
        else if (economy.getBalance(p.getName()) > charge)
            lost = charge;
            // Drain all the money they have
        else if (config.getBoolean("drain"))
            lost = economy.getBalance(p.getName());
        // Check if we're actually going to be taking anything
        if (lost != -1) {
            String message = ChatColor.YELLOW + MODULE_NAME +
                    deathMessage.replace("{AMOUNT}", String.format("%.2f", lost));
            economy.withdrawPlayer(p.getName(), lost);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }
}
