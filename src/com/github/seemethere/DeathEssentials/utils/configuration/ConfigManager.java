package com.github.seemethere.DeathEssentials.utils.configuration;

import com.github.seemethere.DeathEssentials.DeathEssentialsPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Custom version of https://gist.github.com/SagaciousZed/3174347
 * This version will create custom yml files in specified directories
 *
 * @author seemethere
 */

public class ConfigManager {

    private final File moduleFolder;
    private final String fileName;
    private final DeathEssentialsPlugin plugin;
    private final Logger logger;
    private final String name;
    private File configFile;
    private Map<String, File> customFiles;
    private Map<YamlConfiguration, File> customConfigs;
    private YamlConfiguration fileConfiguration;
    private double version;

    public ConfigManager(DeathEssentialsPlugin plugin, String dir, String name) {
        if (plugin == null)
            throw new IllegalArgumentException("plugin cannot be null");
        if (!plugin.isInitialized())
            throw new IllegalArgumentException("plugin must be initiaized");
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.fileName = name + ".yml";
        this.name = name;
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists())
            dataFolder.mkdir();
        File moduleFolder = new File(dataFolder + dir);
        this.moduleFolder = moduleFolder;
        if (!moduleFolder.exists()) {
            moduleFolder.mkdirs();
        }
        configFile = new File(moduleFolder, fileName);
        saveDefaultConfig();
        fileConfiguration = YamlConfiguration.loadConfiguration(configFile);
        version = fileConfiguration.getDouble("version");
        customFiles = new HashMap<String, File>();
        customConfigs =  new HashMap<YamlConfiguration, File>();
    }

    //========================================================
    // Typical methods related to using a Config
    //========================================================
    public void reloadConfig() {
        // Look for defaults in the jar
        InputStream defConfigStream = plugin.getResource(fileName);
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            fileConfiguration.setDefaults(defConfig);
        }
    }

    public YamlConfiguration getConfig() {
        if (fileConfiguration == null) {
            this.reloadConfig();
        }
        return fileConfiguration;
    }

    public File getModuleFolder() {
        return moduleFolder;
    }

    public void saveConfig() {
        if (fileConfiguration != null && configFile != null) try {
            getConfig().save(configFile);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, ex);
        }
    }

    public void saveDefaultConfig() {
        if (!configFile.exists()) {
            try {
                copyResource(configFile, fileName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //========================================================
    // Creating custom files dealing with plugin things
    //========================================================
    public YamlConfiguration getModuleConfig(String name) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(createModuleFile(name));
        customConfigs.put(config, createModuleFile(name));
        return config;
    }

    public void saveModuleConfig(YamlConfiguration inConfig) {
        if (customConfigs.containsKey(inConfig)) {
            try {
                inConfig.save(customConfigs.get(inConfig));
            } catch (Exception e) {
                plugin.getLogger().severe("[" + name + "] Could not save custom config '" + inConfig.getName() + "'");
            }
            return;
        }
        plugin.getLogger().severe("[" + name +"] Config '" + inConfig.getName() + "' could not be saved because it " +
                "was not registered with the config manager");
    }

    private File createModuleFile(String name) {
        if (!customFiles.containsKey(name))
            customFiles.put(name, new File(this.getModuleFolder(), name));
        return customFiles.get(name);
    }

    //========================================================
    // All things dealing with updating the config
    //========================================================
    public int updateConfig() {
        if (!needsUpdate())
            return 1;
        if (plugin.getConfig().getBoolean("KeepOldConfigs")) {
            File oldFile = new File(moduleFolder, name + " v" + version + ".yml");
            if (!configFile.renameTo(oldFile))
                plugin.getLogger().severe("[" + name + "] Error updating module config!");
        } else
            configFile.delete();
        configFile = new File(moduleFolder, name + ".yml");
        saveDefaultConfig();
        fileConfiguration = YamlConfiguration.loadConfiguration(configFile);
        version = fileConfiguration.getDouble("version");
        return 0;
    }

    public boolean needsUpdate() {
        return (fileConfiguration.getDouble("version") < plugin.getModuleManager().getModuleInfo(name).version());
    }

    private void versionConfig(BufferedWriter out) throws IOException {
        out.newLine();
        out.write("#========================================================");
        out.newLine();
        out.write("#=IMPORTANT: Do not touch this variable!!! The plugin   =");
        out.newLine();
        out.write("#=           uses this variable to see if your config   =");
        out.newLine();
        out.write("#=           needs to be updated                        =");
        out.newLine();
        out.write("version: " + plugin.getModuleManager().getModuleInfo(name).version());
        out.newLine();
        out.write("#========================================================");
        out.newLine();
        out.flush();
        out.close();
    }

    private boolean copyResource(File file, String resource) {
        if (!file.exists()) try {
            InputStream input = plugin.getResource(resource);
            if (input == null) {
//                logger.severe("[" + name + "] Resource '" + resource +
//                        "' not found! Contact author for help! Unplugging module!");
//                plugin.getModuleManager().unplugModule(name);
                return true;
            }
            logger.info("[" + name + "] Creating default config file '" + fileName + "'");
            file.createNewFile();
            OutputStream output = new FileOutputStream(file);
            copy(input, output);
            versionConfig(new BufferedWriter(new FileWriter(file, true)));
        } catch (IOException e) {
            logger.severe("[" + name + "] Error creating config file '" + fileName + "' ! Unplugging module!");
            plugin.getModuleManager().unplugModule(name);
        }
        return false;
    }

    private void copy(InputStream input, OutputStream output) throws IOException {
        int read;
        byte[] bytes = new byte[1024];
        while ((read = input.read(bytes)) != -1)
            output.write(bytes, 0, read);
        input.close();
        output.close();
    }
}