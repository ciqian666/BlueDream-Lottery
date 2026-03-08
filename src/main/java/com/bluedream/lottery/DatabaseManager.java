package com.bluedream.lottery;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private final BlueDreamLottery plugin;
    private HikariDataSource dataSource;
    private final String prefix;

    public DatabaseManager(BlueDreamLottery plugin) {
        this.plugin = plugin;
        ConfigurationSection dbConfig = plugin.getConfig().getConfigurationSection("database");
        this.prefix = dbConfig.getString("prefix", "bd_lottery_");
        
        String type = dbConfig.getString("type", "sqlite").toLowerCase();
        if (type.equals("mysql") || type.equals("mariadb")) {
            createDatabaseIfNotExists(dbConfig);
        }
        
        setupPool(dbConfig);
        createTables();
    }

    private void createDatabaseIfNotExists(ConfigurationSection config) {
        String host = config.getString("host");
        int port = config.getInt("port");
        String name = config.getString("name");
        String user = config.getString("user");
        String pass = config.getString("password");
        String type = config.getString("type", "mysql").toLowerCase();

        String driverClass = "com.mysql.cj.jdbc.Driver";
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            driverClass = "com.mysql.jdbc.Driver";
        }

        String url = "jdbc:mysql://" + host + ":" + port + "/?useSSL=false&characterEncoding=UTF-8";
        
        try (Connection conn = java.sql.DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + name + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci");
        } catch (SQLException e) {
            plugin.getLogger().warning("无法自动创建数据库: " + e.getMessage());
        }
    }

    private void setupPool(ConfigurationSection config) {
        String type = config.getString("type", "sqlite").toLowerCase();
        HikariConfig hikariConfig = new HikariConfig();

        if (type.equals("sqlite")) {
            File dbFile = new File(plugin.getDataFolder(), "lottery.db");
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            
            hikariConfig.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());

            try {
                Class.forName("org.xerial.sqlite.JDBC");
                hikariConfig.setDriverClassName("org.xerial.sqlite.JDBC");
            } catch (ClassNotFoundException e) {

            }
        } else {
            String host = config.getString("host");
            int port = config.getInt("port");
            String name = config.getString("name");
            String user = config.getString("user");
            String pass = config.getString("password");

            String url = "";
            switch (type) {
                case "mysql":
                case "mariadb":
                    url = "jdbc:mysql://" + host + ":" + port + "/" + name + "?useSSL=false&characterEncoding=UTF-8";
                    try {
                        Class.forName("com.mysql.cj.jdbc.Driver");
                        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
                    } catch (ClassNotFoundException e) {
                        hikariConfig.setDriverClassName("com.mysql.jdbc.Driver");
                    }
                    break;
                case "postgresql":
                    url = "jdbc:postgresql://" + host + ":" + port + "/" + name;
                    hikariConfig.setDriverClassName("org.postgresql.Driver");
                    break;
            }
            hikariConfig.setJdbcUrl(url);
            hikariConfig.setUsername(user);
            hikariConfig.setPassword(pass);
        }

        hikariConfig.setMaximumPoolSize(config.getInt("pool_size", 10));
        hikariConfig.setPoolName("BlueDreamLotteryPool");
        
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(hikariConfig);
    }

    private void createTables() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            String idType = isSQLite() ? "INTEGER PRIMARY KEY AUTOINCREMENT" : 
                           (isMySQL() ? "INTEGER PRIMARY KEY AUTO_INCREMENT" : "SERIAL PRIMARY KEY");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS " + prefix + "logs (" +
                    "id " + idType + ", " +
                    "player_uuid VARCHAR(36) NOT NULL, " +
                    "player_name VARCHAR(32) NOT NULL, " +
                    "pool_name VARCHAR(64) NOT NULL, " +
                    "item_name TEXT NOT NULL, " +
                    "is_grand_prize BOOLEAN NOT NULL, " +
                    "cost_type VARCHAR(20) NOT NULL, " +
                    "cost_value DOUBLE NOT NULL, " +
                    "draw_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            try {
                stmt.execute("ALTER TABLE " + prefix + "logs ADD COLUMN item_display_name TEXT");
            } catch (SQLException ignored) {}

            stmt.execute("CREATE TABLE IF NOT EXISTS " + prefix + "player_stats (" +
                    "player_uuid VARCHAR(36) PRIMARY KEY, " +
                    "player_name VARCHAR(32) NOT NULL, " +
                    "total_draws INTEGER DEFAULT 0, " +
                    "total_spent_vault DOUBLE DEFAULT 0, " +
                    "total_spent_points DOUBLE DEFAULT 0, " +
                    "total_spent_keys INTEGER DEFAULT 0, " +
                    "grand_prizes INTEGER DEFAULT 0, " +
                    "last_draw TIMESTAMP)");
            
            try {
                stmt.execute("ALTER TABLE " + prefix + "player_stats ADD COLUMN total_spent_vault DOUBLE DEFAULT 0");
                stmt.execute("ALTER TABLE " + prefix + "player_stats ADD COLUMN total_spent_points DOUBLE DEFAULT 0");
                stmt.execute("ALTER TABLE " + prefix + "player_stats ADD COLUMN total_spent_keys INTEGER DEFAULT 0");
            } catch (SQLException ignored) {}

            stmt.execute("CREATE TABLE IF NOT EXISTS " + prefix + "pool_stats (" +
                    "pool_name VARCHAR(64) PRIMARY KEY, " +
                    "total_draws INTEGER DEFAULT 0, " +
                    "total_spent_vault DOUBLE DEFAULT 0, " +
                    "total_spent_points DOUBLE DEFAULT 0, " +
                    "total_spent_keys INTEGER DEFAULT 0, " +
                    "grand_prizes_given INTEGER DEFAULT 0)");

            try {
                stmt.execute("ALTER TABLE " + prefix + "pool_stats ADD COLUMN total_spent_vault DOUBLE DEFAULT 0");
                stmt.execute("ALTER TABLE " + prefix + "pool_stats ADD COLUMN total_spent_points DOUBLE DEFAULT 0");
                stmt.execute("ALTER TABLE " + prefix + "pool_stats ADD COLUMN total_spent_keys INTEGER DEFAULT 0");
            } catch (SQLException ignored) {}

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isSQLite() {
        return plugin.getConfig().getString("database.type", "sqlite").equalsIgnoreCase("sqlite");
    }

    public boolean isMySQL() {
        String type = plugin.getConfig().getString("database.type", "sqlite").toLowerCase();
        return type.equals("mysql") || type.equals("mariadb");
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public String getPrefix() {
        return prefix;
    }
}
