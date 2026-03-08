package com.bluedream.lottery;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LotteryTabCompleter implements TabCompleter {
    private final BlueDreamLottery plugin;

    public LotteryTabCompleter(BlueDreamLottery plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.add("play");
            suggestions.add("stats");
            suggestions.add("ranking");
            if (sender.hasPermission("bluedream.lottery.admin")) {
                suggestions.add("create");
                suggestions.add("remove");
                suggestions.add("rename");
                suggestions.add("edit");
                suggestions.add("give");
                suggestions.add("reload");
                suggestions.add("lang");
                suggestions.add("setblock");
                suggestions.add("removeblock");
            }
            StringUtil.copyPartialMatches(args[0], suggestions, completions);
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("play") || subCommand.equals("edit") || subCommand.equals("remove") || subCommand.equals("delete") || subCommand.equals("setblock") || subCommand.equals("give") || subCommand.equals("rename")) {
                suggestions.addAll(plugin.getManager().getPools().keySet());
            } else if (subCommand.equals("lang")) {
                suggestions.add("zh_cn");
                suggestions.add("en_us");
            }
            StringUtil.copyPartialMatches(args[1], suggestions, completions);
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("play") || subCommand.equals("give")) {
                suggestions.add("1");
                suggestions.add("10");
                if (subCommand.equals("give")) {
                    suggestions.add("64");
                }
            }
            StringUtil.copyPartialMatches(args[2], suggestions, completions);
        } else if (args.length == 4) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("give")) {
                for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                    suggestions.add(p.getName());
                }
            }
            StringUtil.copyPartialMatches(args[3], suggestions, completions);
        }

        Collections.sort(completions);
        return completions;
    }
}
