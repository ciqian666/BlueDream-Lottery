package com.bluedream.lottery;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LotteryPlayGUI implements Listener {
    private final BlueDreamLottery plugin;

    public LotteryPlayGUI(BlueDreamLottery plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, LotteryPool pool) {
        open(player, pool, 0);
    }

    public void open(Player player, LotteryPool pool, int page) {
        LanguageManager lm = plugin.getLanguageManager();
        String title = lm.getMessage("gui_admin_title").replace("{name}", pool.getName());
        Inventory inv = Bukkit.createInventory(new LotteryHolder(pool.getName(), "PLAY_PAGE_" + page), 54, Adapter.color(title));

        ItemStack glass = Adapter.getGlassPane(9);
        ItemMeta meta = glass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            glass.setItemMeta(meta);
        }
        for (int i = 45; i < 54; i++) inv.setItem(i, glass);

        if (page > 0) {
            inv.setItem(45, createItem(Adapter.getMaterial("ARROW", "ARROW"), "§a上一页", "§7当前第 " + (page + 1) + " 页"));
        }
        if ((page + 1) * 45 < pool.getItems().size()) {
            inv.setItem(53, createItem(Adapter.getMaterial("ARROW", "ARROW"), "§a下一页", "§7当前第 " + (page + 1) + " 页"));
        }

        int pity = plugin.getPlayerDataManager().getPity(player.getUniqueId(), pool.getName());
        List<String> startLore = new ArrayList<>();
        startLore.add(lm.getMessage("start_animation"));
        startLore.add(lm.getMessage("good_luck"));
        startLore.add(" ");
        startLore.add(lm.getMessage("single_roll"));
        startLore.add(lm.getMessage("ten_rolls"));
        startLore.add(" ");
        
        String costText = "";
        String costType = pool.getCostType();
        if ("VAULT".equals(costType)) {
            costText = lm.getMessage("cost_vault").replace("{value}", String.valueOf(pool.getCostValue())).replace("{value10}", String.valueOf(pool.getCostValue() * 10));
        } else if ("PLAYERPOINTS".equals(costType)) {
            costText = lm.getMessage("cost_points").replace("{value}", String.valueOf((int)pool.getCostValue())).replace("{value10}", String.valueOf((int)(pool.getCostValue() * 10)));
        } else if ("KEY".equals(costType)) {
            String keyName = pool.getKeyName();
            if (keyName.equals("专属钥匙") || keyName.equals("Exclusive Key") || keyName.equals("DEFAULT_KEY")) keyName = lm.getMessage("default_key_name");
            costText = lm.getMessage("cost_key").replace("{name}", keyName);
        } else {
            costText = lm.getMessage("cost_free");
        }
        startLore.add(costText);
        startLore.add(lm.getMessage("consume_chest").replace("{name}", pool.getName()));
        startLore.add(" ");
        startLore.add(lm.getMessage("pity_progress").replace("{current}", String.valueOf(pity)).replace("{max}", String.valueOf(pool.getPityCount())));

        inv.setItem(49, createItem(Adapter.getMaterial("GOLD_INGOT", "GOLD_INGOT"), lm.getMessage("start_lottery_btn"), startLore.toArray(new String[0])));

        double totalWeight = 0;
        if (pool.isShowProbability()) {
            for (LotteryItem item : pool.getItems()) totalWeight += item.getChance();
        }

        int start = page * 45;
        int end = Math.min(start + 45, pool.getItems().size());
        int slot = 0;
        for (int i = start; i < end; i++) {
            LotteryItem item = pool.getItems().get(i);
            ItemStack display = item.getItem().clone();
            ItemMeta itemMeta = display.getItemMeta();
            List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();
            lore.add(" ");
            
            if (pool.isShowProbability() && totalWeight > 0) {
                double prob = (item.getChance() / totalWeight) * 100.0;
                lore.add(lm.getMessage("get_probability").replace("{prob}", String.format("%.2f", prob)));
            }

            if (item.getCustomName() != null && !item.getCustomName().isEmpty()) {
                itemMeta.setDisplayName(Adapter.color(item.getCustomName()));
            }

            if (item.isGrandPrize()) {
                String displayName = (item.getCustomName() != null && !item.getCustomName().isEmpty()) 
                    ? Adapter.color(item.getCustomName()) 
                    : Adapter.getItemName(display);
                itemMeta.setDisplayName(lm.getMessage("grand_prize_display").replace("{name}", displayName));
                lore.add(lm.getMessage("grand_prize_label"));
                lore.add(lm.getMessage("grand_prize_desc"));
            }
            
            itemMeta.setLore(lore);
            display.setItemMeta(itemMeta);
            inv.setItem(slot++, display);
        }

        player.openInventory(inv);
        Adapter.playSound(player, player.getLocation(), "BLOCK_NOTE_BLOCK_PLING", "NOTE_PLING", 1.0f, 2.0f);
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = Adapter.createItemStack(material.name(), material.name(), 0);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> loreList = new ArrayList<>();
            for (String l : lore) loreList.add(l);
            meta.setLore(loreList);
            item.setItemMeta(meta);
        }
        return item;
    }

    public void play(Player player, LotteryPool pool, int count) {
        LanguageManager lm = plugin.getLanguageManager();
        if (AnimationEngine.isAnimating(player.getUniqueId())) {
            player.sendMessage(lm.getMessage("animating"));
            return;
        }

        if (pool.getItems().isEmpty()) {
            player.sendMessage(lm.getMessage("pool_empty"));
            return;
        }

        if (!checkAndDeductCost(player, pool, count)) {
            return;
        }

        AnimationEngine engine = new AnimationEngine(plugin, player, pool);
        engine.setCostInfo(pool.getCostType(), pool.getCostValue() * count);
        engine.start(count);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof LotteryHolder) {
            LotteryHolder holder = (LotteryHolder) event.getInventory().getHolder();
            LanguageManager lm = plugin.getLanguageManager();
            if (holder.getType().startsWith("PLAY_PAGE_")) {
                event.setCancelled(true);
                Player player = (Player) event.getWhoClicked();
                LotteryPool pool = plugin.getManager().getPool(holder.getPoolName());
                if (pool == null) return;
                int page = Integer.parseInt(holder.getType().substring(10));

                int slot = event.getRawSlot();
                if (slot == 45 && page > 0) {
                    open(player, pool, page - 1);
                    Adapter.playSound(player, player.getLocation(), "UI_BUTTON_CLICK", "CLICK", 1f, 1f);
                    return;
                }
                if (slot == 53 && (page + 1) * 45 < pool.getItems().size()) {
                    open(player, pool, page + 1);
                    Adapter.playSound(player, player.getLocation(), "UI_BUTTON_CLICK", "CLICK", 1f, 1f);
                    return;
                }

                if (slot == 49) {
                    play(player, pool, event.isLeftClick() ? 1 : 10);
                }
            }
        }
    }

    private boolean consumeLotteryChest(Player player, String poolName, int count) {
        LotteryPool pool = plugin.getManager().getPool(poolName);
        if (pool == null) return false;
        
        int remaining = count;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == pool.getChestMaterial() && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta.hasDisplayName()) {
                    LanguageManager lm = plugin.getLanguageManager();
                    String prefix = lm.getMessage("chest_name_prefix");
                    String oldPrefix = "§6§l奖池宝箱: §f";
                    String enPrefix = "§6§lLottery Chest: §f";
                    String displayName = meta.getDisplayName();
                    
                    if (displayName.equals(prefix + poolName) || displayName.equals(oldPrefix + poolName) || displayName.equals(enPrefix + poolName)) {
                        if (pool.getChestItem() != null && Adapter.hasCustomModelData(pool.getChestItem())) {
                            if (Adapter.getCustomModelData(pool.getChestItem()) != Adapter.getCustomModelData(item)) {
                                continue;
                            }
                        }
                        
                        int amount = item.getAmount();
                        if (amount > remaining) {
                            item.setAmount(amount - remaining);
                            return true;
                        } else {
                            remaining -= amount;
                            player.getInventory().setItem(i, null);
                            if (remaining <= 0) return true;
                        }
                    }
                }
            }
        }
        return remaining <= 0;
    }

    private boolean hasLotteryChest(Player player, String poolName, int count) {
        LotteryPool pool = plugin.getManager().getPool(poolName);
        if (pool == null) return false;

        int total = 0;
        ItemStack[] contents = player.getInventory().getContents();
        for (ItemStack item : contents) {
            if (item != null && item.getType() == pool.getChestMaterial() && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta.hasDisplayName()) {
                    LanguageManager lm = plugin.getLanguageManager();
                    String prefix = lm.getMessage("chest_name_prefix");
                    String oldPrefix = "§6§l奖池宝箱: §f";
                    String enPrefix = "§6§lLottery Chest: §f";
                    String displayName = meta.getDisplayName();
                    if (displayName.equals(prefix + poolName) || displayName.equals(oldPrefix + poolName) || displayName.equals(enPrefix + poolName)) {
                        if (pool.getChestItem() != null && Adapter.hasCustomModelData(pool.getChestItem())) {
                            if (Adapter.getCustomModelData(pool.getChestItem()) != Adapter.getCustomModelData(item)) {
                                continue;
                            }
                        }
                        total += item.getAmount();
                    }
                }
            }
        }
        return total >= count;
    }

    private boolean checkAndDeductCost(Player player, LotteryPool pool, int count) {
        LanguageManager lm = plugin.getLanguageManager();
        
        if (pool.isConsumeChest()) {
            if (!hasLotteryChest(player, pool.getName(), count)) {
                player.sendMessage(lm.getMessage("need_chests").replace("{count}", String.valueOf(count)));
                return false;
            }
        }

        String costType = pool.getCostType();
        double costValue = pool.getCostValue() * count;

        switch (costType) {
            case "VAULT":
                if (plugin.getEconomy() == null) {
                    player.sendMessage(lm.getMessage("vault_missing"));
                    return false;
                }
                if (!plugin.getEconomy().has(player, costValue)) {
                    player.sendMessage(lm.getMessage("insufficient_vault").replace("{cost}", String.valueOf(costValue)));
                    return false;
                }
                break;
            case "PLAYERPOINTS":
                Object ppAPI = plugin.getPlayerPointsAPI();
                if (ppAPI == null) {
                    player.sendMessage(lm.getMessage("playerpoints_missing"));
                    return false;
                }
                try {
                    int balance = (int) ppAPI.getClass().getMethod("look", UUID.class).invoke(ppAPI, player.getUniqueId());
                    if (balance < (int) costValue) {
                        player.sendMessage(lm.getMessage("insufficient_points").replace("{cost}", String.valueOf((int) costValue)));
                        return false;
                    }
                } catch (Exception e) {
                    player.sendMessage(lm.getMessage("points_check_error").replace("{error}", e.getMessage()));
                    return false;
                }
                break;
            case "KEY":
                ItemStack key = pool.getKeyItem();
                if (key == null) {
                    player.sendMessage(lm.getMessage("key_not_set"));
                    return false;
                }
                if (!player.getInventory().containsAtLeast(key, count)) {
                    player.sendMessage(lm.getMessage("insufficient_keys").replace("{count}", String.valueOf(count)).replace("{name}", pool.getKeyName()));
                    return false;
                }
                break;
        }

        if (pool.isConsumeChest()) {
            consumeLotteryChest(player, pool.getName(), count);
        }

        switch (costType) {
            case "VAULT":
                plugin.getEconomy().withdrawPlayer(player, costValue);
                player.sendMessage(lm.getMessage("vault_deducted").replace("{value}", String.valueOf(costValue)));
                break;
            case "PLAYERPOINTS":
                try {
                    Object ppAPI = plugin.getPlayerPointsAPI();
                    ppAPI.getClass().getMethod("take", UUID.class, int.class).invoke(ppAPI, player.getUniqueId(), (int) costValue);
                    player.sendMessage(lm.getMessage("points_deducted").replace("{value}", String.valueOf((int) costValue)));
                } catch (Exception ignored) {}
                break;
            case "KEY":
                ItemStack keysToConsume = pool.getKeyItem().clone();
                keysToConsume.setAmount(count);
                player.getInventory().removeItem(keysToConsume);
                player.sendMessage(lm.getMessage("keys_consumed").replace("{count}", String.valueOf(count)));
                break;
        }

        return true;
    }
}
