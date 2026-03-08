package com.bluedream.lottery;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {
    private final BlueDreamLottery plugin;
    private final File dataFolder;
    private final Map<UUID, FileConfiguration> configs = new ConcurrentHashMap<>();

    public PlayerDataManager(BlueDreamLottery plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "userdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    private FileConfiguration getPlayerConfig(UUID uuid) {
        if (configs.containsKey(uuid)) {
            return configs.get(uuid);
        }
        
        File file = new File(dataFolder, uuid.toString() + ".yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        configs.put(uuid, config);
        return config;
    }

    public int getPity(UUID uuid, String poolName) {
        return getPlayerConfig(uuid).getInt("pity." + poolName, 0);
    }

    public int getTotalDraws(UUID uuid, String poolName) {
        return getPlayerConfig(uuid).getInt("total_draws." + poolName, 0);
    }

    public int getGlobalTotalDraws(UUID uuid) {
        FileConfiguration config = getPlayerConfig(uuid);
        if (!config.contains("total_draws")) return 0;
        int total = 0;
        for (String key : config.getConfigurationSection("total_draws").getKeys(false)) {
            total += config.getInt("total_draws." + key);
        }
        return total;
    }

    public void incrementPity(UUID uuid, String poolName) {
        FileConfiguration config = getPlayerConfig(uuid);
        int current = config.getInt("pity." + poolName, 0);
        config.set("pity." + poolName, current + 1);
        
        int total = config.getInt("total_draws." + poolName, 0);
        config.set("total_draws." + poolName, total + 1);
        
        save(uuid);
    }

    public void resetPity(UUID uuid, String poolName) {
        FileConfiguration config = getPlayerConfig(uuid);
        config.set("pity." + poolName, 0);
        save(uuid);
    }

    public void setPity(UUID uuid, String poolName, int count) {
        FileConfiguration config = getPlayerConfig(uuid);
        config.set("pity." + poolName, count);
        save(uuid);
    }

    private void save(UUID uuid) {
        FileConfiguration config = configs.get(uuid);
        if (config == null) return;
        
        String data = config.saveToString();
        
        if (!plugin.isEnabled()) {
            try {
                config.save(new File(dataFolder, uuid.toString() + ".yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                config.save(new File(dataFolder, uuid.toString() + ".yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void unloadPlayer(UUID uuid) {
        configs.remove(uuid);
    }

    public void saveAll() {
        for (UUID uuid : configs.keySet()) {
            save(uuid);
        }
    }
}
