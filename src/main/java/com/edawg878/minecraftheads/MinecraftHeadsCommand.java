package com.edawg878.minecraftheads;

import com.edawg878.minecraftheads.MinecraftHeads.Callback;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.util.org.apache.commons.lang3.StringUtils;
import net.minecraft.util.org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author EDawg878 <EDawg878@gmail.com>
 */
public class MinecraftHeadsCommand implements CommandExecutor {

    private final MinecraftHeads plugin;

    public MinecraftHeadsCommand(MinecraftHeads instance) {
        plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                showCategories(sender);
                return true;
            } else if (args[0].equalsIgnoreCase("download")) {
                download(sender);
                return true;
            }
            return false;
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("list")) {
                showAvailableHeads(sender, args[1], 1);
                return true;
            }
            return false;
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("list")) {
                if (StringUtils.isNumeric(args[2])) {
                    int page = NumberUtils.toInt(args[2]);
                    showAvailableHeads(sender, args[1], page);
                    return true;
                }
            }
        }
        invalidArgument(sender);
        return false;
    }

    private void invalidArgument(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Invalid Argument");
    }

    private void showCategories(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "Available Categories: ");
        for (String category : plugin.getHeads().keySet()) {
            sender.sendMessage(ChatColor.RED + "- " + ChatColor.GOLD + WordUtils.capitalize(category));
        }
    }

    private void showAvailableHeads(CommandSender sender, String category, int page) {
        category = category.toLowerCase();
        if (plugin.getHeads().containsKey(category)) {
            Map<String, String> heads = plugin.getHeads().get(category);
            List<String> keys = new ArrayList<String>(heads.keySet());
            final int pagesize = 8;
            final int maxpage = (int) Math.ceil(heads.size() / (double) pagesize);
            if (page > 0 && page <= maxpage) {
                sender.sendMessage(ChatColor.GOLD + "Available Heads: (page " + page + "/" + maxpage + ")");
                for (int i = (page - 1) * pagesize; (i < heads.size()) && (i < page * pagesize); i++) {
                    String displayName = keys.get(i);
                    String username = heads.get(displayName);
                    sender.sendMessage(ChatColor.GOLD + "[" + (i + 1) + "] " + ChatColor.RED + displayName + ChatColor.GOLD + " [" + username + "]");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Invalid page");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Invalid Category");
        }
    }

    private void download(final CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "Downloading latest heads...");
        plugin.download(sender, new Callback() {

            public void onCompletion() {
                sender.sendMessage(ChatColor.GOLD + "File dowload complete");
            }

            public void onFailure() {
                sender.sendMessage(ChatColor.RED + "Error downloading file");
            }

        });
    }

}
