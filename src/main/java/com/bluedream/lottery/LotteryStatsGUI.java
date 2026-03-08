package com.bluedream.lottery;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class LotteryStatsGUI implements Listener {
    private final BlueDreamLottery plugin;

    public LotteryStatsGUI(BlueDreamLottery plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player) {
        LanguageManager lm = plugin.getLanguageManager();
        Inventory inv = Bukkit.createInventory(new LotteryHolder("SYSTEM", "STATS_MAIN"), 27, "§6§lBlueDream §8- §n统计与排行");

        ItemStack glass = Adapter.getGlassPane(7);
        for (int i = 0; i < 27; i++) inv.setItem(i, glass);

        inv.setItem(10, createItem(Adapter.getMaterial("BOOK", "BOOK"), "§e§l全局统计", "§7查看全服抽奖数据概览"));
        inv.setItem(11, createItem(Adapter.getMaterial("PLAYER_HEAD", "SKULL_ITEM"), "§b§l个人统计", "§7查看您的抽奖记录与成就"));
        inv.setItem(12, createItem(Adapter.getMaterial("GOLD_INGOT", "GOLD_INGOT"), "§6§l排行榜", "§7查看全服欧皇与土豪排行"));
        inv.setItem(14, createItem(Adapter.getMaterial("DIAMOND", "DIAMOND"), "§a§l热门掉落", "§7查看最受欢迎的奖励物品"));
        inv.setItem(15, createItem(Adapter.getMaterial("CHEST", "CHEST"), "§d§l热门奖池", "§7查看参与人数最多的奖池"));
        inv.setItem(16, createItem(Adapter.getMaterial("REDSTONE_TORCH", "REDSTONE_TORCH_ON"), "§c§l数据库监控", "§7查看数据库连接与状态"));

        player.openInventory(inv);
    }

    public void openLeaderboardMenu(Player player) {
        Inventory inv = Bukkit.createInventory(new LotteryHolder("SYSTEM", "STATS_RANK_MENU"), 27, "§6§l排行榜中心");
        ItemStack glass = Adapter.getGlassPane(7);
        for (int i = 0; i < 27; i++) inv.setItem(i, glass);

        inv.setItem(10, createItem(Adapter.getMaterial("IRON_PICKAXE", "IRON_PICKAXE"), "§f§l抽奖榜", "§7抽奖次数最多的玩家"));
        inv.setItem(12, createItem(Adapter.getMaterial("GOLD_BLOCK", "GOLD_BLOCK"), "§e§l消耗榜", "§7消费金额最高的玩家"));
        inv.setItem(14, createItem(Adapter.getMaterial("NETHER_STAR", "NETHER_STAR"), "§b§l大奖榜", "§7抽中大奖次数最多的玩家"));
        inv.setItem(16, createItem(Adapter.getMaterial("TOTEM_OF_UNDYING", "TOTEM"), "§d§l欧皇榜", "§7大奖爆率最高的玩家", "§8(需至少抽奖10次)"));

        inv.setItem(22, createItem(Adapter.getMaterial("ARROW", "ARROW"), "§7返回主菜单"));
        player.openInventory(inv);
    }

    public void openRank(Player player, String type, String title) {
        Inventory inv = Bukkit.createInventory(new LotteryHolder("SYSTEM", "STATS_RANK_" + type.toUpperCase()), 27, title);
        ItemStack glass = Adapter.getGlassPane(7);
        for (int i = 0; i < 27; i++) inv.setItem(i, glass);

        List<StatisticsManager.RankingEntry> list = plugin.getStatisticsManager().getRanking(type);
        int[] slots = {10, 11, 12, 13, 14, 15, 16};
        
        for (int i = 0; i < list.size() && i < slots.length; i++) {
            StatisticsManager.RankingEntry entry = list.get(i);
            String valueStr = type.equals("spent") ? String.format("%.2f", entry.value) : 
                             type.equals("lucky") ? String.format("%.2f%%", entry.value) : 
                             String.valueOf((int)entry.value);
            
            inv.setItem(slots[i], createItem(Adapter.getMaterial("PAPER", "PAPER"), 
                "§e第 " + (i + 1) + " 名: §f" + entry.name, 
                "§7数值: §b" + valueStr));
        }

        inv.setItem(22, createItem(Adapter.getMaterial("ARROW", "ARROW"), "§7返回排行榜菜单"));
        player.openInventory(inv);
    }

    public void openGlobalStats(Player player) {
        Map<String, Object> stats = plugin.getStatisticsManager().getGlobalStats();
        Inventory inv = Bukkit.createInventory(new LotteryHolder("SYSTEM", "STATS_GLOBAL"), 27, "§6§l全局统计概览");
        ItemStack glass = Adapter.getGlassPane(7);
        for (int i = 0; i < 27; i++) inv.setItem(i, glass);

        inv.setItem(10, createItem(Adapter.getMaterial("CLOCK", "WATCH"), "§e今日抽奖数", "§f" + stats.getOrDefault("today_draws", 0)));
        inv.setItem(12, createItem(Adapter.getMaterial("BEACON", "BEACON"), "§b总计抽奖数", "§f" + stats.getOrDefault("total_draws", 0)));
        inv.setItem(14, createItem(Adapter.getMaterial("EMERALD", "EMERALD"), "§a累计总消耗", 
            "§7金币: §f" + String.format("%.2f", (Double)stats.getOrDefault("total_spent_vault", 0.0)),
            "§7点券: §f" + String.format("%.2f", (Double)stats.getOrDefault("total_spent_points", 0.0)),
            "§7钥匙: §f" + stats.getOrDefault("total_spent_keys", 0)));
        inv.setItem(16, createItem(Adapter.getMaterial("NETHER_STAR", "NETHER_STAR"), "§d大奖总产出", "§f" + stats.getOrDefault("total_grands", 0)));

        inv.setItem(22, createItem(Adapter.getMaterial("ARROW", "ARROW"), "§7返回主菜单"));
        player.openInventory(inv);
    }

    public void openPersonalStats(Player player) {
        Map<String, Object> stats = plugin.getStatisticsManager().getPlayerStats(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(new LotteryHolder("SYSTEM", "STATS_PERSONAL"), 27, "§b§l个人统计中心");
        ItemStack glass = Adapter.getGlassPane(7);
        for (int i = 0; i < 27; i++) inv.setItem(i, glass);

        inv.setItem(10, createItem(Adapter.getMaterial("PAPER", "PAPER"), "§e累计抽奖", "§f" + stats.getOrDefault("draws", 0) + " 次"));
        inv.setItem(12, createItem(Adapter.getMaterial("GOLD_INGOT", "GOLD_INGOT"), "§6累计消费", 
            "§7金币: §f" + String.format("%.2f", (Double)stats.getOrDefault("spent_vault", 0.0)),
            "§7点券: §f" + String.format("%.2f", (Double)stats.getOrDefault("spent_points", 0.0)),
            "§7钥匙: §f" + stats.getOrDefault("spent_keys", 0)));
        inv.setItem(14, createItem(Adapter.getMaterial("DIAMOND", "DIAMOND"), "§b抽中大奖", "§f" + stats.getOrDefault("grands", 0) + " 次"));
        
        int draws = (int)stats.getOrDefault("draws", 0);
        int grands = (int)stats.getOrDefault("grands", 0);
        double rate = draws > 0 ? (double)grands / draws * 100 : 0;
        inv.setItem(16, createItem(Adapter.getMaterial("EXPERIENCE_BOTTLE", "EXP_BOTTLE"), "§d大奖爆率", "§f" + String.format("%.2f%%", rate)));

        inv.setItem(22, createItem(Adapter.getMaterial("ARROW", "ARROW"), "§7返回主菜单"));
        player.openInventory(inv);
    }

    public void openHotDrops(Player player) {
        List<Map<String, Object>> hotDrops = plugin.getStatisticsManager().getHotDrops();
        Inventory inv = Bukkit.createInventory(new LotteryHolder("SYSTEM", "STATS_HOT_DROPS"), 27, "§a§l热门掉落排行");
        ItemStack glass = Adapter.getGlassPane(7);
        for (int i = 0; i < 27; i++) inv.setItem(i, glass);

        int[] slots = {10, 11, 12, 13, 14, 15, 16};
        for (int i = 0; i < hotDrops.size() && i < slots.length; i++) {
            Map<String, Object> drop = hotDrops.get(i);
            inv.setItem(slots[i], createItem(Adapter.getMaterial("CHEST", "CHEST"), 
                "§eTOP " + (i + 1) + ": §f" + drop.get("name"), 
                "§7掉落次数: §b" + drop.get("count")));
        }

        inv.setItem(22, createItem(Adapter.getMaterial("ARROW", "ARROW"), "§7返回主菜单"));
        player.openInventory(inv);
    }

    public void openHotPools(Player player) {
        List<Map<String, Object>> hotPools = plugin.getStatisticsManager().getHotPools();
        Inventory inv = Bukkit.createInventory(new LotteryHolder("SYSTEM", "STATS_HOT_POOLS"), 27, "§d§l热门奖池排行");
        ItemStack glass = Adapter.getGlassPane(7);
        for (int i = 0; i < 27; i++) inv.setItem(i, glass);

        int[] slots = {11, 12, 13, 14, 15};
        for (int i = 0; i < hotPools.size() && i < slots.length; i++) {
            Map<String, Object> pool = hotPools.get(i);
            inv.setItem(slots[i], createItem(Adapter.getMaterial("ENDER_CHEST", "ENDER_CHEST"), 
                "§eTOP " + (i + 1) + ": §f" + pool.get("name"), 
                "§7参与次数: §b" + pool.get("count")));
        }

        inv.setItem(22, createItem(Adapter.getMaterial("ARROW", "ARROW"), "§7返回主菜单"));
        player.openInventory(inv);
    }

    public void openDBMonitor(Player player) {
        Inventory inv = Bukkit.createInventory(new LotteryHolder("SYSTEM", "STATS_DB"), 27, "§c§l数据库监控状态");
        ItemStack glass = Adapter.getGlassPane(7);
        for (int i = 0; i < 27; i++) inv.setItem(i, glass);

        String type = plugin.getConfig().getString("database.type", "sqlite").toUpperCase();
        inv.setItem(11, createItem(Adapter.getMaterial("COMPARATOR", "REDSTONE_COMPARATOR"), "§e数据库类型", "§f" + type));
        inv.setItem(13, createItem(Adapter.getMaterial("REPEATER", "DIODE"), "§a连接状态", "§f已连接 (HikariCP)"));
        inv.setItem(15, createItem(Adapter.getMaterial("REDSTONE", "REDSTONE"), "§b数据保留天数", "§f" + plugin.getConfig().getInt("statistics.retention_days", 30) + " 天"));

        inv.setItem(22, createItem(Adapter.getMaterial("ARROW", "ARROW"), "§7返回主菜单"));
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof LotteryHolder)) return;
        LotteryHolder holder = (LotteryHolder) event.getInventory().getHolder();
        if (!holder.getPoolName().equals("SYSTEM")) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        String type = holder.getType();

        if (type.equals("STATS_MAIN")) {
            switch (event.getRawSlot()) {
                case 10: openGlobalStats(player); break;
                case 11: openPersonalStats(player); break;
                case 12: openLeaderboardMenu(player); break;
                case 14: openHotDrops(player); break;
                case 15: openHotPools(player); break;
                case 16: openDBMonitor(player); break;
            }
        } else if (type.equals("STATS_RANK_MENU")) {
            if (event.getRawSlot() == 22) { openMainMenu(player); return; }
            switch (event.getRawSlot()) {
                case 10: openRank(player, "draws", "§f§l抽奖榜 TOP 10"); break;
                case 12: openRank(player, "spent", "§e§l消耗榜 TOP 10"); break;
                case 14: openRank(player, "grands", "§b§l大奖榜 TOP 10"); break;
                case 16: openRank(player, "lucky", "§d§l欧皇榜 TOP 10"); break;
            }
        } else if (type.startsWith("STATS_RANK_") || type.equals("STATS_GLOBAL") || type.equals("STATS_PERSONAL") || type.equals("STATS_HOT_DROPS") || type.equals("STATS_HOT_POOLS") || type.equals("STATS_DB")) {
            if (event.getRawSlot() == 22) {
                if (type.startsWith("STATS_RANK_")) openLeaderboardMenu(player);
                else openMainMenu(player);
            }
        }
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }
}
