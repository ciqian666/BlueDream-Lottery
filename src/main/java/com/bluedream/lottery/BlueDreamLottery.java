package com.bluedream.lottery;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class BlueDreamLottery extends JavaPlugin {
    private static BlueDreamLottery instance;
    private LotteryManager manager;
    private PlayerDataManager playerDataManager;
    private LanguageManager languageManager;
    private HologramManager hologramManager;
    private LotteryAdminGUI adminGUI;
    private LotteryPlayGUI playGUI;
    private LotteryStatsGUI statsGUI;
    private Economy econ = null;
    private Object ppAPI = null;
    private DatabaseManager databaseManager;
    private StatisticsManager statisticsManager;

    @Override
    public void onEnable() {
        try {
            instance = this;
            
            saveDefaultConfig();
            languageManager = new LanguageManager(this);

            if (!setupEconomy()) {
                getLogger().warning(languageManager.getMessage("vault_missing").replaceAll("§.", ""));
            }
            setupPlayerPoints();

            if (getConfig().getBoolean("database.enabled", false)) {
                try {
                    databaseManager = new DatabaseManager(this);
                    statisticsManager = new StatisticsManager(this, databaseManager);
                } catch (Exception e) {
                    getLogger().severe("数据库初始化失败！请检查数据库配置或驱动。");
                    e.printStackTrace();
                }
            }

            manager = new LotteryManager(this);
            playerDataManager = new PlayerDataManager(this);
            hologramManager = new HologramManager(this);
            hologramManager.updateAllHolograms();
            adminGUI = new LotteryAdminGUI(this);
            playGUI = new LotteryPlayGUI(this);
            statsGUI = new LotteryStatsGUI(this);

            getServer().getPluginManager().registerEvents(adminGUI, this);
            getServer().getPluginManager().registerEvents(playGUI, this);
            getServer().getPluginManager().registerEvents(statsGUI, this);
            getServer().getPluginManager().registerEvents(new GlobalListener(this), this);
            getServer().getPluginManager().registerEvents(new ChatInputListener(this), this);
            getServer().getPluginManager().registerEvents(new LotteryChestListener(this), this);
            
            try {
                Class.forName("org.bukkit.event.block.BlockExplodeEvent");
                getServer().getPluginManager().registerEvents(new ExplosionListener(this), this);
            } catch (Exception ignored) {}
            
            if (getCommand("lottery") != null) {
                getCommand("lottery").setExecutor(new LotteryCommand(this));
                getCommand("lottery").setTabCompleter(new LotteryTabCompleter(this));
            } else {
                getLogger().severe("无法找到指令 'lottery'，请检查 plugin.yml！");
            }

            if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                new LotteryExpansion(this).register();
            }

            new ParticleTask(this).runTaskTimer(this, 20L, 4L);

            getLogger().info(languageManager.getMessage("plugin_loaded").replaceAll("§.", ""));
        } catch (Throwable t) {
            getLogger().severe("插件加载过程中发生严重错误: " + t.getMessage());
            t.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    public void reloadPlugin() {
        reloadConfig();
        languageManager.reload();
        
        if (manager != null) {
            manager.loadPools();
            manager.loadBlocks();
        }

        if (hologramManager != null) {
            hologramManager.updateAllHolograms();
        }
        
        if (getConfig().getBoolean("database.enabled", false)) {
            if (databaseManager == null) {
                try {
                    databaseManager = new DatabaseManager(this);
                    statisticsManager = new StatisticsManager(this, databaseManager);
                } catch (Exception e) {
                    getLogger().severe("数据库重载初始化失败！");
                    e.printStackTrace();
                }
            }
        } else {
            if (databaseManager != null) {
                databaseManager.close();
                databaseManager = null;
                statisticsManager = null;
                getLogger().info("数据库功能已关闭并断开连接。");
            }
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private void setupPlayerPoints() {
        if (getServer().getPluginManager().getPlugin("PlayerPoints") != null) {
            try {
                Class<?> ppClass = Class.forName("org.black_ixx.playerpoints.PlayerPoints");
                Object instance = ppClass.getMethod("getInstance").invoke(null);
                ppAPI = ppClass.getMethod("getAPI").invoke(instance);
            } catch (Exception e) {
                getLogger().warning(languageManager.getMessage("playerpoints_init_failed")
                        .replace("{error}", e.getMessage())
                        .replaceAll("§.", ""));
            }
        } else {
            getLogger().warning(languageManager.getMessage("playerpoints_missing").replaceAll("§.", ""));
        }
    }

    @Override
    public void onDisable() {
        if (hologramManager != null) {
            hologramManager.removeAllHolograms();
        }
        if (manager != null) {
            manager.savePools();
            manager.saveBlocks();
        }
        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    public static BlueDreamLottery getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public StatisticsManager getStatisticsManager() {
        return statisticsManager;
    }

    public LotteryStatsGUI getStatsGUI() {
        return statsGUI;
    }

    public LotteryManager getManager() {
        return manager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public LotteryAdminGUI getAdminGUI() {
        return adminGUI;
    }

    public LotteryPlayGUI getPlayGUI() {
        return playGUI;
    }

    public Economy getEconomy() {
        return econ;
    }

    public Object getPlayerPointsAPI() {
        return ppAPI;
    }
}
