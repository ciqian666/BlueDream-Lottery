package com.bluedream.lottery;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class LotteryHolder implements InventoryHolder {
    private final String poolName;
    private final String type;

    public LotteryHolder(String poolName, String type) {
        this.poolName = poolName;
        this.type = type;
    }

    public String getPoolName() {
        return poolName;
    }

    public String getType() {
        return type;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
