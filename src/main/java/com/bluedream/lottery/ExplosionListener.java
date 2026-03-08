package com.bluedream.lottery;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import java.util.Iterator;
import java.util.List;

public class ExplosionListener implements Listener {
    private final BlueDreamLottery plugin;

    public ExplosionListener(BlueDreamLottery plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        handleExplosion(event.blockList());
    }

    private void handleExplosion(List<Block> blocks) {
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
}
