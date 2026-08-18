package com.bluedream.lottery;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import java.util.Collection;

public class HologramManager implements org.bukkit.event.Listener {
    private final BlueDreamLottery plugin;
    private final NamespacedKey hologramKey;
    private final Map<Location, Entity> activeItemHolograms = new ConcurrentHashMap<>();
    private final Map<Location, Entity> activeTextHolograms = new ConcurrentHashMap<>();
    private final Map<Location, Integer> itemIndices = new ConcurrentHashMap<>();
    private final boolean isSupported;
    private WrappedTask updateTask;
    private final AtomicInteger tickCounter = new AtomicInteger(0);

    public NamespacedKey getHologramKey() {
        return hologramKey;
    }

    public HologramManager(BlueDreamLottery plugin) {
        this.plugin = plugin;
        this.hologramKey = new NamespacedKey(plugin, "hologram");
        this.isSupported = Adapter.isDisplaySupported();
        if (isSupported) {
            startUpdateTask();
            // 延迟清理孤儿实体，调度到全局区域线程（Folia）/主线程（Paper/Spigot）
            plugin.getFoliaLib().getImpl().runLater(this::cleanupOrphanedHolograms, 20L);
        }
    }

    @org.bukkit.event.EventHandler
    public void onChunkLoad(org.bukkit.event.world.ChunkLoadEvent event) {
        if (!isSupported || !plugin.getConfig().getBoolean("hologram.enabled", true)) return;

        org.bukkit.Chunk chunk = event.getChunk();
        Map<Location, String> cachedLocations = plugin.getManager().getCachedLocations();

        for (Map.Entry<Location, String> entry : cachedLocations.entrySet()) {
            Location loc = entry.getKey();
            if (loc.getWorld() == null || !loc.getWorld().equals(chunk.getWorld())) continue;

            int chunkX = loc.getBlockX() >> 4;
            int chunkZ = loc.getBlockZ() >> 4;
            if (chunkX != chunk.getX() || chunkZ != chunk.getZ()) continue;

            Entity itemEntity = activeItemHolograms.get(loc);
            Entity textEntity = activeTextHolograms.get(loc);
            boolean itemValid = itemEntity != null && itemEntity.isValid();
            boolean textValid = textEntity != null && textEntity.isValid();

            if (itemValid && textValid) continue;

            forceRemoveNearbyDisplays(loc);
            activeItemHolograms.remove(loc);
            activeTextHolograms.remove(loc);
            itemIndices.remove(loc);

            createHologram(loc, entry.getValue());
        }
    }

    public void cleanupOrphanedHolograms() {
        if (!isSupported) return;

        int removedCount = 0;

        Map<Location, String> cachedLocations = plugin.getManager().getCachedLocations();

        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity == null || !entity.isValid()) continue;

