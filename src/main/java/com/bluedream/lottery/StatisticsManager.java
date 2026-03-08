package com.bluedream.lottery;

import org.bukkit.Bukkit;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StatisticsManager {
    private final BlueDreamLottery plugin;
    private final DatabaseManager db;
    private final Map<String, List<RankingEntry>> rankings = new ConcurrentHashMap<>();
    
    public StatisticsManager(BlueDreamLottery plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
        
        if (plugin.getConfig().getBoolean("statistics.enabled", true)) {
            startRankingTask();
        }
    }

    public void logDraw(UUID uuid, String name, String poolName, String itemName, String itemDisplayName, boolean isGrandPrize, String costType, double costValue) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String prefix = db.getPrefix();
            try (Connection conn = db.getConnection()) {
                String logSql = "INSERT INTO " + prefix + "logs (player_uuid, player_name, pool_name, item_name, item_display_name, is_grand_prize, cost_type, cost_value) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(logSql)) {
                    ps.setString(1, uuid.toString());
                    ps.setString(2, name);
                    ps.setString(3, poolName);
                    ps.setString(4, itemName);
                    ps.setString(5, itemDisplayName);
                    ps.setBoolean(6, isGrandPrize);
                    ps.setString(7, costType);
                    ps.setDouble(8, costValue);
                    ps.executeUpdate();
                }

                updatePlayerStats(conn, prefix, uuid, name, costType, costValue, isGrandPrize);
                updatePoolStats(conn, prefix, poolName, costType, costValue, isGrandPrize);
                
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void updatePlayerStats(Connection conn, String prefix, UUID uuid, String name, String costType, double costValue, boolean isGrandPrize) throws SQLException {
        String checkSql = "SELECT player_uuid FROM " + prefix + "player_stats WHERE player_uuid = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                String column = getCostColumn(costType);
                if (rs.next()) {
                    String updateSql = "UPDATE " + prefix + "player_stats SET player_name = ?, total_draws = total_draws + 1, " + column + " = " + column + " + ?, grand_prizes = grand_prizes + ?, last_draw = CURRENT_TIMESTAMP WHERE player_uuid = ?";
                    try (PreparedStatement ups = conn.prepareStatement(updateSql)) {
                        ups.setString(1, name);
                        ups.setDouble(2, costValue);
                        ups.setInt(3, isGrandPrize ? 1 : 0);
                        ups.setString(4, uuid.toString());
                        ups.executeUpdate();
                    }
                } else {
                    String insertSql = "INSERT INTO " + prefix + "player_stats (player_uuid, player_name, total_draws, " + column + ", grand_prizes, last_draw) VALUES (?, ?, 1, ?, ?, CURRENT_TIMESTAMP)";
                    try (PreparedStatement ips = conn.prepareStatement(insertSql)) {
                        ips.setString(1, uuid.toString());
                        ips.setString(2, name);
                        ips.setDouble(3, costValue);
                        ips.setInt(4, isGrandPrize ? 1 : 0);
                        ips.executeUpdate();
                    }
                }
            }
        }
    }

    private void updatePoolStats(Connection conn, String prefix, String poolName, String costType, double costValue, boolean isGrandPrize) throws SQLException {
        String checkSql = "SELECT pool_name FROM " + prefix + "pool_stats WHERE pool_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, poolName);
            try (ResultSet rs = ps.executeQuery()) {
                String column = getCostColumn(costType);
                if (rs.next()) {
                    String updateSql = "UPDATE " + prefix + "pool_stats SET total_draws = total_draws + 1, " + column + " = " + column + " + ?, grand_prizes_given = grand_prizes_given + ? WHERE pool_name = ?";
                    try (PreparedStatement ups = conn.prepareStatement(updateSql)) {
                        ups.setDouble(1, costValue);
                        ups.setInt(2, isGrandPrize ? 1 : 0);
                        ups.setString(3, poolName);
                        ups.executeUpdate();
                    }
                } else {
                    String insertSql = "INSERT INTO " + prefix + "pool_stats (pool_name, total_draws, " + column + ", grand_prizes_given) VALUES (?, 1, ?, ?)";
                    try (PreparedStatement ips = conn.prepareStatement(insertSql)) {
                        ips.setString(1, poolName);
                        ips.setDouble(2, costValue);
                        ips.setInt(3, isGrandPrize ? 1 : 0);
                        ips.executeUpdate();
                    }
                }
            }
        }
    }

    private String getCostColumn(String costType) {
        if (costType == null) return "total_spent_vault";
        switch (costType.toUpperCase()) {
            case "VAULT":
            case "MONEY":
            case "ECONOMY":
                return "total_spent_vault";
            case "POINTS":
            case "PLAYERPOINTS":
                return "total_spent_points";
            case "KEY":
            case "ITEM":
                return "total_spent_keys";
            default:
                return "total_spent_vault";
        }
    }

    private boolean isSQLite() {
        return plugin.getConfig().getString("database.type", "sqlite").equalsIgnoreCase("sqlite");
    }

    private void startRankingTask() {
        long interval = plugin.getConfig().getLong("statistics.ranking_update_interval", 10) * 20 * 60;
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::updateRankings, 20 * 5, interval);
        
        long maintenanceInterval = 24 * 60 * 60 * 20;
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::performMaintenance, 20 * 60, maintenanceInterval);
    }

    public void updateRankings() {
        String prefix = db.getPrefix();
        try (Connection conn = db.getConnection()) {
            rankings.put("draws", fetchRanking(conn, "SELECT player_name, total_draws as value FROM " + prefix + "player_stats ORDER BY total_draws DESC LIMIT 10"));
            
            rankings.put("spent", fetchRanking(conn, "SELECT player_name, (total_spent_vault + total_spent_points) as value FROM " + prefix + "player_stats ORDER BY value DESC LIMIT 10"));
            
            rankings.put("grands", fetchRanking(conn, "SELECT player_name, grand_prizes as value FROM " + prefix + "player_stats ORDER BY grand_prizes DESC LIMIT 10"));
            
            rankings.put("lucky", fetchRanking(conn, "SELECT player_name, (CAST(grand_prizes AS DOUBLE) / total_draws) * 100 as value FROM " + prefix + "player_stats WHERE total_draws >= 10 ORDER BY value DESC LIMIT 10"));
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<RankingEntry> fetchRanking(Connection conn, String sql) throws SQLException {
        List<RankingEntry> list = new ArrayList<>();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new RankingEntry(rs.getString(1), rs.getDouble(2)));
            }
        }
        return list;
    }

    private void performMaintenance() {
        int days = plugin.getConfig().getInt("statistics.retention_days", 30);
        String prefix = db.getPrefix();
        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            String sql = isSQLite() ? 
                "DELETE FROM " + prefix + "logs WHERE draw_time < datetime('now', '-" + days + " days')" :
                "DELETE FROM " + prefix + "logs WHERE draw_time < NOW() - INTERVAL " + days + " DAY";
            int deleted = stmt.executeUpdate(sql);
            if (deleted > 0) {
                plugin.getLogger().info("Cleared " + deleted + " old lottery logs.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<RankingEntry> getRanking(String type) {
        return rankings.getOrDefault(type, Collections.emptyList());
    }

    public Map<String, Object> getGlobalStats() {
        Map<String, Object> stats = new HashMap<>();
        String prefix = db.getPrefix();
        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + prefix + "logs")) {
                if (rs.next()) stats.put("total_draws", rs.getInt(1));
            }
            try (ResultSet rs = stmt.executeQuery("SELECT SUM(CASE WHEN cost_type IN ('VAULT','MONEY','ECONOMY') THEN cost_value ELSE 0 END), " +
                                                 "SUM(CASE WHEN cost_type IN ('POINTS','PLAYERPOINTS') THEN cost_value ELSE 0 END), " +
                                                 "SUM(CASE WHEN cost_type IN ('KEY','ITEM') THEN cost_value ELSE 0 END) FROM " + prefix + "logs")) {
                if (rs.next()) {
                    stats.put("total_spent_vault", rs.getDouble(1));
                    stats.put("total_spent_points", rs.getDouble(2));
                    stats.put("total_spent_keys", rs.getInt(3));
                }
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + prefix + "logs WHERE is_grand_prize = 1")) {
                if (rs.next()) stats.put("total_grands", rs.getInt(1));
            }
            String todaySql = isSQLite() ? 
                "SELECT COUNT(*) FROM " + prefix + "logs WHERE draw_time >= date('now')" :
                "SELECT COUNT(*) FROM " + prefix + "logs WHERE draw_time >= CURDATE()";
            try (ResultSet rs = stmt.executeQuery(todaySql)) {
                if (rs.next()) stats.put("today_draws", rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    public Map<String, Object> getPlayerStats(UUID uuid) {
        Map<String, Object> stats = new HashMap<>();
        String prefix = db.getPrefix();
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT total_draws, total_spent_vault, total_spent_points, total_spent_keys, grand_prizes FROM " + prefix + "player_stats WHERE player_uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.put("draws", rs.getInt(1));
                    stats.put("spent_vault", rs.getDouble(2));
                    stats.put("spent_points", rs.getDouble(3));
                    stats.put("spent_keys", rs.getInt(4));
                    stats.put("grands", rs.getInt(5));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    public List<Map<String, Object>> getHotDrops() {
        List<Map<String, Object>> list = new ArrayList<>();
        String prefix = db.getPrefix();
        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT item_display_name, COUNT(*) as count FROM " + prefix + "logs GROUP BY item_display_name ORDER BY count DESC LIMIT 10")) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", rs.getString(1));
                map.put("count", rs.getInt(2));
                list.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Map<String, Object>> getHotPools() {
        List<Map<String, Object>> list = new ArrayList<>();
        String prefix = db.getPrefix();
        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT pool_name, total_draws FROM " + prefix + "pool_stats ORDER BY total_draws DESC LIMIT 5")) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", rs.getString(1));
                map.put("count", rs.getInt(2));
                list.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static class RankingEntry {
        public final String name;
        public final double value;
        public RankingEntry(String name, double value) {
            this.name = name;
            this.value = value;
        }
    }
}
