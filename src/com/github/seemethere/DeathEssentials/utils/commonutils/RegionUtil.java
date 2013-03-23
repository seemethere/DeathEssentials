package com.github.seemethere.DeathEssentials.utils.commonutils;

import com.github.seemethere.DeathEssentials.utils.module.ModuleDependencies;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;


public class RegionUtil {
    public static ProtectedRegion getRegionAt(Location loc) {
        ProtectedRegion highest = null;
        for (ProtectedRegion r : ModuleDependencies.getWorldGuard().getRegionManager(loc.getWorld()).getApplicableRegions(loc))
            if (highest == null || highest.getPriority() < r.getPriority())
                highest = r;
        if (highest == null)
            return null;
        return highest;
    }
}
