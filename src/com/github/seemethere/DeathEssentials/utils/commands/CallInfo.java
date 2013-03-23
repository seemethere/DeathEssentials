package com.github.seemethere.DeathEssentials.utils.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CallInfo {
    public Player player = null;
    public CommandSender sender;
    public String[] args;

    public CallInfo(CommandSender sender, String[] args) {
        this.sender = sender;
        this.args = args;
        if (sender instanceof Player)
            player = (Player) sender;
    }

    public void reply(String msg, Object... args) {
        sender.sendMessage(String.format(ChatColor.translateAlternateColorCodes('&', msg), args));
    }

    public Location location() {
        return player.getLocation();
    }

    public World world() {
        return player.getWorld();
    }

    public String name() {
        return player.getName();
    }
}
