package com.bluedream.lottery;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LotteryCommand implements CommandExecutor {
    private final BlueDreamLottery plugin;

    public LotteryCommand(BlueDreamLottery plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        LanguageManager lm = plugin.getLanguageManager();

        if (args.length == 0) {
            if (sender instanceof Player) {
                sendHelp((Player) sender);
            } else {
                sender.sendMessage("§6§l=== BlueDream Lottery ===");
                sender.sendMessage("§f/lt give <pool> <amount> <player> - 给抽奖箱");
                sender.sendMessage("§f/lt reload - 重载插件");
            }
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(lm.getMessage("only_players"));
                    return true;
                }
                Player player = (Player) sender;
                if (!player.hasPermission("bluedream.lottery.admin")) return true;
                if (args.length < 2) {
                    player.sendMessage(lm.getMessage("usage_create"));
                    return true;
                }
                String name = args[1];
                if (plugin.getManager().getPool(name) != null) {
                    player.sendMessage(lm.getMessage("pool_already_exists").replace("{name}", name));
                    return true;
                }
                plugin.getManager().createPool(name);
                plugin.getAdminGUI().openPoolEditor(player, plugin.getManager().getPool(name));
                player.sendMessage(lm.getMessage("pool_created").replace("{name}", name));
                break;

            case "rename":
                if (sender instanceof Player && !sender.hasPermission("bluedream.lottery.admin")) return true;
                if (args.length < 3) {
                    sender.sendMessage(lm.getMessage("usage_rename"));
                    return true;
                }
                String oldRenameName = args[1];
                String newRenameName = args[2];
                if (plugin.getManager().getPool(oldRenameName) == null) {
                    sender.sendMessage(lm.getMessage("pool_not_found").replace("{name}", oldRenameName));
                    return true;
                }
                if (plugin.getManager().getPool(newRenameName) != null) {
                    sender.sendMessage(lm.getMessage("pool_already_exists").replace("{name}", newRenameName));
                    return true;
                }
                if (plugin.getManager().renamePool(oldRenameName, newRenameName)) {
                    sender.sendMessage(lm.getMessage("pool_renamed")
                            .replace("{old}", oldRenameName)
                            .replace("{new}", newRenameName));
                } else {
                    sender.sendMessage(lm.getMessage("rename_failed"));
                }
                break;

            case "remove":
            case "delete":
                if (sender instanceof Player && !sender.hasPermission("bluedream.lottery.admin")) return true;
                if (args.length < 2) {
                    sender.sendMessage(lm.getMessage("usage_remove"));
                    return true;
                }
                String delName = args[1];
                if (plugin.getManager().getPool(delName) == null) {
                    sender.sendMessage(lm.getMessage("pool_not_found").replace("{name}", delName));
                    return true;
                }
                plugin.getManager().deletePool(delName);
                sender.sendMessage(lm.getMessage("pool_deleted").replace("{name}", delName));
                break;

            case "edit":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(lm.getMessage("only_players"));
                    return true;
                }
                Player editPlayer = (Player) sender;
                if (!editPlayer.hasPermission("bluedream.lottery.admin")) return true;
                if (args.length < 2) {
                    editPlayer.sendMessage(lm.getMessage("usage_edit"));
                    return true;
                }
                LotteryPool pool = plugin.getManager().getPool(args[1]);
                if (pool == null) {
                    editPlayer.sendMessage(lm.getMessage("pool_not_found").replace("{name}", args[1]));
                    return true;
                }
                plugin.getAdminGUI().openPoolEditor(editPlayer, pool);
                break;

            case "give":
                if (sender instanceof Player && !sender.hasPermission("bluedream.lottery.admin")) return true;
                if (args.length < 3) {
                    sender.sendMessage(lm.getMessage("usage_give"));
                    return true;
                }
                String givePoolName = args[1];
                LotteryPool givePool = plugin.getManager().getPool(givePoolName);
                if (givePool == null) {
                    sender.sendMessage(lm.getMessage("pool_not_found").replace("{name}", givePoolName));
                    return true;
                }
                
                int amount;
                try {
                    amount = Integer.parseInt(args[2]);
                    if (amount <= 0) {
                        sender.sendMessage(lm.getMessage("invalid_number"));
                        return true;
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(lm.getMessage("invalid_number"));
                    return true;
                }

                Player target;
                if (args.length >= 4) {
                    String targetName = args[3];
                    if (targetName.equalsIgnoreCase("%player%") && sender instanceof Player) {
                        target = (Player) sender;
                    } else {
                        target = org.bukkit.Bukkit.getPlayer(targetName);
                    }
                    
                    if (target == null) {
                        sender.sendMessage(lm.getMessage("player_not_found"));
                        return true;
                    }
                } else {
                    if (sender instanceof Player) {
                        target = (Player) sender;
                    } else {
                        sender.sendMessage("§c控制台执行此命令必须指定目标玩家: /lt give <pool> <amount> <player>");
                        return true;
                    }
                }

                org.bukkit.inventory.ItemStack chestItem = givePool.getFinalChestItem();
                chestItem.setAmount(amount);
                target.getInventory().addItem(chestItem);
                
                sender.sendMessage(lm.getMessage("give_success")
                        .replace("{player}", target.getName())
                        .replace("{amount}", String.valueOf(amount))
                        .replace("{pool}", givePoolName));
                break;

            case "play":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(lm.getMessage("only_players"));
                    return true;
                }
                Player playPlayer = (Player) sender;
                if (!playPlayer.hasPermission("bluedream.lottery.use")) return true;
                if (args.length < 2) {
                    playPlayer.sendMessage(lm.getMessage("usage_play"));
                    return true;
                }
                LotteryPool playPool = plugin.getManager().getPool(args[1]);
                if (playPool == null) {
                    playPlayer.sendMessage(lm.getMessage("pool_not_found").replace("{name}", args[1]));
                    return true;
                }
                if (playPool.getItems().isEmpty()) {
                    playPlayer.sendMessage(lm.getMessage("pool_empty"));
                    return true;
                }
                
                if (args.length >= 3) {
                    try {
                        int count = Integer.parseInt(args[2]);
                        if (count <= 0) {
                            playPlayer.sendMessage(lm.getMessage("invalid_number"));
                            return true;
                        }

                        if (count > 100) count = 100;
                        plugin.getPlayGUI().play(playPlayer, playPool, count);
                    } catch (NumberFormatException e) {
                        playPlayer.sendMessage(lm.getMessage("invalid_number"));
                    }
                } else {
                    plugin.getPlayGUI().open(playPlayer, playPool);
                }
                break;

            case "setblock":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(lm.getMessage("only_players"));
                    return true;
                }
                Player setPlayer = (Player) sender;
                if (!setPlayer.hasPermission("bluedream.lottery.admin")) return true;
                if (args.length < 2) {
                    setPlayer.sendMessage(lm.getMessage("usage_setblock"));
                    return true;
                }
                String setPoolName = args[1];
                if (plugin.getManager().getPool(setPoolName) == null) {
                    setPlayer.sendMessage(lm.getMessage("pool_not_found").replace("{name}", setPoolName));
                    return true;
                }
                org.bukkit.block.Block targetBlock = Adapter.getTargetBlock(setPlayer, 5);
                if (targetBlock == null || targetBlock.getType().name().contains("AIR")) {
                    setPlayer.sendMessage(lm.getMessage("must_look_at_block"));
                    return true;
                }
                plugin.getManager().addLotteryBlock(targetBlock.getLocation(), setPoolName);
                setPlayer.sendMessage(lm.getMessage("setblock_success").replace("{name}", setPoolName));
                break;

            case "removeblock":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(lm.getMessage("only_players"));
                    return true;
                }
                Player rbPlayer = (Player) sender;
                if (!rbPlayer.hasPermission("bluedream.lottery.admin")) return true;
                org.bukkit.block.Block removeBlock = Adapter.getTargetBlock(rbPlayer, 5);
                if (removeBlock == null || removeBlock.getType().name().contains("AIR")) {
                    rbPlayer.sendMessage(lm.getMessage("must_look_at_block"));
                    return true;
                }
                String boundPool = plugin.getManager().getPoolAt(removeBlock.getLocation());
                if (boundPool == null) {
                    rbPlayer.sendMessage(lm.getMessage("not_a_lottery_block"));
                    return true;
                }
                plugin.getManager().removeLotteryBlock(removeBlock.getLocation());
                rbPlayer.sendMessage(lm.getMessage("removeblock_success").replace("{name}", boundPool));
                break;

            case "stats":
            case "ranking":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(lm.getMessage("only_players"));
                    return true;
                }
                Player sPlayer = (Player) sender;
                if (!sPlayer.hasPermission("bluedream.lottery.use")) return true;
                if (plugin.getStatisticsManager() == null) {
                    sPlayer.sendMessage(lm.getMessage("database_disabled"));
                    return true;
                }
                plugin.getStatsGUI().openMainMenu(sPlayer);
                break;

            case "reload":
                if (sender instanceof Player && !sender.hasPermission("bluedream.lottery.admin")) return true;
                plugin.reloadPlugin();
                sender.sendMessage(plugin.getLanguageManager().getMessage("reload_success"));
                break;

            case "lang":
                if (sender instanceof Player && !sender.hasPermission("bluedream.lottery.admin")) return true;
                if (args.length < 2) {
                    sender.sendMessage(lm.getMessage("usage_lang"));
                    return true;
                }
                String targetLang = args[1].toLowerCase();
                lm.setLanguage(targetLang);
                sender.sendMessage(lm.getMessage("lang_changed").replace("{lang}", targetLang));
                break;

            default:
                if (sender instanceof Player) {
                    sendHelp((Player) sender);
                }
                break;
        }

        return true;
    }

    private void sendHelp(Player player) {
        LanguageManager lm = plugin.getLanguageManager();
        player.sendMessage(lm.getMessage("help_header"));
        player.sendMessage(lm.getMessage("help_create"));
        player.sendMessage(lm.getMessage("help_rename"));
        player.sendMessage(lm.getMessage("help_edit"));
        player.sendMessage(lm.getMessage("help_give"));
        player.sendMessage(lm.getMessage("help_remove"));
        player.sendMessage(lm.getMessage("help_play"));
        player.sendMessage(lm.getMessage("help_play_cmd"));
        player.sendMessage(lm.getMessage("help_setblock"));
        player.sendMessage(lm.getMessage("help_removeblock"));
        player.sendMessage(lm.getMessage("help_stats"));
        player.sendMessage(lm.getMessage("help_reload"));
        player.sendMessage(lm.getMessage("help_lang"));
    }
}
