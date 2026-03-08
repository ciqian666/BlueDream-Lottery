package com.bluedream.lottery;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class LotteryAdminGUI implements Listener {
    private final BlueDreamLottery plugin;
    private final java.util.Map<java.util.UUID, Long> grandPrizeCooldowns = new java.util.HashMap<>();

    public LotteryAdminGUI(BlueDreamLottery plugin) {
        this.plugin = plugin;
    }

    public void openPoolEditor(Player player, LotteryPool pool) {
        openPoolEditor(player, pool, 0);
    }

    public void openPoolEditor(Player player, LotteryPool pool, int page) {
        LanguageManager lm = plugin.getLanguageManager();
        String title = lm.getMessage("gui_admin_title").replace("{name}", pool.getName());
        Inventory inv = Bukkit.createInventory(new LotteryHolder(pool.getName(), "ADMIN_PAGE_" + page), 54, Adapter.color(title));

        ItemStack glass = Adapter.getGlassPane(15);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glass.setItemMeta(glassMeta);
        }

        for (int i = 45; i < 54; i++) {
            inv.setItem(i, glass);
        }

        if (page > 0) {
            inv.setItem(45, createItem(Adapter.getMaterial("ARROW", "ARROW"), "§a上一页", "§7当前第 " + (page + 1) + " 页"));
        }

        if ((page + 1) * 45 < pool.getItems().size()) {
            inv.setItem(46, createItem(Adapter.getMaterial("ARROW", "ARROW"), "§a下一页", "§7当前第 " + (page + 1) + " 页"));
        }

        inv.setItem(47, createItem(Adapter.getMaterial("GOLD_INGOT", "GOLD_INGOT"), lm.getMessage("cost_type").replace("{type}", pool.getCostType()), 
                lm.getMessage("cost_type_lore_current").replace("{type}", pool.getCostType()), lm.getMessage("cost_type_lore_click")));

        inv.setItem(48, createItem(Adapter.getMaterial("PAPER", "PAPER"), lm.getMessage("cost_value").replace("{value}", String.valueOf(pool.getCostValue())), lm.getMessage("cost_value_lore_click")));
        
        ItemStack keyDisplay = pool.getKeyItem() != null ? pool.getKeyItem().clone() : Adapter.createItemStack("BARRIER", "BARRIER", 0);
        ItemMeta keyMeta = keyDisplay.getItemMeta();
        if (keyMeta != null) {
            keyMeta.setDisplayName(lm.getMessage("key_setting_title"));
            List<String> keyLore = new ArrayList<>();
            String keyName = pool.getKeyName();
            if (keyName.equals("专属钥匙") || keyName.equals("Exclusive Key") || keyName.equals("DEFAULT_KEY")) keyName = lm.getMessage("default_key_name");
            keyLore.add(lm.getMessage("key_setting_lore_name").replace("{name}", keyName));
            keyLore.add(pool.getKeyItem() != null ? lm.getMessage("key_setting_lore_status_set") : lm.getMessage("key_setting_lore_status_unset"));
            keyLore.add(" ");
            keyLore.add(lm.getMessage("key_setting_lore_left"));
            keyLore.add(lm.getMessage("key_setting_lore_shift_left"));
            keyMeta.setLore(keyLore);
            keyDisplay.setItemMeta(keyMeta);
        }
        inv.setItem(49, keyDisplay);

        inv.setItem(50, createItem(Adapter.getMaterial("NETHER_STAR", "NETHER_STAR"), lm.getMessage("pity_count").replace("{count}", String.valueOf(pool.getPityCount())), 
                lm.getMessage("pity_count_lore_click"),
                lm.getMessage("pity_count_lore_cycle")));

        String animDisplayName = lm.getMessage("anim_" + pool.getAnimationType());
        inv.setItem(51, createItem(Adapter.getMaterial("COMPASS", "COMPASS"), lm.getMessage("animation_type").replace("{type}", animDisplayName), lm.getMessage("animation_type_lore_current").replace("{type}", animDisplayName)));

        ItemStack chestDisplay = pool.getChestItem() != null ? pool.getChestItem().clone() : new ItemStack(pool.getChestMaterial());
        ItemMeta chestMeta = chestDisplay.getItemMeta();
        if (chestMeta != null) {
            chestMeta.setDisplayName(lm.getMessage("chest_item_title"));
            List<String> lore = new ArrayList<>();
            lore.add(lm.getMessage("chest_item_lore_material").replace("{material}", pool.getChestMaterial().name()));
            lore.add(lm.getMessage("chest_item_lore_preview"));
            lore.add(lm.getMessage("chest_item_lore_set"));
            lore.add(lm.getMessage("chest_item_lore_consume"));
            chestMeta.setLore(lore);
            chestDisplay.setItemMeta(chestMeta);
        }
        inv.setItem(52, chestDisplay);

        String particleName = lm.getMessage("particle_" + pool.getParticleType());
        inv.setItem(53, createItem(Adapter.getMaterial("BLAZE_POWDER", "BLAZE_POWDER"), lm.getMessage("particle_type_title").replace("{type}", particleName),
                "§7显示概率: " + (pool.isShowProbability() ? "§a开启" : "§c关闭"),
                "§7消耗模式: " + (pool.isConsumeChest() ? "§a消耗" : "§c不消耗"),
                " ",
                "§e左键 §7- 切换粒子效果",
                "§e右键 §7- 切换宝箱消耗模式",
                "§eShift+右键 §7- 切换概率显示"));

        int start = page * 45;
        int end = Math.min(start + 45, pool.getItems().size());
        int slot = 0;
        for (int i = start; i < end; i++) {
            LotteryItem item = pool.getItems().get(i);
            ItemStack displayItem = item.getItem().clone();
            ItemMeta meta = displayItem.getItemMeta();
            
            if (item.getCustomName() != null && !item.getCustomName().isEmpty()) {
                meta.setDisplayName(Adapter.color(item.getCustomName()));
            } else if (item.getItem().hasItemMeta() && item.getItem().getItemMeta().hasDisplayName()) {
                meta.setDisplayName(item.getItem().getItemMeta().getDisplayName());
            }
            
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            lore.add(" ");
            lore.add(lm.getMessage("item_lore_attribute_header"));
            lore.add(lm.getMessage("item_lore_weight").replace("{weight}", String.valueOf(item.getChance())));
            lore.add(lm.getMessage("item_lore_grand_prize").replace("{status}", (item.isGrandPrize() ? lm.getMessage("grand_prize_yes") : lm.getMessage("grand_prize_no"))));
            lore.add(lm.getMessage("item_lore_display_status").replace("{status}", (item.isDisplayMessage() ? lm.getMessage("item_lore_display_yes") : lm.getMessage("item_lore_display_no"))));
            if (!item.getCommands().isEmpty()) {
                lore.add(lm.getMessage("item_lore_commands"));
                for (String cmd : item.getCommands()) {
                    lore.add("§8 - §f/" + cmd);
                }
            }
            lore.add(" ");
            lore.add(lm.getMessage("item_lore_left_click"));
            lore.add(lm.getMessage("item_lore_middle_click"));
            lore.add(lm.getMessage("item_lore_shift_left_click"));
            lore.add(lm.getMessage("item_lore_right_click"));
            lore.add(lm.getMessage("item_lore_shift_right_click"));
            meta.setLore(lore);
            displayItem.setItemMeta(meta);
            inv.setItem(slot++, displayItem);
        }

        player.openInventory(inv);
        Adapter.playSound(player, player.getLocation(), "BLOCK_NOTE_BLOCK_PLING", "NOTE_PLING", 1.0f, 2.0f);
    }

    private void handleAddItem(Player player, LotteryPool pool, ItemStack item) {
        if (item == null || item.getType().name().contains("AIR")) return;
        pool.getItems().add(new LotteryItem(item.clone(), 1.0, false));
        int lastPage = (pool.getItems().size() - 1) / 45;
        openPoolEditor(player, pool, lastPage);
        player.sendMessage(plugin.getLanguageManager().getMessage("add_item_success"));
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

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof LotteryHolder) {
            LotteryHolder holder = (LotteryHolder) event.getInventory().getHolder();
            if (holder.getType().startsWith("ADMIN_PAGE_")) {
                Player player = (Player) event.getWhoClicked();
                LotteryPool pool = plugin.getManager().getPool(holder.getPoolName());
                if (pool == null) return;
                LanguageManager lm = plugin.getLanguageManager();
                int page = Integer.parseInt(holder.getType().substring(11));

                Inventory clickedInv = event.getClickedInventory();
                if (clickedInv == null) return;

                if (clickedInv.equals(event.getView().getTopInventory())) {
                    event.setCancelled(true);
                    int slot = event.getSlot();
                    
                    if (slot == 45 && page > 0) {
                        openPoolEditor(player, pool, page - 1);
                        Adapter.playSound(player, player.getLocation(), "UI_BUTTON_CLICK", "CLICK", 1f, 1f);
                        return;
                    }
                    if (slot == 46 && (page + 1) * 45 < pool.getItems().size()) {
                        openPoolEditor(player, pool, page + 1);
                        Adapter.playSound(player, player.getLocation(), "UI_BUTTON_CLICK", "CLICK", 1f, 1f);
                        return;
                    }

                    if (slot == 47) {
                        String current = pool.getCostType();
                        java.util.List<String> types = java.util.Arrays.asList("NONE", "VAULT", "PLAYERPOINTS", "KEY");
                        int nextIndex = (types.indexOf(current) + 1) % types.size();
                        pool.setCostType(types.get(nextIndex));
                        openPoolEditor(player, pool, page);
                        Adapter.playSound(player, player.getLocation(), "UI_BUTTON_CLICK", "CLICK", 1f, 1f);
                        return;
                    }

                    if (slot == 48) {
                        ChatInputListener.startCostValueSession(player, pool);
                        return;
                    }

                    if (slot == 49) {
                        if (event.isLeftClick() && !event.isShiftClick()) {
                            ChatInputListener.startKeyNameSession(player, pool);
                        }
                        return;
                    }

                    if (slot == 50) {
                        if (event.isLeftClick()) {
                            ChatInputListener.startPityCountSession(player, pool);
                        } else if (event.isRightClick()) {
                            int current = pool.getPityCount();
                            int next = (current >= 100) ? 10 : current + 10;
                            pool.setPityCount(next);
                            openPoolEditor(player, pool, page);
                            Adapter.playSound(player, player.getLocation(), "UI_BUTTON_CLICK", "CLICK", 1f, 1f);
                        }
                        return;
                    }

                    if (slot == 51) {
                        String current = pool.getAnimationType();
                        java.util.List<String> modes = java.util.Arrays.asList("SLOT_MACHINE", "SHUFFLE", "STAR", "STORM", "CIRCLE", "PHANTOM", "JUDGMENT", "RIFT", "DIVINE", "PULSE", "THUNDER", "TIME_TRAVEL", "CROSS", "FIREWORK", "VR", "BLACKHOLE", "HELIX", "DECONSTRUCT");
                        int currentIndex = modes.indexOf(current);
                        int nextIndex;
                        if (event.isRightClick()) {
                            nextIndex = (currentIndex - 1 + modes.size()) % modes.size();
                        } else {
                            nextIndex = (currentIndex + 1) % modes.size();
                        }
                        if (nextIndex < 0) nextIndex = 0;
                        String nextMode = modes.get(nextIndex);
                        pool.setAnimationType(nextMode);
                        openPoolEditor(player, pool, page);
                        Adapter.playSound(player, player.getLocation(), "UI_BUTTON_CLICK", "CLICK", 1f, 1f);
                        player.sendMessage(lm.getMessage("animation_type").replace("{type}", lm.getMessage("anim_" + nextMode)));
                        return;
                    }

                    if (slot == 52) {
                        ItemStack chest = pool.getFinalChestItem();
                        player.getInventory().addItem(chest);
                        player.sendMessage(lm.getMessage("chest_received").replace("{name}", pool.getName()));
                        return;
                    }

                    if (slot == 53) {
                        if (event.isLeftClick()) {
                            String current = pool.getParticleType();
                            java.util.List<String> particles = java.util.Arrays.asList("NONE", "VORTEX", "HALO", "STAR_DUST", "FLAME_RING", "MAGIC_AURA");
                            int nextIndex = (particles.indexOf(current) + 1) % particles.size();
                            if (nextIndex < 0) nextIndex = 0;
                            String nextParticle = particles.get(nextIndex);
                            pool.setParticleType(nextParticle);
                            player.sendMessage(lm.getMessage("particle_type_changed").replace("{type}", lm.getMessage("particle_" + nextParticle)));
                        } else if (event.isRightClick()) {
                            if (event.isShiftClick()) {
                                pool.setShowProbability(!pool.isShowProbability());
                            } else {
                                pool.setConsumeChest(!pool.isConsumeChest());
                                plugin.getManager().savePool(pool);
                                player.sendMessage(lm.getMessage("pool_saved").replace("{name}", holder.getPoolName()));
                            }
                        }
                        openPoolEditor(player, pool, page);
                        Adapter.playSound(player, player.getLocation(), "UI_BUTTON_CLICK", "CLICK", 1f, 1f);
                        return;
                    }

                    if (slot < 45) {
                        int actualIndex = page * 45 + slot;
                        if (actualIndex < pool.getItems().size()) {
                            LotteryItem targetItem = pool.getItems().get(actualIndex);
                            if (event.getClick() == ClickType.MIDDLE) {
                                targetItem.setDisplayMessage(!targetItem.isDisplayMessage());
                                plugin.getManager().savePool(pool);
                                openPoolEditor(player, pool, page);
                                Adapter.playSound(player, player.getLocation(), "UI_BUTTON_CLICK", "CLICK", 1f, 1f);
                                String status = targetItem.isDisplayMessage() 
                                    ? lm.getMessage("item_lore_display_enabled") 
                                    : lm.getMessage("item_lore_display_disabled");
                                player.sendMessage(status);
                                event.setCancelled(true);
                                return;
                            }
                            if (event.isLeftClick()) {
                                if (event.isShiftClick()) {
                                    long now = System.currentTimeMillis();
                                    long lastToggle = grandPrizeCooldowns.getOrDefault(player.getUniqueId(), 0L);
                                    if (now - lastToggle < 1000) {
                                        player.sendMessage(lm.getMessage("too_fast"));
                                        return;
                                    }
                                    grandPrizeCooldowns.put(player.getUniqueId(), now);
                                    
                                    targetItem.setGrandPrize(!targetItem.isGrandPrize());
                                    plugin.getManager().savePool(pool);
                                    openPoolEditor(player, pool, page);
                                    Adapter.playSound(player, player.getLocation(), "UI_BUTTON_CLICK", "CLICK", 1f, 1f);
                                } else {
                                    ChatInputListener.startSession(player, pool, targetItem);
                                }
                            } else if (event.isRightClick()) {
                                if (event.isShiftClick()) {
                                    ChatInputListener.startCommandsSession(player, pool, targetItem);
                                } else {
 
                                    pool.getItems().remove(actualIndex);
                                    plugin.getManager().savePool(pool);
                                    openPoolEditor(player, pool, page);
                                    player.sendMessage(lm.getMessage("item_removed"));
                                }
                            }
                        }
                    }
                } else if (clickedInv.equals(event.getView().getBottomInventory())) {
                    ItemStack clicked = event.getCurrentItem();
                    if (clicked != null && clicked.getType() != Material.AIR) {
                        if (event.isShiftClick() && event.isLeftClick()) {
                            event.setCancelled(true);
                            ItemStack keyItem = clicked.clone();
                            keyItem.setAmount(1);
                            pool.setKeyItem(keyItem);
                            pool.setCostType("KEY");
                            openPoolEditor(player, pool, page);
                            player.sendMessage(lm.getMessage("key_item_set"));
                        } else if (event.isRightClick()) {
                            event.setCancelled(true);
                            ItemStack chestItem = clicked.clone();
                            chestItem.setAmount(1);
                            pool.setChestItem(chestItem);
                            pool.setChestMaterial(clicked.getType());
                            plugin.getManager().savePool(pool);
                            openPoolEditor(player, pool, page);
                            player.sendMessage(lm.getMessage("chest_material_set").replace("{material}", Adapter.getItemName(clicked)));
                        } else if (event.isLeftClick()) {
                            event.setCancelled(true);
                            LotteryItem newItem = new LotteryItem(clicked.clone(), 10.0, false);
                            pool.getItems().add(newItem);
                            plugin.getManager().savePool(pool);
                            int lastPage = (pool.getItems().size() - 1) / 45;
                            if (lastPage < 0) lastPage = 0;
                            openPoolEditor(player, pool, lastPage);
                            player.sendMessage(lm.getMessage("item_added"));
                        }
                    }
                }
            }
        }
    }
}
