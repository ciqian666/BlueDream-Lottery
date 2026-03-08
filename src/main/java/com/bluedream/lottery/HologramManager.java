package com.bluedream.lottery;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HologramManager {
    private final BlueDreamLottery plugin;
    private final Map<Location, Entity> activeItemHolograms = new ConcurrentHashMap<>();
    private final Map<Location, Entity> activeTextHolograms = new ConcurrentHashMap<>();
    private final Map<Location, Integer> itemIndices = new ConcurrentHashMap<>();
    private final boolean isSupported;

    public HologramManager(BlueDreamLottery plugin) {
        this.plugin = plugin;
        this.isSupported = Adapter.isDisplaySupported();
        if (isSupported) {
            startUpdateTask();
        }
    }

    public void updateAllHolograms() {
        if (!isSupported) return;

        removeAllHolograms();
        
        Map<Location, String> blocks = plugin.getManager().getCachedLocations();
        for (Map.Entry<Location, String> entry : blocks.entrySet()) {
            createHologram(entry.getKey(), entry.getValue());
        }
    }

    public void createHologram(Location loc, String poolName) {
        if (!isSupported || !plugin.getConfig().getBoolean("hologram.enabled", true)) return;
        
        LotteryPool pool = plugin.getManager().getPool(poolName);
        if (pool == null) return;

        double itemHeight = plugin.getConfig().getDouble("hologram.item_height_offset", 1.2);
        double textHeight = plugin.getConfig().getDouble("hologram.text_height_offset", 1.8);

        Location itemLoc = loc.clone().add(0.5, itemHeight, 0.5);
        Location textLoc = loc.clone().add(0.5, textHeight, 0.5);
        
        Bukkit.getScheduler().runTask(plugin, () -> {
            removeHologram(loc);
            
            if (!pool.getItems().isEmpty()) {
                ItemDisplay display = (ItemDisplay) itemLoc.getWorld().spawnEntity(itemLoc, EntityType.valueOf("ITEM_DISPLAY"));
                display.setItemStack(pool.getItems().get(0).getItem());
                display.setBillboard(ItemDisplay.Billboard.CENTER);
                
                org.bukkit.util.Transformation transformation = display.getTransformation();
                transformation.getScale().set(0.6f, 0.6f, 0.6f);
                display.setTransformation(transformation);
                
                activeItemHolograms.put(loc, display);
                itemIndices.put(loc, 0);
            }

            if (plugin.getConfig().getBoolean("hologram.show_text", true)) {
                org.bukkit.entity.TextDisplay textDisplay = (org.bukkit.entity.TextDisplay) textLoc.getWorld().spawnEntity(textLoc, EntityType.valueOf("TEXT_DISPLAY"));
                updateHologramText(textDisplay, pool);
                textDisplay.setBillboard(org.bukkit.entity.Display.Billboard.CENTER);
                
                String bgColorStr = plugin.getConfig().getString("hologram.text_background_color", "DEFAULT");
                if (!bgColorStr.equalsIgnoreCase("DEFAULT")) {
                    try {
                        if (bgColorStr.startsWith("#")) {
                            long colorLong = Long.parseLong(bgColorStr.substring(1), 16);
                            if (bgColorStr.length() == 7) {
                                colorLong |= 0xFF000000L;
                            }
                            textDisplay.setBackgroundColor(org.bukkit.Color.fromARGB((int) colorLong));
                        }
                    } catch (Exception ignored) {}
                }
                
                textDisplay.setShadowed(plugin.getConfig().getBoolean("hologram.text_shadow", true));
                
                activeTextHolograms.put(loc, textDisplay);
            }
        });
    }

    private void updateHologramText(org.bukkit.entity.TextDisplay display, LotteryPool pool) {
        LanguageManager lm = plugin.getLanguageManager();
        String title = plugin.getConfig().getString("hologram.text_format", "&6&l{name}")
                .replace("{name}", pool.getName());
        
        StringBuilder sb = new StringBuilder();
        sb.append(title);
        
        if (plugin.getConfig().getBoolean("hologram.show_cost", true)) {
            sb.append("\n");
            String costText = "";
            switch (pool.getCostType()) {
                case "VAULT":
                    costText = lm.getMessage("cost_vault")
                        .replace("{value}", String.valueOf(pool.getCostValue()))
                        .replace("{value10}", String.valueOf(pool.getCostValue() * 10));
                    break;
                case "PLAYERPOINTS":
                    costText = lm.getMessage("cost_points")
                        .replace("{value}", String.valueOf((int)pool.getCostValue()))
                        .replace("{value10}", String.valueOf((int)(pool.getCostValue() * 10)));
                    break;
                case "KEY":
                    costText = lm.getMessage("cost_key")
                        .replace("{name}", pool.getKeyName());
                    break;
                default:
                    costText = lm.getMessage("cost_free");
                    break;
            }
            sb.append(costText);
        }
        
        display.setText(Adapter.color(sb.toString()));
    }

    public void removeHologram(Location loc) {
        Entity itemEntity = activeItemHolograms.remove(loc);
        if (itemEntity != null) {
            itemEntity.remove();
        }
        Entity textEntity = activeTextHolograms.remove(loc);
        if (textEntity != null) {
            textEntity.remove();
        }
        itemIndices.remove(loc);
    }

    public void removeAllHolograms() {
        for (Entity entity : activeItemHolograms.values()) {
            entity.remove();
        }
        for (Entity entity : activeTextHolograms.values()) {
            entity.remove();
        }
        activeItemHolograms.clear();
        activeTextHolograms.clear();
        itemIndices.clear();
    }

    private void startUpdateTask() {
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!plugin.isEnabled()) {
                    cancel();
                    return;
                }

                ticks++;
                
                float rotationSpeed = (float) plugin.getConfig().getDouble("hologram.rotation_speed", 2.0);
                int cycleInterval = plugin.getConfig().getInt("hologram.cycle_interval", 3) * 20;

                for (Map.Entry<Location, Entity> entry : activeItemHolograms.entrySet()) {
                    Location loc = entry.getKey();
                    Entity entity = entry.getValue();
                    
                    if (entity == null || !entity.isValid()) {
                        continue;
                    }

                    if (entity instanceof ItemDisplay) {
                        ItemDisplay display = (ItemDisplay) entity;
                        
                        org.bukkit.util.Transformation transformation = display.getTransformation();
                        transformation.getLeftRotation().rotationY((float) Math.toRadians(ticks * rotationSpeed));
                        display.setTransformation(transformation);
                        
                        if (ticks % cycleInterval == 0) {
                            String poolName = plugin.getManager().getCachedLocations().get(loc);
                            if (poolName != null) {
                                LotteryPool pool = plugin.getManager().getPool(poolName);
                                if (pool != null) {
                                    if (!pool.getItems().isEmpty()) {
                                        int nextIndex = (itemIndices.getOrDefault(loc, 0) + 1) % pool.getItems().size();
                                        display.setItemStack(pool.getItems().get(nextIndex).getItem());
                                        itemIndices.put(loc, nextIndex);
                                    }
                                    
                                    Entity textEntity = activeTextHolograms.get(loc);
                                    if (textEntity instanceof org.bukkit.entity.TextDisplay) {
                                        updateHologramText((org.bukkit.entity.TextDisplay) textEntity, pool);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }
}
