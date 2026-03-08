package com.bluedream.lottery;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.block.Block;
import java.util.Iterator;

public class GlobalListener implements Listener {
    private final BlueDreamLottery plugin;

    public GlobalListener(BlueDreamLottery plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        AnimationEngine.stopAnimating(event.getPlayer().getUniqueId());
        plugin.getPlayerDataManager().unloadPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof LotteryHolder) {
            LotteryHolder holder = (LotteryHolder) event.getInventory().getHolder();
            
            if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
                event.setCancelled(true);
                return;
            }

            if (holder.getType().startsWith("PLAY_PAGE_") || holder.getType().equals("ANIMATION")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        String poolName = plugin.getManager().getPoolAt(event.getBlock().getLocation());
        if (poolName != null) {
            if (plugin.getManager().getPool(poolName) != null) {
                LanguageManager lm = plugin.getLanguageManager();
                event.setCancelled(true);
                event.getPlayer().sendMessage(lm.getMessage("block_is_pool").replace("{name}", poolName));
                event.getPlayer().sendMessage(lm.getMessage("remove_block_usage"));
            } else {
                plugin.getManager().removeLotteryBlock(event.getBlock().getLocation());
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        handleExplosion(event.blockList());
    }

    public void handleExplosion(java.util.List<Block> blocks) {
        Iterator<Block> iterator = blocks.iterator();
        while (iterator.hasNext()) {
            Block block = iterator.next();
            String poolName = plugin.getManager().getPoolAt(block.getLocation());
            if (poolName != null) {
                if (plugin.getManager().getPool(poolName) != null) {
                    iterator.remove();
                } else {
                    plugin.getManager().removeLotteryBlock(block.getLocation());
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (Adapter.isHandOffHand(event)) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        String poolName = plugin.getManager().getPoolAt(event.getClickedBlock().getLocation());
        if (poolName != null) {
            event.setCancelled(true);
            LotteryPool pool = plugin.getManager().getPool(poolName);
            if (pool != null) {
                if (pool.getItems().isEmpty()) {
                    event.getPlayer().sendMessage(plugin.getLanguageManager().getMessage("pool_empty_interact"));
                    return;
                }
                plugin.getPlayGUI().open(event.getPlayer(), pool);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof LotteryHolder) {
            event.setCancelled(true);
        }
    }
}
