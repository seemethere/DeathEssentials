package com.github.seemethere.DeathEssentials.modules;

import com.github.seemethere.DeathEssentials.DeathEssentialsPlugin;
import com.github.seemethere.DeathEssentials.utils.commonutils.CustomConfig;
import com.github.seemethere.DeathEssentials.utils.commonutils.TimeUtil;
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
                "Keeps a record of it's own bans and is not affiliated with any\n" +
                "other banning system",
        version = 0.5)
public class DeathBan implements ModuleBase, Listener {
    private static boolean status = false;
    private DeathEssentialsPlugin plugin;
    private YamlConfiguration config;
    private File banned_file;
    private YamlConfiguration banned_config;
    private String MODULE_NAME;
    private Map<String, Long> bannedPlayers;
    private Long banTime;
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
        CustomConfig customConfig = new CustomConfig(plugin, "DeathBan.yml", "/DeathBan", name);
        config = customConfig.getConfig();
        banned_file = new File(customConfig.getModuleFolder(), "BannedPlayers.yml");
        try {
            banned_config = YamlConfiguration.loadConfiguration(banned_file);
        } catch (Throwable t) {
            plugin.getLogger().info(MODULE_NAME + "Unable to load BannedPlayers.yml! Unplugging!");
            plugin.getModuleManager().unplugModule(name);
            return;
        }
        if (banned_config.getConfigurationSection("bannedPlayers") != null) {
            Map<String, Object> temp = banned_config.getConfigurationSection("bannedPlayers").getValues(false);
            for (Map.Entry<String, Object> entry : temp.entrySet())
                bannedPlayers.put(entry.getKey(), (Long) entry.getValue());
        }
        kickMessage = config.getString("KickMessage");
        broadcastMessage = config.getString("BroadcastMessage");
        banTime = TimeUtil.ParseTime(config.getString("BanTime"));
    }

    @Override
    public void disableModule() {
        save();
        status = false;
    }

    private void save() {
        banned_config.set("BannedPlayers", bannedPlayers);
        try {
            banned_config.save(banned_file);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error saving BannedPlayer.yml");
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player p = event.getEntity();
        // Deaths attributed to non-pvp
        if (p.getKiller() == null && config.getBoolean("ExcludedNonPVPDeaths"))
            return;
        // Players who can bypass the plugin
        if (p.hasPermission("deathban.bypass"))
            return;
        bannedPlayers.put(p.getName(), System.currentTimeMillis());
        // Check to see if they actually want to broadcast the message
        if (config.getBoolean("BroadcastDeath")) {
            String broadcastmsg = ChatColor.translateAlternateColorCodes('&', "&4[DeathBan]&e" + broadcastMessage.
                    replace("{PLAYER}", p.getName()).
                    replace("{KILLER}", p.getKiller().getName()).
                    replace("{TIME}", TimeUtil.StringTime(banTime)));
            plugin.getServer().broadcastMessage(broadcastmsg);
        }
        plugin.getLogger().info(MODULE_NAME + "Player '" + p.getName() + "' has been banned for "
                + TimeUtil.StringTime(banTime) + "!");
        String kickmsg = "&4[DeathBan]&e" + config.getString("KickMessage");
        kickmsg = kickmsg.replace("{TIME}", TimeUtil.StringTime(banTime));
        p.kickPlayer(ChatColor.translateAlternateColorCodes('&', kickmsg));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player p = event.getPlayer();
        if (bannedPlayers.containsKey(p.getName())) {
            Long millisLeft = System.currentTimeMillis() - bannedPlayers.get(p.getName());
            if (millisLeft < banTime) {
                String message = "&4[DeathBan]&e" + config.getString("KickMessage");
                message = message.replace("{TIME}", TimeUtil.StringTime(millisLeft));
                p.kickPlayer(ChatColor.translateAlternateColorCodes('&', message));
            } else {
                bannedPlayers.remove(p.getName());
            }
        }
    }
}
