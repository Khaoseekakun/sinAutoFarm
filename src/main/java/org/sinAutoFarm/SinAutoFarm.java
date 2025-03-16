package org.sinAutoFarm;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.sinAutoFarm.events.onBreakBlockEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SinAutoFarm extends JavaPlugin {

    public static FileConfiguration config;
    public static Map<UUID, Long> cooldowns = new HashMap<>();
    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        config = getConfig();
        getServer().getPluginManager().registerEvents(new onBreakBlockEvent(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
