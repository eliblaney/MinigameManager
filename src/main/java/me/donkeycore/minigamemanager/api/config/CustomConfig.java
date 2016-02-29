package me.donkeycore.minigamemanager.api.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import me.donkeycore.minigamemanager.core.MinigameManager;

public class CustomConfig {
	
	/**
	 * The FileConfiguration instance that can be manipulated
	 */
	private FileConfiguration config;
	/**
	 * The file that the config is saved to
	 */
	private File configFile;
	/**
	 * The folder that the config is saved in
	 */
	private final File folder;
	/**
	 * The name of the config file
	 */
	private final String fileName;
	/**
	 * Where the config is saved in the JAR
	 */
	private final String resourceFolder;
	/**
	 * Plugin that owns the config
	 */
	private final Plugin plugin;
	
	public CustomConfig(Plugin plugin, FileConfiguration config, File folder, String fileName) {
		this(plugin, config, "", folder, fileName);
	}
	
	public CustomConfig(Plugin plugin, File folder, String fileName) {
		this(plugin, "", folder, fileName);
	}
	
	public CustomConfig(Plugin plugin, FileConfiguration config, String resourceFolder, File folder, String fileName) {
		Validate.notNull(plugin, "Plugin cannot be null");
		Validate.notNull(config, "Configuration cannot be null");
		Validate.notNull(resourceFolder, "Resource folder cannot be null");
		Validate.notNull(folder, "Folder cannot be null");
		Validate.notEmpty(fileName, "File name cannot be null");
		this.plugin = plugin;
		this.config = config;
		this.resourceFolder = resourceFolder;
		this.folder = folder;
		this.fileName = fileName;
		this.configFile = new File(folder, fileName);
	}
	
	public CustomConfig(Plugin plugin, String resourceFolder, File folder, String fileName) {
		Validate.notNull(plugin, "Plugin cannot be null");
		Validate.notNull(resourceFolder, "Resource folder cannot be null");
		Validate.notNull(folder, "Folder cannot be null");
		Validate.notEmpty(fileName, "File name cannot be null");
		this.plugin = plugin;
		this.resourceFolder = resourceFolder;
		this.folder = folder;
		this.fileName = fileName;
		this.configFile = new File(folder, fileName);
		saveDefaultConfig();
		reloadConfig();
	}
	
	/**
	 * Get the plugin that owns this config
	 * 
	 * @return The owning plugin
	 */
	public Plugin getPlugin() {
		return plugin;
	}
	
	/**
	 * Reload the locations.yml configuration file
	 * 
	 * @return Whether the config was successfully reloaded
	 */
	public boolean reloadConfig() {
		if (configFile == null)
			configFile = new File(folder, fileName);
		config = YamlConfiguration.loadConfiguration(new File(folder, fileName));
		Reader defConfigStream = null;
		try {
			defConfigStream = new InputStreamReader(MinigameManager.getPlugin().getResource(resourceFolder + fileName), "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			config.setDefaults(defConfig);
			return true;
		}
		return false;
	}
	
	/**
	 * Get the instance of FileConfiguration that handles the config
	 * 
	 * @return An instance of FileConfiguration representing the config
	 */
	public FileConfiguration getConfig() {
		if (config == null)
			reloadConfig();
		return config;
	}
	
	/**
	 * Save any changes to the config to disk
	 */
	public void saveConfig() {
		if (config == null || configFile == null)
			return;
		try {
			getConfig().save(configFile);
		} catch (IOException ex) {
			plugin.getLogger().log(Level.SEVERE, "[MinigameManager] [Config] Could not save config to " + configFile, ex);
		}
	}
	
	/**
	 * Save the default config if the config file does not exist
	 * 
	 * @return Whether the config was saved
	 */
	public boolean saveDefaultConfig() {
		if (configFile == null) {
			folder.mkdirs();
			configFile = new File(folder, fileName);
		}
		if (!configFile.exists()) {
			try {
				plugin.saveResource(resourceFolder + fileName, false);
			} catch(Throwable t) {
				try {
					configFile.createNewFile();
				} catch(Throwable t2) {
					return false;
				}
			}
		} else
			return false;
		return true;
	}
	
}
