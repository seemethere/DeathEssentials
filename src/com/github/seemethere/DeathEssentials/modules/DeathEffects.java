package com.github.seemethere.DeathEssentials.modules;

import com.github.seemethere.DeathEssentials.ModularPlugin;
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

    public void enableModule(ModularPlugin plugin, String name) {

    }

    public void disableModule() {

    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {

    }
}
