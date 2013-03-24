package com.github.seemethere.DeathEssentials.modules;

import com.github.seemethere.DeathEssentials.DeathEssentialsPlugin;
import com.github.seemethere.DeathEssentials.utils.module.ModuleBase;
import com.github.seemethere.DeathEssentials.utils.module.ModuleInfo;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

@ModuleInfo(name = "DeathEffects",
            description = "Displays effects on player death",
            version = 0.1)
public class DeathEffects implements ModuleBase, Listener {
    private static boolean status = false;

    public boolean isEnabled() {
        return status;
    }

    public void enableModule(DeathEssentialsPlugin plugin, String name) {
        status = true;
    }

    public void disableModule() {
        status = false;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {

    }
}
