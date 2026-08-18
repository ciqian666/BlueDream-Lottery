package com.bluedream.lottery;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class AnimationEngine {
    private static final Set<UUID> activeAnimations = new HashSet<>();
    
    private final BlueDreamLottery plugin;
    private final Player player;
    private final LotteryPool pool;
    private final Random random = new Random();
    private LotteryItem finalResult;
    private java.util.List<LotteryItem> multiResults;
    private int count = 1;
    private String costType = "FREE";
    private double costValue = 0;

    public AnimationEngine(BlueDreamLottery plugin, Player player, LotteryPool pool) {
        this.plugin = plugin;
        this.player = player;
        this.pool = pool;
        this.multiResults = new java.util.ArrayList<>();
    }

    public void setCostInfo(String type, double value) {
        this.costType = type;
        this.costValue = value;
    }

    public static boolean isAnimating(UUID uuid) {
        return activeAnimations.contains(uuid);
    }

    public static void stopAnimating(UUID uuid) {
        activeAnimations.remove(uuid);
    }

    public void start(int count) {
        LanguageManager lm = plugin.getLanguageManager();
        this.count = count;
        if (activeAnimations.contains(player.getUniqueId())) {
            player.sendMessage(lm.getMessage("animation_in_progress"));
            return;
        }

        if (pool.getItems().isEmpty()) {
            player.sendMessage(lm.getMessage("pool_empty"));
            return;
        }

        UUID uuid = player.getUniqueId();
        int currentPity = plugin.getPlayerDataManager().getPity(uuid, pool.getName());
        int pityCount = pool.getPityCount();
        
        java.util.List<LotteryItem> grandPrizes = new java.util.ArrayList<>();
        for (LotteryItem li : pool.getItems()) {
            if (li.isGrandPrize()) {
                grandPrizes.add(li);
            }
        }

        for (int i = 0; i < count; i++) {
            LotteryItem res = null;
            if (!grandPrizes.isEmpty() && currentPity + 1 >= pityCount) {
                res = grandPrizes.get(random.nextInt(grandPrizes.size()));
                currentPity = 0;
            } else {
                res = doWeightSelection();
                if (res != null && res.isGrandPrize()) {
                    currentPity = 0;
                } else {
                    currentPity++;
                }
            }
            if (res != null) {
                multiResults.add(res);
                if (plugin.getStatisticsManager() != null) {
                    String drawName = (res.getCustomName() != null && !res.getCustomName().isEmpty()) 
                                ? Adapter.color(res.getCustomName()) 
                                : Adapter.getItemName(res.getItem());
                    
                    plugin.getStatisticsManager().logDraw(
                        player.getUniqueId(), 
                        player.getName(), 
                        pool.getName(), 
                        res.getItem().getType().name(),
                        drawName,
                        res.isGrandPrize(),
                        costType,
                        costValue / count
                    );
                }
            }
        }

        plugin.getPlayerDataManager().setPity(uuid, pool.getName(), currentPity);

        if (multiResults.isEmpty()) {
            player.sendMessage(lm.getMessage("no_result_generated"));
            return;
        }

        this.finalResult = multiResults.get(0);
        activeAnimations.add(player.getUniqueId());

        if (count > 1) {
            startSummaryAnimation();
        } else {
            switch (pool.getAnimationType()) {
                case "SLOT_MACHINE":
                case "老虎机":
                    startSlotAnimation();
                    break;
                case "STAR":
                case "星辰汇聚":
                    startStarAnimation();
                    break;
                case "STORM":
                case "颜色风暴":
                    startStormAnimation();
                    break;
                case "CIRCLE":
                case "幸运转盘":
                    startCircleAnimation();
                    break;
                case "PHANTOM":
                case "幻影序列":
                    startPhantomAnimation();
                    break;
                case "JUDGMENT":
                case "终极审判":
                    startJudgmentAnimation();
                    break;
                case "RIFT":
                case "维度裂隙":
                    startRiftAnimation();
                    break;
                case "DIVINE":
                case "神圣降临":
                    startDivineAnimation();
                    break;
                case "PULSE":
                case "赛博脉冲":
                    startPulseAnimation();
                    break;
                case "THUNDER":
                case "雷霆一击":
                    startThunderAnimation();
                    break;
                case "TIME_TRAVEL":
                case "时空穿梭":
                    startTimeTravelAnimation();
                    break;
                case "CROSS":
                case "幸运十字":
                    startCrossAnimation();
                    break;
                case "FIREWORK":
                case "烟花盛典":
                    startFireworkAnimation();
                    break;
                case "VR":
                case "悬浮(3D)":
                case "3D_DISPLAY":
                case "3D悬浮":
                    if (Adapter.isDisplaySupported()) {
                        start3DDisplayAnimation();
                    } else {
                        startShuffleAnimation();
                    }
                    break;
                case "BLACKHOLE":
                case "黑洞吞噬":
                    if (Adapter.isDisplaySupported()) {
                        start3DBlackHoleAnimation();
                    } else {
                        startShuffleAnimation();
                    }
                    break;
                case "HELIX":
                case "螺旋基因":
                    if (Adapter.isDisplaySupported()) {
                        start3DHelixAnimation();
                    } else {
                        startShuffleAnimation();
                    }
                    break;
                case "DECONSTRUCT":
                case "次元解构":
                    if (Adapter.isDisplaySupported()) {
                        start3DDeconstructAnimation();
                    } else {
                        startShuffleAnimation();
                    }
                    break;
                case "GALAXY":
                case "银河漩涡":
                    if (Adapter.isDisplaySupported()) {
                        start3DGalaxyAnimation();
                    } else {
                        startShuffleAnimation();
                    }
                    break;
                case "METEOR":
                case "星陨坠落":
                    if (Adapter.isDisplaySupported()) {
                        start3DMeteorAnimation();
                    } else {
                        startShuffleAnimation();
                    }
                    break;
                case "TESSERACT":
                case "四维超立方":
                    if (Adapter.isDisplaySupported()) {
                        start3DTesseractAnimation();
                    } else {
                        startShuffleAnimation();
                    }
                    break;
                case "SHUFFLE":
                case "随机滚动":
                default:
                    startShuffleAnimation();
                    break;
            }
        }
    }

    private void startSummaryAnimation() {
        LanguageManager lm = plugin.getLanguageManager();
        Inventory inv = Bukkit.createInventory(new LotteryHolder(pool.getName(), "ANIMATION"), 27, lm.getMessage("summary_title").replace("{name}", pool.getName()));
        player.openInventory(inv);

        final int[] ticks = {0};
        plugin.getFoliaLib().getImpl().runAtEntityTimer(player, task -> {
            if (ticks[0] >= multiResults.size()) {
                task.cancel();
                plugin.getFoliaLib().getImpl().runAtEntityLater(player, () -> {
                    activeAnimations.remove(player.getUniqueId());
                    finishBatchLottery(multiResults);
                }, 40L);
                return;
            }

            inv.setItem(ticks[0] + 8, multiResults.get(ticks[0]).getItem());
            Adapter.playSound(player, player.getLocation(), "ENTITY_EXPERIENCE_ORB_PICKUP", "ORB_PICKUP", 1f, 0.5f + (ticks[0] * 0.1f));
            ticks[0]++;
        }, 1L, 3L);
    }

    private void startPhantomAnimation() {
        LanguageManager lm = plugin.getLanguageManager();
        Inventory inv = Bukkit.createInventory(new LotteryHolder(pool.getName(), "ANIMATION"), 27, lm.getMessage("animation_title").replace("{name}", pool.getName()));
        player.openInventory(inv);

        final int[] ticks = {0};
        plugin.getFoliaLib().getImpl().runAtEntityTimer(player, task -> {
            if (ticks[0] > 40) {
                task.cancel();
                activeAnimations.remove(player.getUniqueId());
                finishLottery(finalResult);
                return;
            }

            inv.clear();
            int pos = ticks[0] % 9;
            for (int row = 0; row < 3; row++) {
                int slot = row * 9 + pos;
                inv.setItem(slot, pool.getItems().get(random.nextInt(pool.getItems().size())).getItem());
                if (pos > 0) inv.setItem(row * 9 + pos - 1, createItem(Adapter.getMaterial("GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE"), " ", 7));
            }

            if (ticks[0] > 30 && pos == 4) {
                inv.setItem(13, finalResult.getItem());
            }

            Adapter.playSound(player, player.getLocation(), "ENTITY_ENDERMAN_TELEPORT", "ENDERMAN_TELEPORT", 0.3f, 1.5f);
            ticks[0]++;
        }, 1L, 2L);
    }

    private void startJudgmentAnimation() {
        LanguageManager lm = plugin.getLanguageManager();
        Inventory inv = Bukkit.createInventory(new LotteryHolder(pool.getName(), "ANIMATION"), 27, lm.getMessage("animation_title").replace("{name}", pool.getName()));
        player.openInventory(inv);

        final int[] ticks = {0};
        plugin.getFoliaLib().getImpl().runAtEntityTimer(player, task -> {
            if (ticks[0] > 60) {
                task.cancel();
                activeAnimations.remove(player.getUniqueId());
                finishLottery(finalResult);
                return;
            }

            if (ticks[0] < 30) {
                ItemStack left = Adapter.getGlassPane(ticks[0] % 2 == 0 ? 14 : 15);
                ItemStack right = Adapter.getGlassPane(ticks[0] % 2 != 0 ? 14 : 15);
                for (int i = 0; i < 3; i++) {
                    inv.setItem(i * 9 + (ticks[0] / 3) % 4, left);
                    inv.setItem(i * 9 + 8 - (ticks[0] / 3) % 4, right);
                }
                Adapter.playSound(player, player.getLocation(), "BLOCK_IRON_DOOR_CLOSE", "DOOR_CLOSE", 0.5f, 1.0f);
            } else if (ticks[0] == 30) {
                inv.clear();
                inv.setItem(13, createItem(Adapter.getMaterial("OBSIDIAN", "OBSIDIAN"), lm.getMessage("hidden_result")));
                Adapter.playSound(player, player.getLocation(), "ENTITY_WITHER_SPAWN", "WITHER_SPAWN", 1.0f, 0.5f);
            } else if (ticks[0] > 50) {
                inv.setItem(13, finalResult.getItem());
                Adapter.playSound(player, player.getLocation(), "ENTITY_GENERIC_EXPLODE", "EXPLODE", 0.8f, 1.2f);
            }

            ticks[0]++;
        }, 1L, 2L);
    }

    private void startRiftAnimation() {
        LanguageManager lm = plugin.getLanguageManager();
        Inventory inv = Bukkit.createInventory(new LotteryHolder(pool.getName(), "ANIMATION"), 54, lm.getMessage("animation_title").replace("{name}", pool.getName()));
        player.openInventory(inv);

        final int[] ticks = {0};
        plugin.getFoliaLib().getImpl().runAtEntityTimer(player, task -> {
            if (ticks[0] > 50) {
                task.cancel();
                activeAnimations.remove(player.getUniqueId());
                finishLottery(finalResult);
                return;
            }

            inv.clear();
            for (int i = 0; i < 54; i++) {
                int row = i / 9;
                int col = i % 9;
                double dist = Math.sqrt(Math.pow(row - 2.5, 2) + Math.pow(col - 4, 2));

                if (dist > (ticks[0] / 5.0) % 5) {
                    int glassData = (ticks[0] + i) % 2 == 0 ? 10 : 15;
                    inv.setItem(i, createItem(Adapter.getMaterial("PURPLE_STAINED_GLASS_PANE", "STAINED_GLASS_PANE"), " ", glassData));
                }
            }

            if (ticks[0] > 30) {
                inv.setItem(22, ticks[0] > 45 ? finalResult.getItem() : createItem(Adapter.getMaterial("DRAGON_EGG", "DRAGON_EGG"), lm.getMessage("dimension_core")));
                if (ticks[0] == 31) Adapter.playSound(player, player.getLocation(), "ENTITY_ENDER_DRAGON_GROWL", "ENDERDRAGON_GROWL", 1.0f, 0.5f);
            }

            Adapter.playSound(player, player.getLocation(), "BLOCK_PORTAL_AMBIENT", "PORTAL_TRIGGER", 0.2f, 1.0f + (ticks[0] / 50.0f));
            ticks[0]++;
        }, 1L, 2L);
    }

    private void startDivineAnimation() {
        LanguageManager lm = plugin.getLanguageManager();
        Inventory inv = Bukkit.createInventory(new LotteryHolder(pool.getName(), "ANIMATION"), 54, lm.getMessage("animation_title").replace("{name}", pool.getName()));
        player.openInventory(inv);

        final int[] ticks = {0};
        plugin.getFoliaLib().getImpl().runAtEntityTimer(player, task -> {
            if (ticks[0] > 60) {
                task.cancel();
                activeAnimations.remove(player.getUniqueId());
                finishLottery(finalResult);
                return;
            }

            inv.clear();
            int offset = Math.min(4, ticks[0] / 5);
            for (int row = 0; row < 6; row++) {
                for (int col = 0; col < (4 - offset); col++) {
                    inv.setItem(row * 9 + col, createItem(Adapter.getMaterial("GOLD_BLOCK", "GOLD_BLOCK"), " "));
                    inv.setItem(row * 9 + (8 - col), createItem(Adapter.getMaterial("GOLD_BLOCK", "GOLD_BLOCK"), " "));
                }
            }

            if (ticks[0] > 20) {
                for (int row = 0; row < 6; row++) {
                    inv.setItem(row * 9 + 4, createItem(Adapter.getMaterial("YELLOW_STAINED_GLASS_PANE", "STAINED_GLASS_PANE"), lm.getMessage("holy_light"), 4));
                }

                int itemRow = (ticks[0] - 20) / 4;
                if (itemRow < 6) {
                    inv.setItem(itemRow * 9 + 4, ticks[0] > 50 ? finalResult.getItem() : pool.getItems().get(random.nextInt(pool.getItems().size())).getItem());
                } else {
                    inv.setItem(49, finalResult.getItem());
                }
            }

            if (ticks[0] % 10 == 0) Adapter.playSound(player, player.getLocation(), "BLOCK_BEACON_AMBIENT", "BEACON_AMBIENT", 1.0f, 1.5f);
            ticks[0]++;
        }, 1L, 2L);
    }

    private void startPulseAnimation() {
        LanguageManager lm = plugin.getLanguageManager();
        Inventory inv = Bukkit.createInventory(new LotteryHolder(pool.getName(), "ANIMATION"), 27, lm.getMessage("animation_title").replace("{name}", pool.getName()));
        player.openInventory(inv);

        final int[] ticks = {0};
        plugin.getFoliaLib().getImpl().runAtEntityTimer(player, task -> {
            if (ticks[0] > 40) {
                task.cancel();
                activeAnimations.remove(player.getUniqueId());
                finishLottery(finalResult);
                return;
            }

            int[][] neon = {
                {5, 2, 3, 9},
                {5, 2, 3, 9}
            };
            for (int i = 0; i < 27; i++) {
                if (i == 13) {
                    inv.setItem(i, ticks[0] > 35 ? finalResult.getItem() : pool.getItems().get(random.nextInt(pool.getItems().size())).getItem());
                } else {
                    int data = neon[0][(ticks[0] + i) % 4];
                    inv.setItem(i, createItem(Adapter.getMaterial("WHITE_STAINED_GLASS_PANE", "STAINED_GLASS_PANE"), lm.getMessage("pulse_label"), data));
                }
            }

            Adapter.playSound(player, player.getLocation(), "BLOCK_NOTE_BLOCK_PLING", "NOTE_PLING", 0.5f, 1.0f + (ticks[0] / 40.0f));
            ticks[0]++;
        }, 1L, 2L);
    }

    private void startStarAnimation() {
        LanguageManager lm = plugin.getLanguageManager();
        Inventory inv = Bukkit.createInventory(new LotteryHolder(pool.getName(), "ANIMATION"), 54, lm.getMessage("animation_title").replace("{name}", pool.getName()));
        player.openInventory(inv);

        final int[] edgeSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53};
        final int[] ticks = {0};
        plugin.getFoliaLib().getImpl().runAtEntityTimer(player, task -> {
            if (ticks[0] > 30) {
                task.cancel();
                activeAnimations.remove(player.getUniqueId());
                finishLottery(finalResult);
                return;
            }

            inv.clear();
            for (int slot : edgeSlots) {
                if (random.nextDouble() > 0.5) {
                    int[] dataOptions = {3, 0, 11};
                    inv.setItem(slot, createItem(Adapter.getMaterial("WHITE_STAINED_GLASS_PANE", "STAINED_GLASS_PANE"), lm.getMessage("star_symbol"), dataOptions[random.nextInt(dataOptions.length)]));
                }
            }

            if (ticks[0] > 10) {
                if (ticks[0] < 25) {
                    inv.setItem(22, pool.getItems().get(random.nextInt(pool.getItems().size())).getItem());
                } else {
                    inv.setItem(22, finalResult.getItem());
                }
            }

            Adapter.playSound(player, player.getLocation(), "BLOCK_AMETHYST_BLOCK_CHIME", "SUCCESS", 0.5f, 0.5f + (ticks[0] / 30f));
            ticks[0]++;
        }, 1L, 2L);
    }

    private void startStormAnimation() {
        LanguageManager lm = plugin.getLanguageManager();
        Inventory inv = Bukkit.createInventory(new LotteryHolder(pool.getName(), "ANIMATION"), 27, lm.getMessage("animation_title").replace("{name}", pool.getName()));
        player.openInventory(inv);

        final int[] ticks = {0};
        plugin.getFoliaLib().getImpl().runAtEntityTimer(player, task -> {
            if (ticks[0] > 40) {
                task.cancel();
                inv.clear();
                inv.setItem(13, finalResult.getItem());
                plugin.getFoliaLib().getImpl().runAtEntityLater(player, () -> {
                    activeAnimations.remove(player.getUniqueId());
                    finishLottery(finalResult);
                }, 20L);
                return;
            }

            for (int i = 0; i < 27; i++) {
                inv.setItem(i, Adapter.getGlassPane(random.nextInt(16)));
            }

            if (ticks[0] % 5 == 0) {
                Adapter.playSound(player, player.getLocation(), "ENTITY_LIGHTNING_BOLT_THUNDER", "AMBIENCE_THUNDER", 0.5f, 2.0f);
            }
            ticks[0]++;
        }, 1L, 2L);
    }

    private void startCircleAnimation() {
        LanguageManager lm = plugin.getLanguageManager();
        Inventory inv = Bukkit.createInventory(new LotteryHolder(pool.getName(), "ANIMATION"), 54, lm.getMessage("animation_title").replace("{name}", pool.getName()));
        player.openInventory(inv);

        final int[] circle = {12, 13, 14, 23, 32, 31, 30, 21};

        final int[] ticks = {0};
        final int[] currentPos = {0};
        plugin.getFoliaLib().getImpl().runAtEntityTimer(player, task -> {
            if (ticks[0] > 50) {
                task.cancel();
                activeAnimations.remove(player.getUniqueId());
                finishLottery(finalResult);
                return;
            }

            inv.clear();
            for (int i = 0; i < circle.length; i++) {
                if (i == currentPos[0] % circle.length) {
                    if (ticks[0] >= 49) {
                         inv.setItem(circle[i], finalResult.getItem());
                    } else {
                         inv.setItem(circle[i], pool.getItems().get(random.nextInt(pool.getItems().size())).getItem());
                    }
                } else {
                    inv.setItem(circle[i], Adapter.getGlassPane(7));
                }
            }

            Adapter.playSound(player, player.getLocation(), "BLOCK_NOTE_BLOCK_HAT", "NOTE_STICKS", 1f, 1f);
            currentPos[0]++;
            ticks[0]++;
        }, 1L, 2L);
    }

    private void startSlotAnimation() {
        LanguageManager lm = plugin.getLanguageManager();
        Inventory inv = Bukkit.createInventory(new LotteryHolder(pool.getName(), "ANIMATION"), 27, lm.getMessage("animation_title").replace("{name}", pool.getName()));
        player.openInventory(inv);

        ItemStack glass = Adapter.getGlassPane(0);
        ItemMeta meta = glass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            glass.setItemMeta(meta);
        }
        
        for (int i = 0; i < 27; i++) {
            if (i != 13 && i != 4 && i != 22) {
                inv.setItem(i, glass);
            }
        }

        inv.setItem(4, createItem(Adapter.getMaterial("HOPPER", "HOPPER"), lm.getMessage("result_indicator_down")));
        inv.setItem(22, createItem(Adapter.getMaterial("HOPPER", "HOPPER"), lm.getMessage("result_indicator_up")));

        final int[] ticks = {0};
        final int maxTicks = 40 + random.nextInt(20);
        final double[] speed = {1.0};
        plugin.getFoliaLib().getImpl().runAtEntityTimer(player, task -> {
            if (ticks[0] < maxTicks) {
                LotteryItem randomItem = pool.getItems().get(random.nextInt(pool.getItems().size()));
                ItemStack displayItem = randomItem.getItem();

                inv.setItem(13, displayItem);
                Adapter.playSound(player, player.getLocation(), "BLOCK_NOTE_BLOCK_XYLOPHONE", "NOTE_PLING", 1f, 1.5f + (ticks[0] / 100f));

                int glassData = (ticks[0] % 3 == 0) ? 1 : (ticks[0] % 3 == 1 ? 4 : 5);
                ItemStack colorGlass = Adapter.getGlassPane(glassData);
                ItemMeta gMeta = colorGlass.getItemMeta();
                if (gMeta != null) {
                    gMeta.setDisplayName(" ");
                    colorGlass.setItemMeta(gMeta);
                }
                inv.setItem(12, colorGlass);
                inv.setItem(14, colorGlass);

                ticks[0]++;
                if (ticks[0] > maxTicks * 0.7) speed[0] += 0.5;
            } else {
                task.cancel();
                inv.setItem(13, finalResult.getItem());
                inv.setItem(12, Adapter.getGlassPane(0));
                inv.setItem(14, Adapter.getGlassPane(0));

                Adapter.playSound(player, player.getLocation(), "UI_BUTTON_CLICK", "CLICK", 1f, 1f);
                plugin.getFoliaLib().getImpl().runAtEntityLater(player, () -> {
                    activeAnimations.remove(player.getUniqueId());
                    finishLottery(finalResult);
                }, 20L);
            }
        }, 1L, 2L);
    }

    private void start3DDeconstructAnimation() {
        player.closeInventory();
        Location center = player.getLocation().add(player.getLocation().getDirection().multiply(4)).add(0, 1.5, 0);
        List<Entity> parts = new ArrayList<>();
        int rows = 2;
        int cols = 2;
        int layers = 2;
        
        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < cols; y++) {
                for (int z = 0; z < layers; z++) {
                    Entity part = Adapter.spawnItemDisplay(center, pool.getItems().get(random.nextInt(pool.getItems().size())).getItem());
                    if (part != null) {
                        Adapter.setBrightness(part, 15, 15);
                        parts.add(part);
                    }
                }
            }
        }

        final int[] ticks = {0};
        plugin.getFoliaLib().getImpl().runAtLocationTimer(center, task -> {
            if (ticks[0] > 120 || !player.isOnline()) {
                task.cancel();
                parts.forEach(Entity::remove);
                activeAnimations.remove(player.getUniqueId());
                if (player.isOnline()) {
                    finishLottery(finalResult);
                    Adapter.spawnParticle(center, "FLASH", 1, 0, 0, 0, 0);
                    Adapter.playSound(player, center, "ENTITY_GENERIC_EXPLODE", "EXPLODE", 1.0f, 1.5f);
                }
                return;
            }

            if (ticks[0] == 1) {
                Adapter.playSound(player, center, "BLOCK_BEACON_ACTIVATE", "BEACON_ACTIVATE", 1.5f, 0.5f);
                for (int i = 0; i < parts.size(); i++) {
                    int x = (i / 4) % 2;
                    int y = (i / 2) % 2;
                    int z = i % 2;
                    org.bukkit.util.Vector targetPos = new org.bukkit.util.Vector(x - 0.5, y - 0.5, z - 0.5).multiply(4.0);
                    Adapter.setTransformation(parts.get(i), targetPos, new org.joml.Quaternionf().rotationXYZ(3.14f, 3.14f, 0), new org.bukkit.util.Vector(0.5, 0.5, 0.5), 40);
                }
            }

            if (ticks[0] == 45) {
                Adapter.playSound(player, center, "ITEM_CHORUS_FRUIT_TELEPORT", "CHORUS_TELEPORT", 1.0f, 1.0f);
                for (Entity e : parts) {
                    Adapter.setTransformation(e, new org.bukkit.util.Vector(0, 0, 0), new org.joml.Quaternionf().rotationXYZ(0, 0, 0), new org.bukkit.util.Vector(1.5, 1.5, 1.5), 40);
                    if (e instanceof org.bukkit.entity.ItemDisplay) {
                        ((org.bukkit.entity.ItemDisplay) e).setItemStack(finalResult.getItem());
                    }
                }
            }

            if (ticks[0] == 85) {
                Adapter.playSound(player, center, "ENTITY_ENDER_DRAGON_GROWL", "DRAGON_GROWL", 0.8f, 1.2f);
                for (int i = 0; i < parts.size(); i++) {
                    org.bukkit.util.Vector randomExit = new org.bukkit.util.Vector(random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5).multiply(10.0);
                    Adapter.setTransformation(parts.get(i), randomExit, new org.joml.Quaternionf().rotationXYZ(10, 10, 10), new org.bukkit.util.Vector(0, 0, 0), 30);
                }
            }

            if (ticks[0] < 80) {
                Adapter.spawnParticle(center, "REVERSE_PORTAL", 3, 0.5, 0.5, 0.5, 0.1);
                if (ticks[0] % 10 == 0) {
                    Adapter.spawnParticle(center, "END_ROD", 10, 2.0, 2.0, 2.0, 0.05);
                }
            }

            ticks[0]++;
        }, 1L, 1L);
    }

    private void start3DBlackHoleAnimation() {
        LanguageManager lm = plugin.getLanguageManager();
        player.closeInventory();
        Location center = player.getLocation().add(player.getLocation().getDirection().multiply(5)).add(0, 2.0, 0);
        List<Entity> displays = new ArrayList<>();
        int amount = 16;

        for (int i = 0; i < amount; i++) {
            Entity display = Adapter.spawnItemDisplay(center, pool.getItems().get(random.nextInt(pool.getItems().size())).getItem());
            if (display != null) displays.add(display);
        }

        final int[] ticks = {0};
        plugin.getFoliaLib().getImpl().runAtLocationTimer(center, task -> {
            if (ticks[0] > 100 || !player.isOnline()) {
                task.cancel();
                for (Entity e : displays) e.remove();
                activeAnimations.remove(player.getUniqueId());
                if (player.isOnline()) {
                    finishLottery(finalResult);
                    Adapter.spawnParticle(center, "EXPLOSION_HUGE", 2, 0.5, 0.5, 0.5, 0.1);
                    Adapter.playSound(player, center, "ENTITY_GENERIC_EXPLODE", "EXPLODE", 1.5f, 0.8f);
                }
                return;
            }

            for (int i = 0; i < displays.size(); i++) {
                Entity e = displays.get(i);
                double angle = (ticks[0] * 0.2) + (i * (2 * Math.PI / amount));
                double radius = 5.0 * (1.0 - (ticks[0] / 105.0));
                double yOffset = Math.sin(ticks[0] * 0.1 + i) * 1.0;

                Location loc = center.clone().add(Math.cos(angle) * radius, yOffset, Math.sin(angle) * radius);
                e.teleportAsync(loc);

                if (ticks[0] % 2 == 0) {
                    Adapter.spawnParticle(loc, "REVERSE_PORTAL", 1, 0.1, 0.1, 0.1, 0.05);
                }
            }

            if (ticks[0] % 4 == 0) {
                Adapter.spawnParticle(center, "SQUID_INK", 8, 0.5, 0.5, 0.5, 0.02);
                Adapter.playSound(player, center, "ENTITY_ENDERMAN_TELEPORT", "ENDERMAN_TELEPORT", 0.6f, 0.5f + (ticks[0] / 100.0f));
            }

            if (ticks[0] == 85) {
                for (Entity e : displays) {
                    if (e instanceof org.bukkit.entity.ItemDisplay) {
                        ((org.bukkit.entity.ItemDisplay) e).setItemStack(finalResult.getItem());
                    }
                }
                Adapter.playSound(player, center, "ENTITY_WITHER_SPAWN", "WITHER_SPAWN", 1.2f, 0.5f);
            }

            ticks[0]++;
        }, 1L, 1L);
    }

    private void start3DHelixAnimation() {
        player.closeInventory();
        Location center = player.getLocation().add(player.getLocation().getDirection().multiply(4.5)).add(0, 0.5, 0);
        List<Entity> strand1 = new ArrayList<>();
        List<Entity> strand2 = new ArrayList<>();
        int amount = 10;

        for (int i = 0; i < amount; i++) {
            Entity e1 = Adapter.spawnItemDisplay(center, pool.getItems().get(random.nextInt(pool.getItems().size())).getItem());
            Entity e2 = Adapter.spawnItemDisplay(center, pool.getItems().get(random.nextInt(pool.getItems().size())).getItem());
            if (e1 != null) strand1.add(e1);
            if (e2 != null) strand2.add(e2);
        }

        final int[] ticks = {0};
        plugin.getFoliaLib().getImpl().runAtLocationTimer(center, task -> {
            if (ticks[0] > 90 || !player.isOnline()) {
                task.cancel();
                strand1.forEach(Entity::remove);
                strand2.forEach(Entity::remove);
                activeAnimations.remove(player.getUniqueId());
                if (player.isOnline()) {
                    finishLottery(finalResult);
                    Adapter.spawnParticle(center.clone().add(0, 2.5, 0), "TOTEM", 50, 0.8, 0.8, 0.8, 0.2);
                    Adapter.playSound(player, center, "ENTITY_PLAYER_LEVELUP", "LEVEL_UP", 1.2f, 1.2f);
                }
                return;
            }

            double heightStep = 3.5 / amount;
            double maxRadius = 2.0;
            for (int i = 0; i < amount; i++) {
                double angle = ticks[0] * 0.2 + (i * (Math.PI * 2 / amount));
                double y = i * heightStep;
                double currentRadius = Math.sin(ticks[0] * 0.05 + i * 0.5) * 0.5 + maxRadius;

                if (i < strand1.size()) {
                    Location loc1 = center.clone().add(Math.cos(angle) * currentRadius, y, Math.sin(angle) * currentRadius);
                    strand1.get(i).teleportAsync(loc1);
                    Adapter.spawnParticle(loc1, "COLOURED_DUST", 2, 0.1, 0.1, 0.1, 0);
                }

                if (i < strand2.size()) {
                    Location loc2 = center.clone().add(Math.cos(angle + Math.PI) * currentRadius, y, Math.sin(angle + Math.PI) * currentRadius);
                    strand2.get(i).teleportAsync(loc2);
                    Adapter.spawnParticle(loc2, "COLOURED_DUST", 2, 0.1, 0.1, 0.1, 0);
                }
            }

            if (ticks[0] % 5 == 0 && ticks[0] < 70) {
                Adapter.playSound(player, center, "BLOCK_AMETHYST_BLOCK_CHIME", "AMETHYST_CHIME", 0.7f, 0.8f + (ticks[0] / 90.0f));
            }

            if (ticks[0] == 80) {
                center.add(0, 1.5, 0);
                strand1.forEach(e -> {
                    if (e instanceof org.bukkit.entity.ItemDisplay) ((org.bukkit.entity.ItemDisplay) e).setItemStack(finalResult.getItem());
                });
                strand2.forEach(e -> {
                    if (e instanceof org.bukkit.entity.ItemDisplay) ((org.bukkit.entity.ItemDisplay) e).setItemStack(finalResult.getItem());
                });
            }

            ticks[0]++;
        }, 1L, 1L);
    }

    private void start3DDisplayAnimation() {
        LanguageManager lm = plugin.getLanguageManager();
        player.closeInventory();
        Location center = player.getLocation().add(player.getLocation().getDirection().multiply(4)).add(0, 2.0, 0);
        List<Entity> displays = new ArrayList<>();
        int amount = 12;
        double radius = 2.5;

        for (int i = 0; i < amount; i++) {
            double angle = i * (2 * Math.PI / amount);
            Location loc = center.clone().add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
            Entity display = Adapter.spawnItemDisplay(loc, pool.getItems().get(random.nextInt(pool.getItems().size())).getItem());
            if (display != null) {
                displays.add(display);
            }
        }

        final int[] ticks = {0};
        final double[] currentRadius = {radius};
        plugin.getFoliaLib().getImpl().runAtLocationTimer(center, task -> {
             if (ticks[0] > 80 || !player.isOnline()) {
                 task.cancel();
                 for (Entity e : displays) e.remove();
                 activeAnimations.remove(player.getUniqueId());

                 if (player.isOnline()) {
                    finishLottery(finalResult);
                    Location finalLoc = player.getLocation().add(0, 1.5, 0);
                    Adapter.spawnParticle(finalLoc, "FIREWORKS_SPARK", 40, 1.0, 1.0, 1.0, 0.1);
                    Adapter.playSound(player, finalLoc, "ENTITY_FIREWORK_ROCKET_BLAST", "FIREWORK_BLAST", 1.2f, 1.0f);
                 }
                 return;
             }

            double angleOffset = ticks[0] * 0.25;
            if (ticks[0] > 50) {
                currentRadius[0] -= radius / 25.0;
                if (currentRadius[0] < 0.1) currentRadius[0] = 0.1;
            }

            for (int i = 0; i < displays.size(); i++) {
                Entity e = displays.get(i);
                if (!e.isValid()) continue;

                double angle = i * (2 * Math.PI / displays.size()) + angleOffset;
                Location loc = center.clone().add(Math.cos(angle) * currentRadius[0], Math.sin(ticks[0] * 0.15) * 0.5, Math.sin(angle) * currentRadius[0]);

                loc.setDirection(center.toVector().subtract(loc.toVector()));
                e.teleportAsync(loc);

                if (ticks[0] % 3 == 0 && ticks[0] < 60) {
                    if (e instanceof org.bukkit.entity.ItemDisplay) {
                        ((org.bukkit.entity.ItemDisplay) e).setItemStack(pool.getItems().get(random.nextInt(pool.getItems().size())).getItem());
                    }
                }

                Adapter.spawnParticle(loc, "END_ROD", 2, 0.1, 0.1, 0.1, 0.01);
            }

            if (ticks[0] == 65) {
                for (int i = 1; i < displays.size(); i++) {
                    displays.get(i).remove();
                }
                Entity last = displays.get(0);
                if (last instanceof org.bukkit.entity.ItemDisplay) {
                    ((org.bukkit.entity.ItemDisplay) last).setItemStack(finalResult.getItem());
                }
                Adapter.playSound(player, center, "ENTITY_ZOMBIE_VILLAGER_CONVERTED", "ZOMBIE_REMEDY", 1.2f, 1.2f);
            }

            if (ticks[0] % 5 == 0) {
                Adapter.playSound(player, center, "BLOCK_NOTE_BLOCK_CHIME", "NOTE_PLING", 0.6f, 0.5f + (ticks[0] / 80.0f));
            }

            ticks[0]++;
        }, 1L, 1L);
    }

    private void start3DGalaxyAnimation() {
        player.closeInventory();
        Location center = player.getLocation().add(player.getLocation().getDirection().multiply(4)).add(0, 2.0, 0);
        int arms = 3;
        int perArm = 8;
        List<Entity> stars = new ArrayList<>();
        for (int arm = 0; arm < arms; arm++) {
            for (int i = 0; i < perArm; i++) {
                Entity e = Adapter.spawnItemDisplay(center, pool.getItems().get(random.nextInt(pool.getItems().size())).getItem());
                if (e != null) {
                    Adapter.setBrightness(e, 15, 15);
                    stars.add(e);
                }
            }
        }

        final int[] ticks = {0};
        plugin.getFoliaLib().getImpl().runAtLocationTimer(center, task -> {
            if (ticks[0] > 90 || !player.isOnline()) {
                task.cancel();
                for (Entity e : stars) e.remove();
                activeAnimations.remove(player.getUniqueId());
                if (player.isOnline()) {
                    finishLottery(finalResult);
                    Adapter.spawnParticle(center, "FLASH", 3, 0.2, 0.2, 0.2, 0);
                    Adapter.spawnParticle(center, "REVERSE_PORTAL", 30, 1.2, 1.2, 1.2, 0.1);
                    Adapter.playSound(player, center, "ENTITY_GENERIC_EXPLODE", "EXPLODE", 1.2f, 1.0f);
                }
                return;
            }

            // 0-70: spinning spiral galaxy; 70-90: supernova collapse
            double spin = ticks[0] * 0.18;
            double converge = ticks[0] <= 70 ? 0.0 : Math.min(1.0, (ticks[0] - 70) / 15.0);

            for (int idx = 0; idx < stars.size(); idx++) {
                Entity e = stars.get(idx);
                if (!e.isValid()) continue;
                int arm = idx / perArm;
                int i = idx % perArm;
                double armAngle = arm * (2 * Math.PI / arms);
                double r = 0.7 + i * 0.35;
                double angle = spin + armAngle + (i * 0.4);
                double targetR = r * (1.0 - converge * 0.95);
                Location loc = center.clone().add(
                        Math.cos(angle) * targetR,
                        Math.sin(angle * 2 + i) * (0.4 - converge * 0.35),
                        Math.sin(angle) * targetR);
                e.teleportAsync(loc);
                Adapter.spawnParticle(loc, "END_ROD", 1, 0.05, 0.05, 0.05, 0);
            }

            Adapter.spawnParticle(center, "DRAGON_BREATH", 1, 0.6, 0.6, 0.6, 0.01);
            if (ticks[0] % 5 == 0) {
                Adapter.spawnParticle(center, "REVERSE_PORTAL", 6, 1.5, 0.4, 1.5, 0.05);
            }
            if (ticks[0] % 6 == 0 && ticks[0] < 75) {
                Adapter.playSound(player, center, "BLOCK_BEACON_AMBIENT", "BEACON_AMBIENT", 0.7f, 0.6f + (ticks[0] / 110.0f));
            }
            if (ticks[0] == 75) {
                Adapter.playSound(player, center, "ENTITY_WITHER_SPAWN", "WITHER_SPAWN", 1.4f, 0.5f);
            }
            if (ticks[0] >= 82) {
                for (Entity e : stars) {
                    if (e instanceof org.bukkit.entity.ItemDisplay) {
                        ((org.bukkit.entity.ItemDisplay) e).setItemStack(finalResult.getItem());
                    }
                }
            }
            ticks[0]++;
        }, 1L, 1L);
    }

    private void start3DMeteorAnimation() {
        player.closeInventory();
        Location center = player.getLocation().add(player.getLocation().getDirection().multiply(3.5)).add(0, 1.5, 0);
        int amount = 12;
        List<Entity> meteors = new ArrayList<>();
        List<double[]> targets = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            double ang = i * (2 * Math.PI / amount) + (random.nextDouble() * 0.4);
            double[] t = new double[]{Math.cos(ang) * 1.8, Math.sin(ang) * 1.8};
            Location spawn = center.clone().add(
                    t[0] + (random.nextDouble() - 0.5),
                    10 + random.nextDouble() * 2,
                    t[1] + (random.nextDouble() - 0.5));
            Entity e = Adapter.spawnItemDisplay(spawn, pool.getItems().get(random.nextInt(pool.getItems().size())).getItem());
            if (e != null) {
                Adapter.setBrightness(e, 15, 0);
                meteors.add(e);
                targets.add(t);
            }
        }

        final int[] ticks = {0};
        plugin.getFoliaLib().getImpl().runAtLocationTimer(center, task -> {
            if (ticks[0] > 90 || !player.isOnline()) {
                task.cancel();
                for (Entity e : meteors) e.remove();
                activeAnimations.remove(player.getUniqueId());
                if (player.isOnline()) {
                    finishLottery(finalResult);
                    Adapter.spawnParticle(center, "FIREWORKS_SPARK", 80, 1.5, 1.5, 1.5, 0.1);
                    Adapter.spawnParticle(center, "EXPLOSION_HUGE", 2, 0.3, 0.3, 0.3, 0);
                    Adapter.playSound(player, center, "ENTITY_FIREWORK_ROCKET_LARGE_BLAST", "FIREWORK_LARGE_BLAST", 1.4f, 0.9f);
                }
                return;
            }

            for (int i = 0; i < meteors.size(); i++) {
                Entity e = meteors.get(i);
                if (!e.isValid()) continue;
                double[] t = targets.get(i);
                int tTicks = ticks[0] - (i * 3);
                if (tTicks < 0) continue;

                double fall = Math.min(1.0, tTicks / 38.0);
                Location loc = center.clone().add(t[0], 11.0 * (1.0 - fall), t[1]);
                e.teleportAsync(loc);
                Adapter.spawnParticle(loc, "FLAME", 2, 0.15, 0.15, 0.15, 0.01);
                if (tTicks == 38) {
                    Adapter.spawnParticle(loc, "EXPLOSION_LARGE", 1, 0.2, 0.2, 0.2, 0);
                    Adapter.playSound(player, loc, "ENTITY_GENERIC_EXPLODE", "EXPLODE", 0.8f, 1.0f + (random.nextFloat() * 0.3f));
                }
            }

            if (ticks[0] % 20 == 0 && ticks[0] < 70) {
                Adapter.playSound(player, center, "ENTITY_LIGHTNING_BOLT_THUNDER", "AMBIENCE_THUNDER", 0.5f, 0.6f);
            }

            if (ticks[0] == 84) {
                for (Entity e : meteors) {
                    if (e instanceof org.bukkit.entity.ItemDisplay) {
                        ((org.bukkit.entity.ItemDisplay) e).setItemStack(finalResult.getItem());
                    }
                }
                Adapter.playSound(player, center, "ENTITY_PLAYER_LEVELUP", "LEVEL_UP", 1.2f, 1.1f);
            }
            ticks[0]++;
        }, 1L, 1L);
    }

    private void start3DTesseractAnimation() {
        player.closeInventory();
        Location center = player.getLocation().add(player.getLocation().getDirection().multiply(4)).add(0, 2.0, 0);
        int amount = 16;
        List<Entity> nodes = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            Entity e = Adapter.spawnItemDisplay(center, pool.getItems().get(random.nextInt(pool.getItems().size())).getItem());
            if (e != null) {
                Adapter.setBrightness(e, 15, 15);
                nodes.add(e);
            }
        }

        final int[] ticks = {0};
        plugin.getFoliaLib().getImpl().runAtLocationTimer(center, task -> {
            if (ticks[0] > 100 || !player.isOnline()) {
                task.cancel();
                for (Entity e : nodes) e.remove();
                activeAnimations.remove(player.getUniqueId());
                if (player.isOnline()) {
                    finishLottery(finalResult);
                    Adapter.spawnParticle(center, "FLASH", 4, 0.3, 0.3, 0.3, 0);
                    Adapter.spawnParticle(center, "REVERSE_PORTAL", 40, 1.2, 1.2, 1.2, 0.1);
                    Adapter.playSound(player, center, "ENTITY_ELDER_GUARDIAN_CURSE", "MOB_ELDER_GUARDIAN_CURSE", 1.3f, 0.6f);
                }
                return;
            }

            double scale = 2.6;
            if (ticks[0] > 72) {
                scale = 2.6 * Math.max(0.1, 1.0 - (ticks[0] - 72) / 22.0);
            }
            double rotY = ticks[0] * 0.06;
            double rotX = ticks[0] * 0.04;

            for (int i = 0; i < nodes.size(); i++) {
                Entity e = nodes.get(i);
                if (!e.isValid()) continue;
                int isInner = i / 8;   // 0 = outer cube, 1 = inner cube
                int v = i % 8;         // 8 vertices of a cube
                double x = ((v & 1) == 0 ? -1 : 1) * (isInner == 1 ? 0.5 : 1.0);
                double y = ((v & 2) == 0 ? -1 : 1) * (isInner == 1 ? 0.5 : 1.0);
                double z = ((v & 4) == 0 ? -1 : 1) * (isInner == 1 ? 0.5 : 1.0);

                // rotate around Y
                double cy = y;
                double cx1 = x * Math.cos(rotY) + z * Math.sin(rotY);
                double cz1 = -x * Math.sin(rotY) + z * Math.cos(rotY);
                // rotate around X
                double cy2 = cy * Math.cos(rotX) - cz1 * Math.sin(rotX);
                double cz2 = cy * Math.sin(rotX) + cz1 * Math.cos(rotX);

                Location loc = center.clone().add(cx1 * scale, cy2 * scale, cz2 * scale);
                e.teleportAsync(loc);
                Adapter.spawnParticle(loc, "END_ROD", 1, 0.02, 0.02, 0.02, 0);
            }

            if (ticks[0] % 4 == 0) {
                Adapter.spawnParticle(center, "ENCHANTMENT_TABLE", 12, 1.5, 1.5, 1.5, 0.1);
            }
            if (ticks[0] % 8 == 0 && ticks[0] < 75) {
                Adapter.playSound(player, center, "BLOCK_RESPAWN_ANCHOR_AMBIENT", "RESPAWN_ANCHOR_AMBIENT", 0.7f, 0.8f + (ticks[0] / 120.0f));
            }
            if (ticks[0] == 80) {
                Adapter.playSound(player, center, "ENTITY_ENDER_DRAGON_GROWL", "ENDERDRAGON_GROWL", 1.0f, 0.4f);
            }
            if (ticks[0] >= 90) {
                for (Entity e : nodes) {
                    if (e instanceof org.bukkit.entity.ItemDisplay) {
                        ((org.bukkit.entity.ItemDisplay) e).setItemStack(finalResult.getItem());
                    }
                }
            }
            ticks[0]++;
        }, 1L, 1L);
    }

    private void startShuffleAnimation() {
        LanguageManager lm = plugin.getLanguageManager();
        Inventory inv = Bukkit.createInventory(new LotteryHolder(pool.getName(), "ANIMATION"), 27, lm.getMessage("animation_title").replace("{name}", pool.getName()));
        player.openInventory(inv);

        final int[] count = {0};
        plugin.getFoliaLib().getImpl().runAtEntityTimer(player, task -> {
            if (count[0] >= 15) {
                task.cancel();
                activeAnimations.remove(player.getUniqueId());
                finishLottery(finalResult);
                return;
            }

            for (int i = 0; i < 27; i++) {
                if (count[0] >= 14 && i == 13) {
                    inv.setItem(i, finalResult.getItem());
                } else {
                    inv.setItem(i, pool.getItems().get(random.nextInt(pool.getItems().size())).getItem());
                }
            }
            Adapter.playSound(player, player.getLocation(), "UI_BUTTON_CLICK", "CLICK", 1f, 1f);
            count[0]++;
        }, 1L, 3L);
    }

    private LotteryItem doWeightSelection() {
        if (pool.getItems().isEmpty()) return null;
        
        double totalWeight = 0;
        for (LotteryItem item : pool.getItems()) {
            totalWeight += item.getChance();
        }

        double r = random.nextDouble() * totalWeight;
        double current = 0;
        for (LotteryItem item : pool.getItems()) {
            current += item.getChance();
            if (r <= current) {
                return item;
            }
        }
        
        return pool.getItems().get(0);
    }

    private void finishLottery(LotteryItem result) {
        java.util.List<LotteryItem> results = new java.util.ArrayList<>();
        results.add(result);
        finishBatchLottery(results);
    }

    private void finishBatchLottery(java.util.List<LotteryItem> results) {
        // 结算涉及命令派发和全局广播，Folia 要求在 global tick thread 执行
        plugin.getFoliaLib().getImpl().runNextTick(task -> {
        LanguageManager lm = plugin.getLanguageManager();

        for (LotteryItem result : results) {
            // 配置了命令奖励的物品不再发放本体，仅执行命令
            if (!result.getCommands().isEmpty()) {
                for (String cmd : result.getCommands()) {
                    String finalCmd = cmd.replace("%player%", player.getName()).replace("{player}", player.getName());
                    if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                        finalCmd = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, finalCmd);
                    }
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
                }
                continue;
            }
            ItemStack item = result.getItem();
            java.util.Map<Integer, ItemStack> overFlow = player.getInventory().addItem(item.clone());
            if (!overFlow.isEmpty()) {
                for (ItemStack left : overFlow.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), left);
                }
                player.sendMessage(lm.getMessage("inventory_full"));
            }
        }

        boolean hasGrandPrize = results.stream().anyMatch(LotteryItem::isGrandPrize);
        
        if (hasGrandPrize) {
            for (LotteryItem result : results) {
                if (result.isGrandPrize()) {
                    ItemStack item = result.getItem();
                    String displayName = getDisplayName(result, item);
                    
                    java.util.Map<String, String> placeholders = new java.util.HashMap<>();
                    placeholders.put("player", player.getName());
                    placeholders.put("pool", pool.getName());
                    placeholders.put("item", displayName);
                    Bukkit.broadcastMessage(lm.getMessage("broadcast_grand_prize", placeholders));
                    Adapter.playSound(player, player.getLocation(), "ENTITY_PLAYER_LEVELUP", "LEVEL_UP", 1.0f, 1.0f);
                    break; 
                }
            }
        } else {
            java.util.List<String> displayItems = new java.util.ArrayList<>();
            
            for (LotteryItem result : results) {
                if (result.isDisplayMessage()) {
                    ItemStack item = result.getItem();
                    String displayName = getDisplayName(result, item);
                    displayItems.add(displayName);
                }
            }
            
            if (!displayItems.isEmpty()) {
                String itemsStr = String.join("§f, §e", displayItems);
                java.util.Map<String, String> placeholders = new java.util.HashMap<>();
                placeholders.put("player", player.getName());
                placeholders.put("pool", pool.getName());
                placeholders.put("item", itemsStr);
                placeholders.put("count", String.valueOf(results.size()));

                if (results.size() > 1) {
                    Bukkit.broadcastMessage(lm.getMessage("get_items_batch", placeholders));
                } else {
                    Bukkit.broadcastMessage(lm.getMessage("get_item", placeholders));
                }
            }
        }
        });
    }

    private String getDisplayName(LotteryItem result, ItemStack item) {
        if (result.getCustomName() != null && !result.getCustomName().isEmpty()) {
            return Adapter.color(result.getCustomName());
        } else if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        } else {
            return Adapter.getItemName(item);
        }
    }

    private void startThunderAnimation() {
        LanguageManager lm = plugin.getLanguageManager();
        Inventory inv = Bukkit.createInventory(new LotteryHolder(pool.getName(), "ANIMATION"), 27, lm.getMessage("animation_title").replace("{name}", pool.getName()));
        player.openInventory(inv);

        final int[] ticks = {0};
        plugin.getFoliaLib().getImpl().runAtEntityTimer(player, task -> {
            if (ticks[0] > 40) {
                task.cancel();
                activeAnimations.remove(player.getUniqueId());
                finishLottery(finalResult);
                return;
            }

            inv.clear();
            if (ticks[0] < 30) {
                int glassData = ticks[0] % 2 == 0 ? 0 : 3;
                for (int i = 0; i < 27; i++) {
                    if (random.nextDouble() > 0.6) inv.setItem(i, createItem(Adapter.getMaterial("WHITE_STAINED_GLASS_PANE", "STAINED_GLASS_PANE"), lm.getMessage("thunder_label"), glassData));
                }
                if (ticks[0] % 5 == 0) Adapter.playSound(player, player.getLocation(), "ENTITY_LIGHTNING_BOLT_THUNDER", "AMBIENCE_THUNDER", 0.5f, 1.5f);
            } else if (ticks[0] < 38) {
                inv.setItem(13, createItem(Adapter.getMaterial("END_CRYSTAL", "EYE_OF_ENDER"), plugin.getLanguageManager().getMessage("hit_label")));
                Adapter.playSound(player, player.getLocation(), "ENTITY_LIGHTNING_BOLT_IMPACT", "EXPLODE", 1.0f, 1.0f);
            } else {
                inv.setItem(13, finalResult.getItem());
            }
            ticks[0]++;
        }, 1L, 2L);
    }

    private void startTimeTravelAnimation() {
        LanguageManager lm = plugin.getLanguageManager();
        Inventory inv = Bukkit.createInventory(new LotteryHolder(pool.getName(), "ANIMATION"), 27, lm.getMessage("animation_title").replace("{name}", pool.getName()));
        player.openInventory(inv);

        final int[] spiral = {0, 1, 2, 3, 4, 5, 6, 7, 8, 17, 26, 25, 24, 23, 22, 21, 20, 19, 18, 9, 10, 11, 12, 13};
        final int[] ticks = {0};
        plugin.getFoliaLib().getImpl().runAtEntityTimer(player, task -> {
            if (ticks[0] >= spiral.length + 10) {
                task.cancel();
                activeAnimations.remove(player.getUniqueId());
                finishLottery(finalResult);
                return;
            }

            inv.clear();
            if (ticks[0] < 30) {
                int glassData = (ticks[0] % 4 == 0) ? 11 : (ticks[0] % 4 == 1 ? 3 : (ticks[0] % 4 == 2 ? 9 : 15));
                for (int i = 0; i < 27; i++) {
                    if (i % 2 == ticks[0] % 2) inv.setItem(i, createItem(Adapter.getMaterial("WHITE_STAINED_GLASS_PANE", "STAINED_GLASS_PANE"), lm.getMessage("time_fragment"), glassData));
                }
                Adapter.playSound(player, player.getLocation(), "BLOCK_NOTE_BLOCK_CHIME", "NOTE_PLING", 0.5f, 0.5f + (ticks[0] / 30f));
            } else {
                inv.setItem(13, finalResult.getItem());
                if (ticks[0] == 30) Adapter.playSound(player, player.getLocation(), "ENTITY_ZOMBIE_VILLAGER_CONVERTED", "LEVEL_UP", 1.0f, 0.8f);
            }
            ticks[0]++;
        }, 1L, 2L);
    }

    private void startCrossAnimation() {
        LanguageManager lm = plugin.getLanguageManager();
        Inventory inv = Bukkit.createInventory(new LotteryHolder(pool.getName(), "ANIMATION"), 27, lm.getMessage("animation_title").replace("{name}", pool.getName()));
        player.openInventory(inv);

        final int[] ticks = {0};
        plugin.getFoliaLib().getImpl().runAtEntityTimer(player, task -> {
            if (ticks[0] > 40) {
                task.cancel();
                activeAnimations.remove(player.getUniqueId());
                finishLottery(finalResult);
                return;
            }

            inv.clear();
            int offset = (ticks[0] % 5);
            inv.setItem(9 + offset, pool.getItems().get(random.nextInt(pool.getItems().size())).getItem());
            inv.setItem(17 - offset, pool.getItems().get(random.nextInt(pool.getItems().size())).getItem());
            inv.setItem(4 + (offset * 9) % 27, pool.getItems().get(random.nextInt(pool.getItems().size())).getItem());
            inv.setItem(22 - (offset * 9) % 27, pool.getItems().get(random.nextInt(pool.getItems().size())).getItem());

            if (ticks[0] > 30) {
                inv.setItem(13, finalResult.getItem());
            }

            Adapter.playSound(player, player.getLocation(), "BLOCK_NOTE_BLOCK_CHIME", "NOTE_PLING", 0.6f, 1.0f + (ticks[0] / 40.0f));
            ticks[0]++;
        }, 1L, 2L);
    }

    private void startFireworkAnimation() {
        LanguageManager lm = plugin.getLanguageManager();
        Inventory inv = Bukkit.createInventory(new LotteryHolder(pool.getName(), "ANIMATION"), 27, lm.getMessage("animation_title").replace("{name}", pool.getName()));
        player.openInventory(inv);

        final int[] ticks = {0};
        plugin.getFoliaLib().getImpl().runAtEntityTimer(player, task -> {
            if (ticks[0] > 45) {
                task.cancel();
                activeAnimations.remove(player.getUniqueId());
                finishLottery(finalResult);
                return;
            }

            if (ticks[0] < 35) {
                inv.clear();
                int[][] colorData = {
                    {14, 4, 1}
                };
                for (int i = 0; i < 3; i++) {
                    int rSlot = random.nextInt(27);
                    if (random.nextBoolean()) {
                        inv.setItem(rSlot, createItem(Adapter.getMaterial("FIREWORK_STAR", "FIREWORK_CHARGE"), lm.getMessage("firework_bloom")));
                    } else {
                        int data = colorData[0][random.nextInt(3)];
                        inv.setItem(rSlot, createItem(Adapter.getMaterial("WHITE_STAINED_GLASS_PANE", "STAINED_GLASS_PANE"), lm.getMessage("firework_bloom"), data));
                    }
                }
                Adapter.playSound(player, player.getLocation(), "ENTITY_FIREWORK_ROCKET_BLAST", "FIREWORK_BLAST", 0.5f, 1.2f);
            } else {
                inv.setItem(13, finalResult.getItem());
                if (ticks[0] == 35) Adapter.playSound(player, player.getLocation(), "ENTITY_FIREWORK_ROCKET_LARGE_BLAST", "FIREWORK_LARGE_BLAST", 1.0f, 1.0f);
            }
            ticks[0]++;
        }, 1L, 2L);
    }

    private ItemStack createItem(Material material, String name) {
        return createItem(material, name, 0);
    }

    private ItemStack createItem(Material material, String name, int data) {
        ItemStack item = Adapter.createItemStack(material.name(), material.name(), data);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }
}
