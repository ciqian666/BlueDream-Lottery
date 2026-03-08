package com.bluedream.lottery;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LotteryChestListener implements Listener {
    private final BlueDreamLottery plugin;

    public LotteryChestListener(BlueDreamLottery plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (Adapter.isHandOffHand(event)) return;
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta.hasDisplayName()) {
                    LanguageManager lm = plugin.getLanguageManager();
                    String displayName = meta.getDisplayName();
                    String prefix = lm.getMessage("chest_name_prefix");
                    String oldPrefix = "§6§l奖池宝箱: §f";
                    String enPrefix = "§6§lLottery Chest: §f";
                    
                    if (displayName.startsWith(prefix) || displayName.startsWith(oldPrefix) || displayName.startsWith(enPrefix)) {
                        event.setCancelled(true);
                        String poolName;
                        if (displayName.startsWith(prefix)) poolName = displayName.replace(prefix, "");
                        else if (displayName.startsWith(oldPrefix)) poolName = displayName.replace(oldPrefix, "");
                        else poolName = displayName.replace(enPrefix, "");
                        Player player = event.getPlayer();

                        if (AnimationEngine.isAnimating(player.getUniqueId())) {
                            player.sendMessage(lm.getMessage("animation_wait"));
                            return;
                        }

                        LotteryPool pool = plugin.getManager().getPool(poolName);
                        if (pool != null) {
                            if (item.getType() != pool.getChestMaterial()) {
                                return;
                            }
                            
                            if (pool.getChestItem() != null && Adapter.hasCustomModelData(pool.getChestItem())) {
                                int requiredData = Adapter.getCustomModelData(pool.getChestItem());
                                int currentData = Adapter.getCustomModelData(item);
                                if (requiredData != currentData) {
                                    return;
                                }
                            }
                            
                            plugin.getPlayGUI().open(player, pool);
                            player.sendMessage(lm.getMessage("opening_pool").replace("{name}", pool.getName()));
                        } else {
                            player.sendMessage(lm.getMessage("pool_not_found"));
                        }
                    }
                }
            }
        }
    }
}
