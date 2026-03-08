package com.bluedream.lottery;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LotteryPool implements ConfigurationSerializable {
    private String name;
    private List<LotteryItem> items;
    private int pityCount;
    private String animationType;
    private String costType;
    private double costValue;
    private ItemStack keyItem;
    private String keyName;
    private Material chestMaterial;
    private ItemStack chestItem;
    private boolean showProbability;
    private String particleType;
    private boolean consumeChest;

    public LotteryPool(String name) {
        this.name = name;
        this.items = new ArrayList<>();
        this.pityCount = 50;
        this.animationType = "SLOT_MACHINE";
        this.costType = "NONE";
        this.costValue = 0;
        this.keyItem = null;
        this.keyName = "DEFAULT_KEY";
        this.chestMaterial = Material.CHEST;
        this.showProbability = false;
        this.particleType = "NONE";
        this.consumeChest = true;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<LotteryItem> getItems() { return items; }
    public int getPityCount() { return pityCount; }
    public void setPityCount(int pityCount) { this.pityCount = pityCount; }
    public String getAnimationType() { return animationType; }
    public void setAnimationType(String animationType) { this.animationType = animationType; }

    public String getCostType() { return costType; }
    public void setCostType(String costType) { this.costType = costType; }
    public double getCostValue() { return costValue; }
    public void setCostValue(double costValue) { this.costValue = costValue; }
    public ItemStack getKeyItem() { return keyItem; }
    public void setKeyItem(ItemStack keyItem) { this.keyItem = keyItem; }
    public String getKeyName() { return keyName; }
    public void setKeyName(String keyName) { this.keyName = keyName; }
    public Material getChestMaterial() { return chestMaterial; }
    public void setChestMaterial(Material chestMaterial) { this.chestMaterial = chestMaterial; }
    public ItemStack getChestItem() {
        return chestItem;
    }

    public ItemStack getFinalChestItem() {
        ItemStack item = chestItem != null ? chestItem.clone() : new ItemStack(chestMaterial != null ? chestMaterial : Material.CHEST);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            LanguageManager lm = BlueDreamLottery.getInstance().getLanguageManager();
            meta.setDisplayName(Adapter.color(lm.getMessage("chest_name").replace("{name}", name)));
            
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            String desc = Adapter.color(lm.getMessage("chest_lore_desc"));
            String rightClick = Adapter.color(lm.getMessage("chest_lore_right_click"));
            
            if (!lore.contains(desc)) lore.add(desc);
            if (!lore.contains(rightClick)) lore.add(rightClick);
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    public void setChestItem(ItemStack chestItem) { this.chestItem = chestItem; }
    public boolean isShowProbability() { return showProbability; }
    public void setShowProbability(boolean showProbability) { this.showProbability = showProbability; }
    public String getParticleType() { return particleType; }
    public void setParticleType(String particleType) { this.particleType = particleType; }
    public boolean isConsumeChest() { return consumeChest; }
    public void setConsumeChest(boolean consumeChest) { this.consumeChest = consumeChest; }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("items", items);
        map.put("pityCount", pityCount);
        map.put("animationType", animationType);
        map.put("costType", costType);
        map.put("costValue", costValue);
        map.put("keyItem", keyItem);
        map.put("keyName", keyName);
        map.put("chestMaterial", chestMaterial.name());
        if (chestItem != null) map.put("chestItem", chestItem);
        map.put("showProbability", showProbability);
        map.put("particleType", particleType);
        map.put("consumeChest", consumeChest);
        return map;
    }

    @SuppressWarnings("unchecked")
    public static LotteryPool deserialize(Map<String, Object> map) {
        LotteryPool pool = new LotteryPool((String) map.get("name"));
        pool.items = (List<LotteryItem>) map.get("items");
        pool.pityCount = (int) map.get("pityCount");
        pool.animationType = (String) map.get("animationType");
        pool.costType = map.getOrDefault("costType", "NONE").toString();
        pool.costValue = ((Number) map.getOrDefault("costValue", 0.0)).doubleValue();
        pool.keyItem = (ItemStack) map.get("keyItem");
        pool.keyName = (String) map.getOrDefault("keyName", "DEFAULT_KEY");
        String matName = (String) map.getOrDefault("chestMaterial", "CHEST");
        pool.chestMaterial = Material.valueOf(matName);
        if (map.containsKey("chestItem")) {
            pool.chestItem = (ItemStack) map.get("chestItem");
        }
        pool.showProbability = (boolean) map.getOrDefault("showProbability", false);
        pool.particleType = (String) map.getOrDefault("particleType", "NONE");
        pool.consumeChest = (boolean) map.getOrDefault("consumeChest", true);
        return pool;
    }
}
