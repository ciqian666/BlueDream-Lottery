package com.bluedream.lottery;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class LotteryExpansion extends PlaceholderExpansion {

    private final BlueDreamLottery plugin;

    public LotteryExpansion(BlueDreamLottery plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public String getAuthor() {
        return "BlueDream";
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "bluedream";
    }

    @Override
    @NotNull
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        // %bluedream_pity_<pool>%
        if (params.startsWith("pity_")) {
            String poolName = params.substring(5);
            return String.valueOf(plugin.getPlayerDataManager().getPity(player.getUniqueId(), poolName));
        }

        // %bluedream_max_pity_<pool>%
        if (params.startsWith("max_pity_")) {
            String poolName = params.substring(9);
            LotteryPool pool = plugin.getManager().getPool(poolName);
            return pool != null ? String.valueOf(pool.getPityCount()) : "0";
        }

        // %bluedream_total_draws_<pool>%
        if (params.startsWith("total_draws_")) {
            String poolName = params.substring(12);
            return String.valueOf(plugin.getPlayerDataManager().getTotalDraws(player.getUniqueId(), poolName));
        }

        // %bluedream_total_draws%
        if (params.equals("total_draws")) {
            return String.valueOf(plugin.getPlayerDataManager().getGlobalTotalDraws(player.getUniqueId()));
        }

        return null;
    }
}