                if (entity instanceof org.bukkit.entity.ItemDisplay || entity instanceof org.bukkit.entity.TextDisplay) {
                    if (entity.getPersistentDataContainer().has(hologramKey, PersistentDataType.BYTE)) {
                        Location entityLoc = entity.getLocation();
                        boolean nearRegisteredBlock = false;

                        for (Location blockLoc : cachedLocations.keySet()) {
                            if (blockLoc.getWorld() != null && blockLoc.getWorld().equals(entityLoc.getWorld())) {
                                if (blockLoc.distanceSquared(entityLoc) < 16.0) {
                                    nearRegisteredBlock = true;
                                    break;
                                }
                            }
                        }

                        if (!nearRegisteredBlock) {
                            entity.remove();
                            removedCount++;
                        }
                    }
                }
            }
        }

        if (removedCount > 0) {
            plugin.getLogger().info("启动时已清理 " + removedCount + " 个残留全息图实体。");
        }
    }

    private void removeNearbyHolograms(Location loc) {
        if (loc.getWorld() == null) return;
        Location searchCenter = loc.clone().add(0.5, 1.5, 0.5);
        Collection<Entity> entities = loc.getWorld().getNearbyEntities(searchCenter, 2.0, 3.0, 2.0);
        for (Entity entity : entities) {
            if ((entity instanceof org.bukkit.entity.ItemDisplay || entity instanceof org.bukkit.entity.TextDisplay)
                    && entity.getPersistentDataContainer().has(hologramKey, PersistentDataType.BYTE)) {
                entity.remove();
            }
        }
    }

    private void forceRemoveNearbyDisplays(Location loc) {
        if (loc.getWorld() == null) return;
        Location searchCenter = loc.clone().add(0.5, 1.5, 0.5);
        Collection<Entity> entities = loc.getWorld().getNearbyEntities(searchCenter, 2.0, 3.0, 2.0);
        for (Entity entity : entities) {
            if (entity instanceof org.bukkit.entity.ItemDisplay || entity instanceof org.bukkit.entity.TextDisplay) {
                entity.remove();
            }
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

        // 实体 spawn 与清理必须在该位置所在区域线程执行，兼容 Folia 多区域
        plugin.getFoliaLib().getImpl().runAtLocation(loc, task -> {
            // 先清理旧的全息图（直接清理，避免再次调度）
            removeNearbyHolograms(loc);
            forceRemoveNearbyDisplays(loc);
            activeItemHolograms.remove(loc);
            activeTextHolograms.remove(loc);
            itemIndices.remove(loc);

            if (!pool.getItems().isEmpty()) {
                ItemDisplay display = (ItemDisplay) itemLoc.getWorld().spawnEntity(itemLoc, EntityType.valueOf("ITEM_DISPLAY"));
                display.setItemStack(pool.getItems().get(0).getItem());
                display.setBillboard(ItemDisplay.Billboard.CENTER);
                display.getPersistentDataContainer().set(hologramKey, PersistentDataType.BYTE, (byte) 1);

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
                textDisplay.getPersistentDataContainer().set(hologramKey, PersistentDataType.BYTE, (byte) 1);

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

    public int globalCleanup(boolean force) {
        if (!isSupported) return 0;

        int removedCount = 0;
        int failedCount = 0;

        for (Entity entity : activeItemHolograms.values()) {
            if (entity != null) {
                try {
                    entity.remove();
                    removedCount++;
                } catch (Exception e) {
                    failedCount++;
                }
            }
        }
        for (Entity entity : activeTextHolograms.values()) {
            if (entity != null) {
                try {
                    entity.remove();
                    removedCount++;
                } catch (Exception e) {
                    failedCount++;
                }
            }
        }
        activeItemHolograms.clear();
        activeTextHolograms.clear();
        itemIndices.clear();

        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                boolean shouldRemove = false;

                if (entity.getPersistentDataContainer().has(hologramKey, PersistentDataType.BYTE)) {
                    shouldRemove = true;
                }

                if (force && (entity instanceof org.bukkit.entity.ItemDisplay || entity instanceof org.bukkit.entity.TextDisplay)) {
                    shouldRemove = true;
                }

                if (shouldRemove) {
                    try {
                        entity.remove();
                        removedCount++;
                    } catch (Exception e) {
                        failedCount++;
                    }
                }
            }
        }

        plugin.getLogger().info("已清理 " + removedCount + " 个全息图实体，失败 " + failedCount + " 个 (强制模式: " + force + ")。");
        return removedCount;
    }

    public void removeHologram(Location loc) {
        // 实体移除与附近实体查询必须在该位置所在区域线程执行，兼容 Folia 多区域
        plugin.getFoliaLib().getImpl().runAtLocation(loc, task -> {
            removeNearbyHolograms(loc);
            Entity itemEntity = activeItemHolograms.remove(loc);
            if (itemEntity != null && itemEntity.isValid()) {
                itemEntity.remove();
            }
            Entity textEntity = activeTextHolograms.remove(loc);
            if (textEntity != null && textEntity.isValid()) {
                textEntity.remove();
            }
            itemIndices.remove(loc);
            forceRemoveNearbyDisplays(loc);
        });
    }

    public void removeAllHolograms() {
        for (Entity entity : activeItemHolograms.values()) {
            if (entity != null && entity.isValid()) {
                try { entity.remove(); } catch (Exception ignored) {}
            }
        }
        for (Entity entity : activeTextHolograms.values()) {
            if (entity != null && entity.isValid()) {
                try { entity.remove(); } catch (Exception ignored) {}
            }
        }
        activeItemHolograms.clear();
        activeTextHolograms.clear();
        itemIndices.clear();
    }

    public void refreshPoolHolograms(String poolName) {
        // 内部的 removeHologram/createHologram 会各自调度到对应区域，这里只需遍历（读 ConcurrentHashMap 线程安全）
        Map<Location, String> cachedLocations = plugin.getManager().getCachedLocations();
        for (Map.Entry<Location, String> entry : cachedLocations.entrySet()) {
            if (entry.getValue().equals(poolName)) {
                removeHologram(entry.getKey());
                createHologram(entry.getKey(), poolName);
            }
        }
    }

    private void startUpdateTask() {
        updateTask = plugin.getFoliaLib().getImpl().runTimer(() -> {
            if (!plugin.isEnabled()) {
                if (updateTask != null) updateTask.cancel();
                return;
            }

            final int ticks = tickCounter.incrementAndGet();
            final float rotationSpeed = (float) plugin.getConfig().getDouble("hologram.rotation_speed", 2.0);
            final int cycleInterval = plugin.getConfig().getInt("hologram.cycle_interval", 3) * 20;
            final boolean checkPoolChanges = (ticks % 20 == 0);

            for (Map.Entry<Location, Entity> entry : activeItemHolograms.entrySet()) {
                Location loc = entry.getKey();
                Entity entity = entry.getValue();

                if (entity == null || entity.isDead()) {
                    // 实体已死亡，仅清理 Map（无需区域调度）
                    removeHologramFromMaps(loc);
                    continue;
                }

                if (!entity.isValid()) {
                    // 区块卸载等导致的暂时失效，跳过等待重建
                    continue;
                }

                // 实体操作调度到其所在区域线程，兼容 Folia 多区域
                plugin.getFoliaLib().getImpl().runAtLocation(loc, task -> {
                    if (!(entity instanceof ItemDisplay)) return;
                    ItemDisplay display = (ItemDisplay) entity;
                    try {
                        if (display.isDead() || !display.isValid()) return;

                        org.bukkit.util.Transformation transformation = display.getTransformation();
                        transformation.getLeftRotation().rotationY((float) Math.toRadians(ticks * rotationSpeed));
                        display.setTransformation(transformation);

                        if (ticks % cycleInterval == 0 || checkPoolChanges) {
                            String poolName = plugin.getManager().getCachedLocations().get(loc);
                            if (poolName != null) {
                                LotteryPool pool = plugin.getManager().getPool(poolName);
                                if (pool != null) {
                                    if (!pool.getItems().isEmpty() && (ticks % cycleInterval == 0)) {
                                        int nextIndex = (itemIndices.getOrDefault(loc, 0) + 1) % pool.getItems().size();
                                        display.setItemStack(pool.getItems().get(nextIndex).getItem());
                                        itemIndices.put(loc, nextIndex);
                                    }

                                    Entity textEntity = activeTextHolograms.get(loc);
                                    if (textEntity != null && textEntity.isValid() && checkPoolChanges) {
                                        updateHologramText((org.bukkit.entity.TextDisplay) textEntity, pool);
                                    }
                                } else {
                                    // 奖池已不存在，当前已在区域线程，直接清理
                                    removeHologramFromMaps(loc);
                                }
                            } else {
                                removeHologramFromMaps(loc);
                            }
                        }
                    } catch (Exception e) {
                        removeHologramFromMaps(loc);
                        if (display.isValid()) {
                            try { display.remove(); } catch (Exception ignored) {}
                        }
                    }
                });
            }
        }, 1L, 1L);
    }

    private void removeHologramFromMaps(Location loc) {
        Entity itemEntity = activeItemHolograms.remove(loc);
        if (itemEntity != null && itemEntity.isValid()) {
            try { itemEntity.remove(); } catch (Exception ignored) {}
        }
        Entity textEntity = activeTextHolograms.remove(loc);
        if (textEntity != null && textEntity.isValid()) {
            try { textEntity.remove(); } catch (Exception ignored) {}
        }
        itemIndices.remove(loc);
    }
}
