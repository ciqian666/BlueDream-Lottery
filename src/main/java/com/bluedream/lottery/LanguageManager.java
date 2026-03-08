package com.bluedream.lottery;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    private final BlueDreamLottery plugin;
    private FileConfiguration langConfig;

    public LanguageManager(BlueDreamLottery plugin) {
        this.plugin = plugin;
        loadLanguage();
    }

    public void setLanguage(String newLang) {
        plugin.getConfig().set("language", newLang.toLowerCase());
        plugin.saveConfig();
        loadLanguage();
    }

    private String getSelectedLanguage() {
        String configLang = plugin.getConfig().getString("language", "zh_cn");
        return (configLang != null) ? configLang.toLowerCase() : "zh_cn";
    }

    public void reload() {
        loadLanguage();
    }

    public void loadLanguage() {
        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists()) langDir.mkdirs();

        saveDefaultLangFile(langDir, "zh_cn.yml");
        saveDefaultLangFile(langDir, "en_us.yml");

        String currentLang = getSelectedLanguage();
        String fileName = currentLang + ".yml";
        File langFile = new File(langDir, fileName);
        
        if (!langFile.exists()) {
            plugin.getLogger().warning("语言文件 " + fileName + " 不存在，已回退到 zh_cn.yml");
            langFile = new File(langDir, "zh_cn.yml");
        }
        
        langConfig = YamlConfiguration.loadConfiguration(langFile);
        
        try {
            InputStream defaultStream = plugin.getResource("lang/" + (langFile.getName()));
            if (defaultStream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
                
                boolean changed = false;
                for (String key : defaultConfig.getKeys(true)) {
                    if (!langConfig.contains(key)) {
                        langConfig.set(key, defaultConfig.get(key));
                        changed = true;
                    }
                }
                
                if (changed) {
                    langConfig.save(langFile);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("未能更新语言文件: " + e.getMessage());
        }
    }

    private void saveDefaultLangFile(File langDir, String fileName) {
        File file = new File(langDir, fileName);
        if (!file.exists()) {
            try {
                plugin.saveResource("lang/" + fileName, false);
            } catch (Exception ignored) {}
        }
    }

    public String getMessage(String key) {
        return getMessage(null, key);
    }

    public String getMessage(org.bukkit.entity.Player player, String key) {
        String message = langConfig.getString(key);
        if (message == null) {
            return "§c语言键值缺失: " + key;
        }
        return Adapter.color(player, message);
    }

    public String getMessage(String key, Map<String, String> placeholders) {
        return getMessage(null, key, placeholders);
    }

    public String getMessage(org.bukkit.entity.Player player, String key, Map<String, String> placeholders) {
        String message = langConfig.getString(key);
        if (message == null) {
            return "§c语言键值缺失: " + key;
        }
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return Adapter.color(player, message);
    }
}
