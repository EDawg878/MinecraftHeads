package com.edawg878.minecraftheads;

import au.com.bytecode.opencsv.CSVReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.util.org.apache.commons.io.FileUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class MinecraftHeads extends JavaPlugin {

    private MinecraftHeads instance;
    private static final String FILE_NAME = "minecraftheads.csv";
    private URL url;
    private File file;
    private Map<String, Map<String, String>> heads;

    @Override
    public void onEnable() {
        instance = this;
        setupConfig();
        setupURL();
        file = new File(getDataFolder(), FILE_NAME);
        if (!file.exists()) {
            saveResource(FILE_NAME, false);
        }
        loadFromFile(file);
        getCommand("mcheads").setExecutor(new MinecraftHeadsCommand(this));
    }

    public void download(final CommandSender sender, final Callback callback) {
        getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
            @Override
            public void run() {
                try {
                    FileUtils.copyURLToFile(url, file, 5000, 5000);
                    loadFromFile(file);
                    callback.onCompletion();
                } catch (IOException ex) {
                    getLogger().severe("Error saving downloaded data to file");
                    callback.onFailure();
                }
            }
        });
    }

    public interface Callback {

        void onCompletion();

        void onFailure();
    }

    public Map<String, Map<String, String>> getHeads() {
        return heads;
    }

    public MinecraftHeads getInstance() {
        return instance;
    }

    private void setupURL() {
        try {
            url = new URL(getConfig().getString("spreadsheet-url"));
        } catch (MalformedURLException ex) {
            getLogger().severe("Error: Invalid URL");
            setEnabled(false);
        }
    }

    private void setupConfig() {
        getConfig().addDefault("spreadsheet-url", "https://docs.google.com/spreadsheet/pub?key=0AtAZ7aOy1ZWQdE40TUhtNHU1NmstR1VKUlo0aEZleEE&output=csv");
        getConfig().options().copyDefaults(false);
        saveConfig();
    }

    private void loadFromFile(File file) {
        try {
            CSVReader reader = new CSVReader(new FileReader(file));
            String[] categories = reader.readNext();
            if (categories != null) {
                heads = new TreeMap<String, Map<String, String>>();
                for (String category : categories) {
                    heads.put(category.toLowerCase(), new TreeMap<String, String>());
                }
                String[] nextLine;
                while ((nextLine = reader.readNext()) != null) {
                    int count = 0;
                    for (String entry : nextLine) {
                        if (count++ < categories.length) {
                            String[] split = entry.split(":");
                            if (split.length == 2) {
                                heads.get(categories[count-1].toLowerCase()).put(split[0], split[1]);
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            getLogger().severe("Failed to find file to load from");
        } catch (IOException ex) {
            getLogger().severe("Error reading from file");
        }
    }
}
