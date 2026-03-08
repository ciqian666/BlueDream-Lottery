package com.bluedream.lottery;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class LotteryItem implements ConfigurationSerializable {
    private ItemStack item;
    private double chance;
    private boolean isGrandPrize;
    private String customName;
    private java.util.List<String> commands;
    private boolean displayMessage;

    public LotteryItem(ItemStack item, double chance, boolean isGrandPrize) {
        this.item = item;
        this.chance = chance;
        this.isGrandPrize = isGrandPrize;
        this.customName = null;
        this.commands = new java.util.ArrayList<>();
        this.displayMessage = false;
    }

    public ItemStack getItem() { return item; }
    public double getChance() { return chance; }
    public void setChance(double chance) { this.chance = chance; }
    public boolean isGrandPrize() { return isGrandPrize; }
    public void setGrandPrize(boolean grandPrize) { isGrandPrize = grandPrize; }
    public String getCustomName() { return customName; }
    public void setCustomName(String customName) { this.customName = customName; }
    public java.util.List<String> getCommands() { return commands; }
    public void setCommands(java.util.List<String> commands) { this.commands = commands; }
    public boolean isDisplayMessage() { return displayMessage; }
    public void setDisplayMessage(boolean displayMessage) { this.displayMessage = displayMessage; }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("item", item);
        map.put("chance", chance);
        map.put("isGrandPrize", isGrandPrize);
        if (customName != null) map.put("customName", customName);
        map.put("commands", commands);
        map.put("displayMessage", displayMessage);
        return map;
    }

    @SuppressWarnings("unchecked")
    public static LotteryItem deserialize(Map<String, Object> map) {
        LotteryItem item = new LotteryItem(
                (ItemStack) map.get("item"),
                (double) map.get("chance"),
                (boolean) map.get("isGrandPrize")
        );
        if (map.containsKey("customName")) {
            item.setCustomName((String) map.get("customName"));
        }
        if (map.containsKey("commands")) {
            item.setCommands((java.util.List<String>) map.get("commands"));
        }
        if (map.containsKey("displayMessage")) {
            item.setDisplayMessage((boolean) map.get("displayMessage"));
        }
        return item;
    }
}
