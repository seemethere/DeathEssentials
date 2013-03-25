package com.github.seemethere.DeathEssentials.utils.configuration;

import com.github.seemethere.DeathEssentials.DeathEssentialsPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Custom version of https://gist.github.com/SagaciousZed/3174347
 * This version will create custom yml files in specified directories
 *
 * @author seemethere
 */

public class CustomConfig {

    private static File configFile;
    private static YamlConfiguration fileConfiguration;
    private final File moduleFolder;
    private final String fileName;
    private final DeathEssentialsPlugin plugin;
    private final Logger logger;
    private final String module;

    public CustomConfig(DeathEssentialsPlugin plugin, String fileName, String dir, String module) {
        if (plugin == null)
            throw new IllegalArgumentException("plugin cannot be null");
        if (!plugin.isInitialized())
            throw new IllegalArgumentException("plugin must be initiaized");
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.fileName = fileName;
        this.module = module;
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists())
            dataFolder.mkdir();
        File moduleFolder = new File(dataFolder + "/modules" + dir);
        this.moduleFolder = moduleFolder;
        if (!moduleFolder.exists()) {
            moduleFolder.mkdirs();
        }
        configFile = new File(moduleFolder, fileName);
        fileConfiguration = YamlConfiguration.loadConfiguration(configFile);
        saveDefaultConfig();
    }

    public void reloadConfig() {
        fileConfiguration = YamlConfiguration.loadConfiguration(configFile);

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
            copyResource(configFile, fileName);
        }
    }

    private boolean copyResource(File file, String resource) {
        if (!file.exists()) try {
            InputStream input = plugin.getResource(resource);
            if (input == null) {
                logger.severe("[" + module + "] Resource '" + resource +
                        "' not found! Contact author for help! Unplugging module!");
                plugin.getModuleManager().unplugModule(module);
                return true;
            }
            logger.info("[" + module + "] Creating default config file '" + fileName + "'");
            file.createNewFile();
            OutputStream output = new FileOutputStream(file);
            copy(input, output);
        } catch (IOException e) {
            logger.severe("[" + module + "] Error creating config file '" + fileName + "' ! Unplugging module!");
            plugin.getModuleManager().unplugModule(module);
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