package com.github.seemethere.DeathEssentials.modules;

import com.github.seemethere.DeathEssentials.DeathEssentialsPlugin;
import com.github.seemethere.DeathEssentials.utils.commands.CMD;
import com.github.seemethere.DeathEssentials.utils.commands.CallInfo;
import com.github.seemethere.DeathEssentials.utils.commonutils.TimeUtil;
import com.github.seemethere.DeathEssentials.utils.configuration.ConfigManager;
import com.github.seemethere.DeathEssentials.utils.module.ModuleBase;
import com.github.seemethere.DeathEssentials.utils.module.ModuleInfo;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@ModuleInfo(name = "DeathBan",
        description = "Bans players on death, with configurable effects.\n" +
                "Keeps a record of its own bans and is not affiliated with any\n" +
                "other banning system!\n" +
                "Comes with a permissions based bypass: deathban.bypass",
        version = 0.6,
        HasConfig = true)
public class DeathBan implements ModuleBase, Listener {
    private static boolean status = false;
    private DeathEssentialsPlugin plugin;
    private YamlConfiguration config = null;
    private YamlConfiguration banned_config;
    private String MODULE_NAME;
    private Map<String, Long> bannedPlayers;
    private Long banTime = 0L;
    private String kickMessage;
    private String broadcastMessage;

    public boolean isEnabled() {
        return status;
    }

    public void enableModule(DeathEssentialsPlugin plugin, String name) {
        status = true;
        this.plugin = plugin;
        MODULE_NAME = "[" + name + "] ";
        bannedPlayers = new HashMap<String, Long>();
        ConfigManager configManager = plugin.getModuleConfigManager(this);
        config = configManager.getConfig();
        banned_config = configManager.getModuleConfig("BannedPlayers.yml");
        if (banned_config.getConfigurationSection("bannedPlayers") != null) {
            Map<String, Object> temp = banned_config.getConfigurationSection("bannedPlayers").getValues(false);
            for (Map.Entry<String, Object> entry : temp.entrySet())
                bannedPlayers.put(entry.getKey(), (Long) entry.getValue());
        }
        kickMessage = config.getString("KickMessage");
        broadcastMessage = config.getString("BroadcastMessage");
    }

    public void disableModule() {
        if (banned_config != null) {
            banned_config.set("BannedPlayers", bannedPlayers);
            plugin.getModuleConfigManager(this).saveModuleConfig(banned_config);
        }
        status = false;
    }

    @CMD(command = "deathban",
            aliases = "db",
            description = "About command for DeathBan")
    public void cmd_deathban(CallInfo call) {
        call.reply("&e%s&b by seemethere", MODULE_NAME);
    }

    @CMD.SUB(parent = "deathban",
            name = "unban",
            description = "Unbans a player that has been banned by DeathBan",
            permission = "deathban.unban",
            min = 1,
            max = 1,
            AllowConsole = true)
    public void sub_unban(CallInfo call) {
        for (String s : bannedPlayers.keySet()) {
            if (call.args[1].equalsIgnoreCase(s)) {
                bannedPlayers.remove(s);
                call.reply("&e%s&b%s&e has been unbanned!", MODULE_NAME, call.args[1]);
                return;
            }
        }
        call.reply("&e%s&cPlayer not found", MODULE_NAME);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // Parse time only when event is fired for the first time to avoid an NPE
        if (banTime == 0L) {
            String time = config.getString("BanTime");
            banTime = TimeUtil.parseTime(time);
        }
        Player p = event.getEntity();
        // Deaths attributed to non-pvp
        if (p.getKiller() == null && config.getBoolean("ExcludeNonPVPDeaths"))
            return;
        // Players who can bypass the plugin
        if (p.hasPermission("deathban.bypass"))
            return;
        bannedPlayers.put(p.getName(), System.currentTimeMillis());
        // Check to see if they actually want to broadcast the message
        if (config.getBoolean("BroadcastDeath")) {
            String broadcast_msg = ChatColor.translateAlternateColorCodes('&', "&4[DeathBan]&e " + broadcastMessage.
                    replace("{PLAYER}", p.getName()).
                    replace("{TIME}", TimeUtil.timeToString(banTime)));
            plugin.getServer().broadcastMessage(broadcast_msg);
        }
        plugin.getLogger().info(MODULE_NAME + "Player '" + p.getName() + "' has been banned for "
                + TimeUtil.timeToString(banTime) + "!");
        String message = String.format("&4%s&e\n%s", MODULE_NAME, kickMessage);
        message = message.replace("{TIME}", TimeUtil.timeToString(banTime));
        p.kickPlayer(ChatColor.translateAlternateColorCodes('&', message));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player p = event.getPlayer();
        if (bannedPlayers.containsKey(p.getName())) {
            Long millisElapsed = System.currentTimeMillis() - bannedPlayers.get(p.getName());
            if (millisElapsed < banTime) {
                // Display amount of time left on ban
                String message = String.format("&4%s&e\n%s", MODULE_NAME, kickMessage);
                message = message.replace("{TIME}", TimeUtil.timeToString(banTime - millisElapsed));
                event.disallow(PlayerLoginEvent.Result.KICK_BANNED,
                        ChatColor.translateAlternateColorCodes('&', message));
            } else {
                // Unban player from module
                plugin.getLogger().info(MODULE_NAME + "Player '" + p.getName() + "' has been unbanned");
                bannedPlayers.remove(p.getName());
            }
        }
    }
}
