package com.bluedream.lottery;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class Adapter {

    private static final boolean IS_FLATTENING;
    private static final Map<String, Material> MATERIAL_CACHE = new HashMap<>();
    private static final Map<String, Sound> SOUND_CACHE = new HashMap<>();

    static {
        boolean flattening = false;
        try {
            Material.valueOf("WHITE_STAINED_GLASS_PANE");
            flattening = true;
        } catch (Exception ignored) {}
        IS_FLATTENING = flattening;
    }

    public static Material getMaterial(String modernName, String legacyName) {
        String key = IS_FLATTENING ? modernName : legacyName;
        if (MATERIAL_CACHE.containsKey(key)) return MATERIAL_CACHE.get(key);

        Material mat = null;
        try {
            mat = Material.valueOf(key);
        } catch (Exception e) {
            try {
                mat = Material.valueOf(legacyName);
            } catch (Exception ignored) {}
        }

        if (mat == null) mat = Material.STONE;
        MATERIAL_CACHE.put(key, mat);
        return mat;
    }

    public static Sound getSound(String modernName, String legacyName) {
        String key = IS_FLATTENING ? modernName : legacyName;
        if (SOUND_CACHE.containsKey(key)) return SOUND_CACHE.get(key);

        Sound sound = null;
        try {
            sound = Sound.valueOf(key);
        } catch (Exception e) {
            try {
                sound = Sound.valueOf(legacyName);
            } catch (Exception ignored) {}
        }

        if (sound == null) {
            try {
                sound = Sound.valueOf("ENTITY_PLAYER_LEVELUP");
            } catch (Exception ignored) {}
        }

        SOUND_CACHE.put(key, sound);
        return sound;
    }

    public static ItemStack getGlassPane(int data) {
        if (IS_FLATTENING) {
            switch (data) {
                case 0: return new ItemStack(Material.valueOf("WHITE_STAINED_GLASS_PANE"));
                case 1: return new ItemStack(Material.valueOf("ORANGE_STAINED_GLASS_PANE"));
                case 2: return new ItemStack(Material.valueOf("MAGENTA_STAINED_GLASS_PANE"));
                case 3: return new ItemStack(Material.valueOf("LIGHT_BLUE_STAINED_GLASS_PANE"));
                case 4: return new ItemStack(Material.valueOf("YELLOW_STAINED_GLASS_PANE"));
                case 5: return new ItemStack(Material.valueOf("LIME_STAINED_GLASS_PANE"));
                case 7: return new ItemStack(Material.valueOf("GRAY_STAINED_GLASS_PANE"));
                case 8: return new ItemStack(Material.valueOf("LIGHT_GRAY_STAINED_GLASS_PANE"));
                case 9: return new ItemStack(Material.valueOf("CYAN_STAINED_GLASS_PANE"));
                case 10: return new ItemStack(Material.valueOf("PURPLE_STAINED_GLASS_PANE"));
                case 11: return new ItemStack(Material.valueOf("BLUE_STAINED_GLASS_PANE"));
                case 14: return new ItemStack(Material.valueOf("RED_STAINED_GLASS_PANE"));
                case 15: return new ItemStack(Material.valueOf("BLACK_STAINED_GLASS_PANE"));
                default: return new ItemStack(Material.valueOf("WHITE_STAINED_GLASS_PANE"));
            }
        } else {
            Material mat = getMaterial("STAINED_GLASS_PANE", "STAINED_GLASS_PANE");
            return new ItemStack(mat, 1, (short) data);
        }
    }

    public static String color(String text) {
        return color(null, text);
    }

    public static String color(Player player, String text) {
        if (text == null) return "";
        
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            text = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
        }

        java.util.regex.Pattern hexPattern = java.util.regex.Pattern.compile("&#([A-Fa-f0-9]{6})");
        java.util.regex.Matcher matcher = hexPattern.matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String color = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : color.toCharArray()) {
                replacement.append('§').append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }
        matcher.appendTail(buffer);
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    public static void playSound(Player player, Location loc, String modernName, String legacyName, float volume, float pitch) {
        Sound sound = getSound(modernName, legacyName);
        if (sound != null) {
            player.playSound(loc, sound, volume, pitch);
        }
    }

    @SuppressWarnings("deprecation")
    public static ItemStack createItemStack(String modernName, String legacyName, int data) {
        Material mat = getMaterial(modernName, legacyName);
        if (IS_FLATTENING) {
            return new ItemStack(mat);
        } else {
            return new ItemStack(mat, 1, (short) data);
        }
    }

    public static String getItemName(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return "AIR";
        
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName()) return meta.getDisplayName();
        }

        String name = item.getType().name().replace("_", " ").toLowerCase();
        StringBuilder sb = new StringBuilder();
        for (String word : name.split(" ")) {
            if (word.length() > 0) {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    public static void spawnParticle(Location loc, String particleName, int count, double offsetX, double offsetY, double offsetZ, double extra) {
        if (loc == null || loc.getWorld() == null) return;
        try {
            org.bukkit.Particle particle = org.bukkit.Particle.valueOf(particleName);
            loc.getWorld().spawnParticle(particle, loc, count, offsetX, offsetY, offsetZ, extra);
        } catch (Exception ignored) {}
    }

    public static boolean isHandOffHand(org.bukkit.event.player.PlayerInteractEvent event) {
        try {
            return event.getHand() == org.bukkit.inventory.EquipmentSlot.OFF_HAND;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static org.bukkit.block.Block getTargetBlock(Player player, int range) {
        try {
            org.bukkit.block.Block block = player.getTargetBlockExact(range);
            if (block != null) {
                return block;
            }
            return player.getTargetBlock((java.util.Set<Material>) null, range);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isDisplaySupported() {
        try {
            org.bukkit.entity.EntityType.valueOf("ITEM_DISPLAY");
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static org.bukkit.entity.Entity spawnItemDisplay(Location loc, ItemStack item) {
        if (!isDisplaySupported()) return null;
        try {
            org.bukkit.entity.ItemDisplay display = loc.getWorld().spawn(loc, org.bukkit.entity.ItemDisplay.class);
            display.setItemStack(item);
            return display;
        } catch (Exception e) {
            return null;
        }
    }

    public static org.bukkit.entity.Entity spawnTextDisplay(Location loc, String text) {
        if (!isDisplaySupported()) return null;
        try {
            org.bukkit.entity.TextDisplay display = loc.getWorld().spawn(loc, org.bukkit.entity.TextDisplay.class);
            display.setText(text);
            return display;
        } catch (Exception e) {
            return null;
        }
    }

    public static void setTransformation(org.bukkit.entity.Entity entity, org.bukkit.util.Vector translation, org.joml.Quaternionf leftRot, org.bukkit.util.Vector scale, int interpolationDuration) {
        if (!(entity instanceof org.bukkit.entity.Display)) return;
        org.bukkit.entity.Display display = (org.bukkit.entity.Display) entity;
        
        org.bukkit.util.Transformation transformation = new org.bukkit.util.Transformation(
            translation.toVector3f(),
            leftRot,
            scale.toVector3f(),
            new org.joml.Quaternionf()
        );
        
        display.setTransformation(transformation);
        display.setInterpolationDuration(interpolationDuration);
        display.setInterpolationDelay(0);
    }

    public static void setBrightness(org.bukkit.entity.Entity entity, int blockLight, int skyLight) {
        if (!(entity instanceof org.bukkit.entity.Display)) return;
        org.bukkit.entity.Display display = (org.bukkit.entity.Display) entity;
        display.setBrightness(new org.bukkit.entity.Display.Brightness(blockLight, skyLight));
    }

    public static boolean hasCustomModelData(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        try {
            return item.getItemMeta().hasCustomModelData();
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static int getCustomModelData(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        try {
            ItemMeta meta = item.getItemMeta();
            return meta.hasCustomModelData() ? meta.getCustomModelData() : 0;
        } catch (Throwable ignored) {
            return 0;
        }
    }

    public static void setCustomModelData(ItemStack item, int data) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        try {
            meta.setCustomModelData(data);
            item.setItemMeta(meta);
        } catch (Throwable ignored) {}
    }
}
