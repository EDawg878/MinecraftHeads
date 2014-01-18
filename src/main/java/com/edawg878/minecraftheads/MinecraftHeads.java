package com.edawg878.minecraftheads;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.util.org.apache.commons.io.FileUtils;
import net.minecraft.util.org.apache.commons.lang3.text.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class MinecraftHeads extends JavaPlugin {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{2,16}");
    private static final String FILE_NAME = "minecraftheads.csv";
    private static final String DEFAULT_URL = "https://docs.google.com/spreadsheet/pub?key=0AtAZ7aOy1ZWQdE40TUhtNHU1NmstR1VKUlo0aEZleEE&output=csv";
    private static final int TIMEOUT = 5000;
    private File file;
    private Map<String, Head> heads;
    private Map<String, List<Head>> categories;
    private List<String> categoryList;

    @Override
    public void onEnable() {
        setupConfig();
        file = new File(getDataFolder(), FILE_NAME);
        if (!file.exists()) {
            saveResource(FILE_NAME, false);
        }
        reload();
        if (heads == null || categories == null || categoryList == null) {
            getLogger().severe("Error loading heads, disabling plugin...");
            setEnabled(false);
        }
        getCommand("mcheads").setExecutor(new MinecraftHeadsCommand(this));
    }

    public void download(CommandSender sender, final Callback callback) {
        getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
            public void run() {
                try {
                    URL url = getSpreadsheetURL();
                    FileUtils.copyURLToFile(url, file, TIMEOUT, TIMEOUT);
                    reload();
                    callback.onCompletion();
                } catch (IOException ex) {
                    getLogger().severe("Error saving downloaded data to file");
                    callback.onFailure();
                }
            }
        });
    }

    public void reload() {
        loadFromFile(file);
    }

    public Head getHead(String key) {
        return heads.get(key.toLowerCase());
    }

    public List<String> getHeadCompletions(String arg) {
        List<String> completions = new ArrayList<String>();
        for (String key : heads.keySet()) {
            if (key.startsWith(arg)) {
                completions.add(key);
            }
        }
        Collections.sort(completions, String.CASE_INSENSITIVE_ORDER);
        return completions;
    }

    public List<String> getCategoryCompletions(String arg) {
        List<String> completions = new ArrayList<String>();
        for (String key : categories.keySet()) {
            if (key.startsWith(arg)) {
                completions.add(key);
            }
        }
        Collections.sort(completions, String.CASE_INSENSITIVE_ORDER);
        return completions;
    }

    public List<Head> getHeads(String category) {
        return categories.get(category.toLowerCase());
    }

    public List<String> getCategories() {
        return categoryList;
    }

    public boolean isCategory(String category) {
        return categories.containsKey(category.toLowerCase());
    }

    public boolean isHead(String key) {
        return heads.containsKey(key.toLowerCase());
    }

    private void setupConfig() {
        getConfig().addDefault("spreadsheet-url", DEFAULT_URL);
        getConfig().options().copyDefaults(false);
        saveConfig();
    }

    private URL getSpreadsheetURL() throws MalformedURLException {
        return new URL(getConfig().getString("spreadsheet-url", DEFAULT_URL));
    }

    public void removeUnsafe() {
        try {
            File temp = new File(getDataFolder(), "minecraftheads-temporary.csv");
            if (temp.exists()) {
                temp.delete();
            }
            temp.createNewFile();
            CSVReader reader = new CSVReader(new FileReader(file));
            CSVWriter writer = new CSVWriter(new FileWriter(temp));
            String[] header = reader.readNext();
            if (header != null) {
                writer.writeNext(header);
                String[] line;
                while ((line = reader.readNext()) != null) {
                    if (line.length == 6 && line[5].equalsIgnoreCase("yes")) {
                        writer.writeNext(line);
                    }
                }
            }
            writer.close();
            reader.close();
            temp.renameTo(file);
        } catch (FileNotFoundException ex) {
            getLogger().severe("Failed to find file to load from");
        } catch (IOException ex) {
            getLogger().severe("Error reading from file");
        }
    }

    private void loadFromFile(File file) {
        try {
            CSVReader reader = new CSVReader(new FileReader(file));
            String[] header = reader.readNext();
            if ((header != null) && (verifyHeader(header))) {
                heads = new HashMap();
                categories = new TreeMap();
                String[] line;
                Head head;
                while ((line = reader.readNext()) != null) {
                    if (line.length == 6) {
                        String category;
                        String displayName;
                        List<String> keys;
                        String username;
                        Boolean safe;
                        if ((category = parseCategory(line[0])) != null
                                && (displayName = parseDisplayName(line[2])) != null
                                && (keys = parseKeys(line[3])) != null
                                && (username = parseUsername(line[4])) != null
                                && (safe = parseSafe(line[5])) != null) {
                            String id = parseID(line[1]);
                            head = new Head(displayName, username, id, safe);
                            if (categories.get(category) == null) {
                                categories.put(category, new ArrayList());
                            }
                            if (id != null) {
                                heads.put(id, head);
                            }
                            categories.get(category).add(head);
                            for (String key : keys) {
                                heads.put(key, head);
                            }
                        }
                    }
                }
                categoryList = new ArrayList<String>(categories.keySet());
            }
            reader.close();
        } catch (FileNotFoundException ex) {
            getLogger().severe("Failed to find file to load from");
        } catch (IOException ex) {
            getLogger().severe("Error reading from file");
        }
    }

    private boolean verifyHeader(String[] header) {
        if (header.length == 6) {
            return true;
        } else if (header.length == 1 && header[0].equalsIgnoreCase("#DefaultFile")) {
            getLogger().info("Default heads loaded, to download the latest use /mcheads download");
            return true;
        }
        return false;
    }

    private String parseCategory(String category) {
        return category.isEmpty() ? null : category.toLowerCase();
    }

    private String parseID(String id) {
        return id.isEmpty() ? null : id;
    }

    private String parseDisplayName(String displayName) {
        return WordUtils.capitalizeFully(displayName);
    }

    private List<String> parseKeys(String keys) {
        List<String> list = new ArrayList<String>();
        String[] split = keys.split(",");
        for (String entry : split) {
            list.add(entry.toLowerCase().replace(" ", ""));
        }
        return list.isEmpty() ? null : list;
    }

    private String parseUsername(String username) {
        if (USERNAME_PATTERN.matcher(username).matches()) {
            return username;
        }
        return null;
    }

    private Boolean parseSafe(String safe) {
        if (safe.equalsIgnoreCase("yes")) {
            return true;
        } else if (safe.equalsIgnoreCase("no")) {
            return true;
        }
        return null;
    }

    @AllArgsConstructor
    public class Head {

        @Getter
        private final String displayName, username, id;
        private final boolean safe;

        public boolean isSafe() {
            return safe;
        }

        public String getSafe() {
            return ChatColor.RED + "UNSAFE";
        }
    }

    public interface Callback {

        public void onCompletion();

        public void onFailure();
    }
}
