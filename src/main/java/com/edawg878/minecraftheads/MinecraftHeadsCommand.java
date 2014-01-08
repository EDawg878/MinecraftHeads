package com.edawg878.minecraftheads;

import com.edawg878.minecraftheads.MinecraftHeads.Callback;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.util.org.apache.commons.lang3.StringUtils;
import net.minecraft.util.org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

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
            } else if (args[0].equalsIgnoreCase("get")) {
                if (StringUtils.isNumeric(args[2])) {
                    int number = NumberUtils.toInt(args[2]);
                    getHead(sender, args[1], number, 1);
                } else {
                    getHead(sender, args[1], args[2], 1);
                }
                return true;
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("get")) {
                if (StringUtils.isNumeric(args[3])) {
                    int quantity = NumberUtils.toInt(args[3]);
                    if (StringUtils.isNumeric(args[2])) {
                        int number = NumberUtils.toInt(args[2]);
                        getHead(sender, args[1], number, quantity);
                    } else {
                        getHead(sender, args[1], args[2], quantity);
                    }
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

    private void notifySpawnedHeads(Player player, String displayName, int amount) {
        String head = amount == 1? "head" : "heads";
        player.sendMessage(ChatColor.GOLD + "Spawned " + ChatColor.RED + amount + ChatColor.GOLD + " " + WordUtils.capitalize(displayName) + " " + head);
    }

    private void showCategories(CommandSender sender) {
        if (sender.isOp() || sender.hasPermission("mcheads.list")) {
            sender.sendMessage(ChatColor.GOLD + "Available Categories: ");
            for (String category : plugin.getHeads().keySet()) {
                sender.sendMessage(ChatColor.RED + "- " + ChatColor.GOLD + WordUtils.capitalize(category));
            }
        } else {
            sender.sendMessage(ChatColor.RED + "No permission");
        }
    }

    private void showAvailableHeads(CommandSender sender, String category, int page) {
        if (sender.isOp() || sender.hasPermission("mcheads.list")) {
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
        } else {
            sender.sendMessage(ChatColor.RED + "No permission");
        }
    }

    private void getHead(CommandSender sender, String category, int number, int quantity) {
        if (sender.isOp() || sender.hasPermission("mcheads.get")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                category = category.toLowerCase();
                if (plugin.getHeads().containsKey(category)) {
                    Map<String, String> heads = plugin.getHeads().get(category);
                    List<String> keys = new ArrayList<String>(heads.keySet());
                    if (number > 0 && number <= keys.size()) {
                        String displayName = keys.get(number-1);
                        String username = heads.get(displayName);
                        spawnHead(player, username, quantity);
                        notifySpawnedHeads(player, displayName, quantity);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Invalid head number");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Invalid Category");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You must be in-game to execute this command");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "No permission");
        }
    }

    private void getHead(CommandSender sender, String category, String displayName, int quantity) {
        if (sender.isOp() || sender.hasPermission("mcheads.get")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                category = category.toLowerCase();
                displayName = WordUtils.capitalize(displayName);
                if (plugin.getHeads().containsKey(category)) {
                    Map<String, String> heads = plugin.getHeads().get(category);
                    if (heads.containsKey(displayName)) {
                        String username = heads.get(displayName);
                        spawnHead(player, username, quantity);
                        notifySpawnedHeads(player, displayName, quantity);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Invalid head name");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Invalid Category");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You must be in-game to execute this command");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "No permission");
        }
    }

    private void download(final CommandSender sender) {
        if (sender.isOp() || sender.hasPermission("mcheads.download")) {
            sender.sendMessage(ChatColor.GOLD + "Downloading latest heads...");
            plugin.download(sender, new Callback() {

                public void onCompletion() {
                    sender.sendMessage(ChatColor.GOLD + "File download complete");
                }

                public void onFailure() {
                    sender.sendMessage(ChatColor.RED + "Error downloading file");
                }

            });
        } else {
            sender.sendMessage(ChatColor.RED + "No permission");
        }
    }

    private void spawnHead(Player player, String targetName, int amount) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, amount);
        skull.setDurability((short) 3);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwner(targetName);
        skull.setItemMeta(meta);
        player.getInventory().addItem(skull);
    }

}
