package com.bluedream.lottery;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatInputListener implements Listener {
    private final BlueDreamLottery plugin;
    private static final Map<UUID, InputSession> sessions = new HashMap<>();

    public ChatInputListener(BlueDreamLottery plugin) {
        this.plugin = plugin;
    }

    public static void startSession(Player player, LotteryPool pool, LotteryItem item) {
        LanguageManager lm = BlueDreamLottery.getInstance().getLanguageManager();
        sessions.put(player.getUniqueId(), new InputSession(pool, item, SessionType.WEIGHT));
        player.closeInventory();
        player.sendMessage(" ");
        player.sendMessage(lm.getMessage("chat_input_prefix") + lm.getMessage("input_weight"));
        player.sendMessage(lm.getMessage("input_cancel_hint"));
        player.sendMessage(" ");
    }

    public static void startCostValueSession(Player player, LotteryPool pool) {
        LanguageManager lm = BlueDreamLottery.getInstance().getLanguageManager();
        sessions.put(player.getUniqueId(), new InputSession(pool, null, SessionType.COST_VALUE));
        player.closeInventory();
        player.sendMessage(" ");
        player.sendMessage(lm.getMessage("chat_input_prefix") + lm.getMessage("input_cost_value"));
        player.sendMessage(lm.getMessage("input_cancel_hint"));
        player.sendMessage(" ");
    }

    public static void startKeyNameSession(Player player, LotteryPool pool) {
        LanguageManager lm = BlueDreamLottery.getInstance().getLanguageManager();
        sessions.put(player.getUniqueId(), new InputSession(pool, null, SessionType.KEY_NAME));
        player.closeInventory();
        player.sendMessage(" ");
        player.sendMessage(lm.getMessage("chat_input_prefix") + lm.getMessage("input_key_name"));
        player.sendMessage(lm.getMessage("input_color_hint"));
        player.sendMessage(" ");
    }

    public static void startCommandsSession(Player player, LotteryPool pool, LotteryItem item) {
        LanguageManager lm = BlueDreamLottery.getInstance().getLanguageManager();
        sessions.put(player.getUniqueId(), new InputSession(pool, item, SessionType.COMMANDS));
        player.closeInventory();
        player.sendMessage(" ");
        player.sendMessage(lm.getMessage("chat_input_prefix") + lm.getMessage("input_commands"));
        player.sendMessage(lm.getMessage("input_commands_hint"));
        player.sendMessage(lm.getMessage("input_current_commands").replace("{commands}", String.join(" || ", item.getCommands())));
        player.sendMessage(" ");
    }

    public static void startItemNameSession(Player player, LotteryPool pool, LotteryItem item) {
        LanguageManager lm = BlueDreamLottery.getInstance().getLanguageManager();
        sessions.put(player.getUniqueId(), new InputSession(pool, item, SessionType.ITEM_NAME));
        player.closeInventory();
        player.sendMessage(" ");
        player.sendMessage(lm.getMessage("chat_input_prefix") + lm.getMessage("input_item_name"));
        player.sendMessage(lm.getMessage("input_color_hint"));
        player.sendMessage(" ");
    }

    public static void startPityCountSession(Player player, LotteryPool pool) {
        LanguageManager lm = BlueDreamLottery.getInstance().getLanguageManager();
        sessions.put(player.getUniqueId(), new InputSession(pool, null, SessionType.PITY_COUNT));
        player.closeInventory();
        player.sendMessage(" ");
        player.sendMessage(lm.getMessage("chat_input_prefix") + lm.getMessage("input_pity_count"));
        player.sendMessage(lm.getMessage("input_cancel_hint"));
        player.sendMessage(" ");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        InputSession session = sessions.remove(player.getUniqueId());
        if (session == null) return;

        event.setCancelled(true);
        String msg = event.getMessage();
        LanguageManager lm = plugin.getLanguageManager();

        if (msg.equalsIgnoreCase("cancel") || msg.equalsIgnoreCase(lm.getMessage("cancel_word"))) {
            player.sendMessage(lm.getMessage("input_cancelled"));
        } else {
            boolean saveRequired = false;
            if (session.type == SessionType.KEY_NAME) {
                String coloredName = msg.replace("&", "§");
                session.pool.setKeyName(coloredName);
                player.sendMessage(lm.getMessage("key_name_set").replace("{name}", coloredName));
                saveRequired = true;
            } else if (session.type == SessionType.ITEM_NAME) {
                session.item.setCustomName(msg);
                player.sendMessage(lm.getMessage("item_name_set").replace("{name}", msg.replace("&", "§")));
            } else if (session.type == SessionType.COMMANDS) {
                String[] cmds = msg.split("\\|\\|");
                java.util.List<String> cmdList = new java.util.ArrayList<>();
                for (String c : cmds) {
                    cmdList.add(c.trim());
                }
                session.item.setCommands(cmdList);
                player.sendMessage(lm.getMessage("commands_set").replace("{count}", String.valueOf(cmdList.size())));
            } else {
                try {
                    double val = Double.parseDouble(msg);
                    if (session.type == SessionType.WEIGHT) {
                        session.item.setChance(val);
                        player.sendMessage(lm.getMessage("weight_set").replace("{value}", String.valueOf(val)));
                    } else if (session.type == SessionType.COST_VALUE) {
                        session.pool.setCostValue(val);
                        player.sendMessage(lm.getMessage("cost_value_set").replace("{value}", String.valueOf(val)));
                        saveRequired = true;
                    } else if (session.type == SessionType.PITY_COUNT) {
                        session.pool.setPityCount((int) val);
                        player.sendMessage(lm.getMessage("pity_count_set").replace("{value}", String.valueOf((int) val)));
                        saveRequired = true;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(lm.getMessage("invalid_number"));
                }
            }
            
            plugin.getManager().savePool(session.pool);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getAdminGUI().openPoolEditor(player, session.pool);
            }
        }.runTask(plugin);
    }

    private enum SessionType {
        WEIGHT, COST_VALUE, KEY_NAME, COMMANDS, ITEM_NAME, PITY_COUNT
    }

    private static class InputSession {
        LotteryPool pool;
        LotteryItem item;
        SessionType type;
        InputSession(LotteryPool pool, LotteryItem item, SessionType type) {
            this.pool = pool;
            this.item = item;
            this.type = type;
        }
    }
}
