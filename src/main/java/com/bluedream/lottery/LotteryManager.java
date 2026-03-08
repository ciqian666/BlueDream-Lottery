package com.bluedream.lottery;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LotteryManager {
    static {
        ConfigurationSerialization.registerClass(LotteryItem.class);
        ConfigurationSerialization.registerClass(LotteryPool.class);
    }

    private final BlueDreamLottery plugin;
    private final File poolsFolder;
    private final File poolsFile;
    private final File blocksFile;
    private FileConfiguration blocksConfig;
    private final Map<String, LotteryPool> pools = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<String, String> lotteryBlocks = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<org.bukkit.Location, String> cachedLocations = new java.util.concurrent.ConcurrentHashMap<>();

    public LotteryManager(BlueDreamLottery plugin) {
        this.plugin = plugin;
        this.poolsFolder = new File(plugin.getDataFolder(), "pools");
        this.poolsFile = new File(plugin.getDataFolder(), "pools.yml");
        this.blocksFile = new File(plugin.getDataFolder(), "blocks.yml");
        
        if (!poolsFolder.exists()) {
            poolsFolder.mkdirs();
        }
        
        loadPools();
        loadBlocks();
    }

    public synchronized void loadBlocks() {
        lotteryBlocks.clear();
        cachedLocations.clear();
        if (!blocksFile.exists()) return;
        blocksConfig = YamlConfiguration.loadConfiguration(blocksFile);
        if (blocksConfig.contains("blocks")) {
            for (String locKey : blocksConfig.getConfigurationSection("blocks").getKeys(false)) {
                String poolName = blocksConfig.getString("blocks." + locKey);
                lotteryBlocks.put(locKey, poolName);
                
                org.bukkit.Location loc = stringToLocation(locKey);
                if (loc != null) {
                    cachedLocations.put(loc, poolName);
                }
            }
        }
    }

    private org.bukkit.Location stringToLocation(String s) {
        if (s == null || s.isEmpty()) return null;
        String[] parts = s.split(",");
        if (parts.length < 4) return null;
        org.bukkit.World world = org.bukkit.Bukkit.getWorld(parts[0]);
        if (world == null) return null;
        try {
            return new org.bukkit.Location(world, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public synchronized void saveBlocks() {
        if (blocksConfig == null) blocksConfig = new YamlConfiguration();
        blocksConfig.set("blocks", null);
        for (Map.Entry<String, String> entry : lotteryBlocks.entrySet()) {
            blocksConfig.set("blocks." + entry.getKey(), entry.getValue());
        }

        if (!plugin.isEnabled()) {
            try {
                blocksConfig.save(blocksFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        final FileConfiguration configSnapshot = blocksConfig;
        final File fileSnapshot = blocksFile;
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                configSnapshot.save(fileSnapshot);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void addLotteryBlock(org.bukkit.Location loc, String poolName) {
        lotteryBlocks.put(locationToString(loc), poolName);
        cachedLocations.put(loc.getBlock().getLocation(), poolName);
        saveBlocks();
        if (plugin.getHologramManager() != null) {
            plugin.getHologramManager().createHologram(loc.getBlock().getLocation(), poolName);
        }
    }

    public void removeLotteryBlock(org.bukkit.Location loc) {
        lotteryBlocks.remove(locationToString(loc));
        cachedLocations.remove(loc.getBlock().getLocation());
        saveBlocks();
        if (plugin.getHologramManager() != null) {
            plugin.getHologramManager().removeHologram(loc.getBlock().getLocation());
        }
    }

    public String getPoolAt(org.bukkit.Location loc) {
        return lotteryBlocks.get(locationToString(loc));
    }

    public Map<String, String> getLotteryBlocks() {
        return lotteryBlocks;
    }

    public Map<org.bukkit.Location, String> getCachedLocations() {
        return cachedLocations;
    }

    private String locationToString(org.bukkit.Location loc) {
        if (loc == null) return "";
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    public synchronized void loadPools() {
        pools.clear();
        
        if (poolsFile.exists()) {
            FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(poolsFile);
            if (oldConfig.contains("pools")) {
                for (String key : oldConfig.getConfigurationSection("pools").getKeys(false)) {
                    LotteryPool pool = (LotteryPool) oldConfig.get("pools." + key);
                    if (pool != null) {
                        pools.put(key, pool);
                    }
                }
            }
            poolsFile.renameTo(new File(plugin.getDataFolder(), "pools.yml.old"));
            plugin.getLogger().info("已迁移旧版 pools.yml 到 pools 文件夹。");
            savePools();
        }

        File[] files = poolsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                String poolName = file.getName().substring(0, file.getName().length() - 4);
                FileConfiguration poolConfig = YamlConfiguration.loadConfiguration(file);
                if (poolConfig.contains("pool")) {
                    LotteryPool pool = (LotteryPool) poolConfig.get("pool");
                    if (pool != null) {
                        pools.put(poolName, pool);
                    }
                }
            }
        }
    }

    public synchronized void savePools() {
        for (Map.Entry<String, LotteryPool> entry : pools.entrySet()) {
            savePool(entry.getValue());
        }
    }

    public void savePool(LotteryPool pool) {
        File poolFile = new File(poolsFolder, pool.getName() + ".yml");
        FileConfiguration poolConfig = new YamlConfiguration();
        poolConfig.set("pool", pool);

        if (!plugin.isEnabled()) {
            try {
                poolConfig.save(poolFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                poolConfig.save(poolFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void createPool(String name) {
        LotteryPool pool = new LotteryPool(name);
        pools.put(name, pool);
        savePool(pool);
    }

    public synchronized boolean renamePool(String oldName, String newName) {
        LotteryPool pool = pools.get(oldName);
        if (pool == null || pools.containsKey(newName)) return false;

        pools.remove(oldName);
        
        File oldFile = new File(poolsFolder, oldName + ".yml");
        if (oldFile.exists()) oldFile.delete();

        pool.setName(newName);
        pools.put(newName, pool);

        savePool(pool);

        for (Map.Entry<String, String> entry : lotteryBlocks.entrySet()) {
            if (entry.getValue().equals(oldName)) {
                entry.setValue(newName);
            
                org.bukkit.Location loc = stringToLocation(entry.getKey());
                if (loc != null) {
                    cachedLocations.put(loc, newName);
                    if (plugin.getHologramManager() != null) {
                        plugin.getHologramManager().removeHologram(loc);
                        plugin.getHologramManager().createHologram(loc, newName);
                    }
                }
            }
        }
        saveBlocks();

        return true;
    }

    public void deletePool(String name) {
        pools.remove(name);
        File poolFile = new File(poolsFolder, name + ".yml");
        if (poolFile.exists()) {
            poolFile.delete();
        }
        
        boolean modified = false;
        java.util.Iterator<java.util.Map.Entry<org.bukkit.Location, String>> itLoc = cachedLocations.entrySet().iterator();
        while (itLoc.hasNext()) {
            java.util.Map.Entry<org.bukkit.Location, String> entry = itLoc.next();
            if (entry.getValue().equals(name)) {
                if (plugin.getHologramManager() != null) {
                    plugin.getHologramManager().removeHologram(entry.getKey());
                }
                itLoc.remove();
                lotteryBlocks.remove(locationToString(entry.getKey()));
                modified = true;
            }
        }
        
        if (modified) {
            saveBlocks();
        }
    }

    public LotteryPool getPool(String name) {
        return pools.get(name);
    }

    public Map<String, LotteryPool> getPools() {
        return pools;
    }
}
