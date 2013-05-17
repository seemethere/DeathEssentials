package com.github.seemethere.DeathEssentials.modules;

import com.github.seemethere.DeathEssentials.ModularPlugin;
import com.github.seemethere.DeathEssentials.utils.commands.CMD;
import com.github.seemethere.DeathEssentials.utils.commands.CallInfo;
import com.github.seemethere.DeathEssentials.utils.commonutils.ModuleUtil;
import com.github.seemethere.DeathEssentials.utils.configuration.ConfigManager;
import com.github.seemethere.DeathEssentials.utils.module.ModuleBase;
import com.github.seemethere.DeathEssentials.utils.module.ModuleInfo;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.Map;

@ModuleInfo(name = "KDR",
        NoDisable = true,
        description = "Counts the number of kills and deaths of every player",
        HasConfig = true)
public class KDR implements ModuleBase, Listener {
    private ConfigManager manager;
    private YamlConfiguration config;
    private YamlConfiguration storage;

    public void enableModule(ModularPlugin plugin, String name) {
        manager = plugin.getModuleConfigManager(this);
        config = manager.getConfig();
        storage = manager.getModuleConfig("KDR_Storage.yml");
    }

    public void disableModule() {
        manager.saveModuleConfig(storage);
    }

    @CMD(command = "kdr",
            description = "Allows players to see their kill to death ratio",
            aliases = "killdeath",
            AllowConsole = false,
            permission = "kdr.see")
    public void cmd_kdr(CallInfo call) {
        int killcnt, deathcnt;
        if (storage.getConfigurationSection(call.name()) == null) {
            deathcnt = 0;
            killcnt = 0;
        } else {
            deathcnt = storage.getInt(call.name() + ".deaths");
            killcnt = storage.getInt(call.name() + ".kills");
        }

        double kdr = (deathcnt != 0) ? (double) killcnt / (double) deathcnt : killcnt;
        call.reply("&6}----{&cKDR&6}----{\n" +
                "&c    KDR: &6%.2f\n" +
                "&c    KILLS: &6%d\n" +
                "&c    DEATHS: &6%d\n" +
                "&6}----{&cKDR&6}----{", kdr, killcnt, deathcnt);
    }

    @CMD.SUB(name = "leaderboard",
            parent = "KDR",
            max = 1,
            permission = "kdr.leaderboard",
            AllowConsole = true)
    public void sub_leaderboard(CallInfo call) {
        String[] args = call.args;
        String delim;
        String value;
        Map<String, Object> keys = storage.getValues(true);
        Map<String, Double> iter;
        int i;
        if (args.length < 2) {
            delim = "KDR: ";
            value = ".kdr";
        } else if (args[1].equalsIgnoreCase("kdr")) {
            delim = "KDR: ";
            value = ".kdr";
        } else if (args[1].equalsIgnoreCase("deaths")) {
            value = ".deaths";
            delim = "DEATHS: ";
        } else if (args[1].equalsIgnoreCase("kills")) {
            value = ".kills";
            delim = "KILLS: ";
        } else {
            return;
        }
        iter = createMap(keys, value);
        iter = ModuleUtil.sortByValues(iter);
        i = 1;
        for (String s : iter.keySet()) {
            call.reply("%d. %s %s %.2f", i++, s, delim, iter.get(s));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() != null) //If a player kills them
            add(event.getEntity().getKiller().getName(), ".kills");
        else if (event.getEntity().getKiller() == null && !config.getBoolean("CountNonPlayerDeaths"))
            return;
        add(event.getEntity().getName(), ".deaths");
    }

    private void add(String player, String delim) {
        if (storage.getConfigurationSection(player) == null) {
            storage.set(player + ".kills", 0);
            storage.set(player + ".deaths", 0);
        }
        storage.set(player + delim, storage.getInt(player + delim) + 1);
        // Try to avoid any math errors IE: Dividing by zero
        storage.set(player + ".kdr", storage.getInt(player + ".kills") /
                (storage.getInt(player + ".deaths") != 0 ? storage.getInt(player + ".deaths") : 1));
    }

    private Map<String, Double> createMap(Map<String, Object> values, String end) {
        Map<String, Double> map = new HashMap<String, Double>();
        for (String s : values.keySet())
            if (s.endsWith(end))
                map.put(s.replace(end, ""), storage.getDouble(s));
        return map;
    }
}
