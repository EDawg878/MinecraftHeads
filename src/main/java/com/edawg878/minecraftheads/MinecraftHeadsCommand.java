package com.edawg878.minecraftheads;

import com.edawg878.minecraftheads.MinecraftHeads.Callback;
import com.edawg878.minecraftheads.MinecraftHeads.Head;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.org.apache.commons.lang3.StringUtils;
import net.minecraft.util.org.apache.commons.lang3.math.NumberUtils;
import net.minecraft.util.org.apache.commons.lang3.text.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class MinecraftHeadsCommand implements CommandExecutor, TabCompleter {

    private final MinecraftHeads plugin;

    public MinecraftHeadsCommand(MinecraftHeads instance) {
        this.plugin = instance;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.isOp() || sender.hasPermission("mcheads.get")) {
            return plugin.getHeadCompletions(args[0].toLowerCase());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("list") && sender.isOp() || sender.hasPermission("mcheads.list")) {
            return plugin.getCategoryCompletions(args[1].toLowerCase());
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            getPlayerHead(sender, sender.getName(), 1);
            return true;
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                showCategories(sender);
            } else if (args[0].equalsIgnoreCase("download")) {
                download(sender);
            } else if (args[0].equalsIgnoreCase("help")) {
                usage(sender);
            } else if (args[0].equalsIgnoreCase("reload")) {
                reload(sender);
            } else if (args[0].equalsIgnoreCase("removeunsafe")) {
                removeUnsafe(sender);
            } else {
                getHead(sender, args[0], 1);
            }
            return true;
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("list")) {
                showAvailableHeads(sender, args[1], 1);
                return true;
            }
            if (args[0].equalsIgnoreCase("player")) {
                getPlayerHead(sender, args[1], 1);
                return true;
            }
            if (StringUtils.isNumeric(args[1])) {
                int quantity = NumberUtils.toInt(args[1]);
                getHead(sender, args[0], quantity);
                return true;
            }
        } else if ((args.length == 3)
                && (StringUtils.isNumeric(args[2]))) {
            int num = NumberUtils.toInt(args[2]);
            if (args[0].equalsIgnoreCase("player")) {
                getPlayerHead(sender, args[1], num);
                return true;
            }
            if (args[0].equalsIgnoreCase("list")) {
                showAvailableHeads(sender, args[1], num);
                return true;
            }
        }
        invalidArgument(sender);
        usage(sender);
        return false;
    }

    private void invalidArgument(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Invalid Argument");
    }

    private void usage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "/mcheads list - List categories");
        sender.sendMessage(ChatColor.GOLD + "/mcheads list <category> - List heads in category");
        sender.sendMessage(ChatColor.GOLD + "/mcheads <key> [qty] - Spawn heads by name or id");
        sender.sendMessage(ChatColor.GOLD + "/mcheads [player] [qty] - Spawn player heads");
    }

    private void showCategories(CommandSender sender) {
        if (sender.isOp() || sender.hasPermission("mcheads.list")) {
            sender.sendMessage(ChatColor.GOLD + "Available Categories: ");
            for (String category : plugin.getCategories()) {
                sender.sendMessage(ChatColor.RED + "- " + ChatColor.GOLD + WordUtils.capitalizeFully(category));
            }
        } else {
            sender.sendMessage(ChatColor.RED + "No permission");
        }
    }

    private void showAvailableHeads(CommandSender sender, String category, int page) {
        if (sender.isOp() || sender.hasPermission("mcheads.list")) {
            if (plugin.isCategory(category)) {
                List<Head> heads = plugin.getHeads(category);
                final int pagesize = 8;
                int maxpage = (int) Math.ceil(heads.size() / (double) pagesize);
                if (page > 0 && page <= maxpage) {
                    sender.sendMessage(ChatColor.GOLD + "Heads in Category " + category + ": (page " + page + "/" + maxpage + ")");
                    for (int i = (page - 1) * pagesize; i < heads.size() && i < page * pagesize; i++) {
                        Head head = heads.get(i);
                        if (head.getId() != null) {
                            sender.sendMessage(ChatColor.GOLD + "[" + head.getId() + "] " + ChatColor.GOLD + head.getDisplayName() + " [" + head.getSafe() + ChatColor.GOLD + "]");
                        } else {
                            sender.sendMessage(ChatColor.RED + "- " + ChatColor.GOLD + head.getDisplayName() + " [" + head.getSafe() + ChatColor.GOLD + "]");
                        }
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

    private void getHead(CommandSender sender, String key, int quantity) {
        if (sender.isOp() || sender.hasPermission("mcheads.get")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (plugin.isHead(key)) {
                    spawnHead(player, plugin.getHead(key), quantity);
                } else {
                    sender.sendMessage(ChatColor.RED + "Invalid head name");
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

                @Override
                public void onCompletion() {
                    sender.sendMessage(ChatColor.GOLD + "File download complete");
                }

                @Override
                public void onFailure() {
                    sender.sendMessage(ChatColor.RED + "Error downloading file");
                }
            });
        } else {
            sender.sendMessage(ChatColor.RED + "No permission");
        }
    }

    private void spawnHead(Player player, Head head, int amount) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, amount);
        skull.setDurability((short) 3);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwner(head.getUsername());
        meta.setDisplayName(ChatColor.BLUE + head.getDisplayName());
        List<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GOLD + "Username: " + ChatColor.RED + head.getUsername());
        lore.add(ChatColor.GOLD + "Safe: " + ChatColor.RED + WordUtils.capitalizeFully(String.valueOf(head.isSafe())));
        lore.add(ChatColor.GOLD + "Generated from " + ChatColor.RED + "MinecraftHeads" + ChatColor.GOLD + " by " + ChatColor.RED + "EDawg878");
        meta.setLore(lore);
        skull.setItemMeta(meta);
        player.getInventory().addItem(skull);
        String heads = amount == 1 ? "head" : "heads";
        player.sendMessage(ChatColor.GOLD + "Spawned " + ChatColor.RED + amount + ChatColor.GOLD + " " + ChatColor.ITALIC + head.getDisplayName() + ChatColor.GOLD + " " + heads + " [" + head.getSafe() + ChatColor.GOLD + "]");
    }

    private void getPlayerHead(CommandSender sender, String target, int amount) {
        if (sender.isOp() || sender.hasPermission("mcheads.get")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                ItemStack skull = new ItemStack(Material.SKULL_ITEM, amount);
                skull.setDurability((short) 3);
                SkullMeta meta = (SkullMeta) skull.getItemMeta();
                meta.setOwner(target);
                meta.setDisplayName(ChatColor.BLUE + target);
                List<String> lore = new ArrayList<String>();
                lore.add(ChatColor.GOLD + "Username: " + ChatColor.RED + target);
                lore.add(ChatColor.GOLD + "Generated from " + ChatColor.RED + "MinecraftHeads" + ChatColor.GOLD + " by " + ChatColor.RED + "EDawg878");
                meta.setLore(lore);
                skull.setItemMeta(meta);
                player.getInventory().addItem(skull);
                String heads = amount == 1 ? "head" : "heads";
                player.sendMessage(ChatColor.GOLD + "Spawned " + ChatColor.RED + amount + ChatColor.GOLD + ChatColor.ITALIC + " Player " + ChatColor.GOLD + heads + " [" + ChatColor.RED + target + ChatColor.GOLD + "]");
            } else {
                sender.sendMessage(ChatColor.RED + "You must be in-game to execute this command");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "No permission");
        }
    }

    private void reload(CommandSender sender) {
        if (sender.isOp() || sender.hasPermission("mcheads.reload")) {
            plugin.reloadConfig();
            plugin.reload();
            sender.sendMessage(ChatColor.GOLD + "MinecraftHeads reloaded");
        } else {
            sender.sendMessage(ChatColor.RED + "No permission");
        }
    }

    private void removeUnsafe(CommandSender sender) {
        if (sender.isOp() || sender.hasPermission("mcheads.removeunsafe")) {
            plugin.removeUnsafe();
            plugin.reload();
            sender.sendMessage(ChatColor.GOLD + "MinecraftHeads removed all unsafe heads");
        } else {
            sender.sendMessage(ChatColor.RED + "No permission");
        }
    }
}
