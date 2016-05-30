package minigamemanager.api.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import minigamemanager.core.MinigameManager;
import minigamemanager.core.MinigameManagerPlugin;

/**
 * Represents a configuration file
 * 
 * @author DonkeyCore
 */
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
	 * Where the config is saved in the JAR, or the template if specified
	 */
	private final String resources;
	/**
	 * Whether the resources variable is actually a reference to a template
	 * config
	 */
	private final boolean template;
	/**
	 * Plugin that owns the config
	 */
	private final Plugin plugin;
	
	/**
	 * Load an existing configuration
	 * 
	 * @param plugin The plugin that owns this config
	 * @param config The existing config
	 * @param folder The folder that the config is saved in
	 * @param fileName The name of the config file
	 */
	public CustomConfig(Plugin plugin, FileConfiguration config, File folder, String fileName) {
		this(plugin, config, "", false, folder, fileName);
	}
	
	/**
	 * Create a new configuration
	 * 
	 * @param plugin The plugin that owns this config
	 * @param folder The folder that the config is saved in
	 * @param fileName The name of the config file
	 */
	public CustomConfig(Plugin plugin, File folder, String fileName) {
		this(plugin, "", false, folder, fileName);
	}
	
	/**
	 * Load an existing configuration
	 * 
	 * @param plugin The plugin that owns this config
	 * @param config The existing config
	 * @param resources Where the config is saved in the JAR, or the template if
	 *            specified
	 * @param template Whether the resources variable is actually a reference to
	 *            a template config
	 * @param folder The folder that the config is saved in
	 * @param fileName The name of the config file
	 */
	public CustomConfig(Plugin plugin, FileConfiguration config, String resources, boolean template, File folder, String fileName) {
		Validate.notNull(plugin, "Plugin cannot be null");
		Validate.notNull(config, "Configuration cannot be null");
		Validate.notNull(resources, "Resource folder cannot be null");
		Validate.notNull(folder, "Folder cannot be null");
		Validate.notEmpty(fileName, "File name cannot be null");
		this.plugin = plugin;
		this.config = config;
		this.resources = resources;
		this.template = template;
		this.folder = folder;
		this.fileName = fileName;
		this.configFile = new File(folder, fileName);
	}
	
	/**
	 * Create a new configuration
	 * 
	 * @param plugin The plugin that owns this config
	 * @param resoures Where the config is saved in the JAR, or the template if
	 *            specified
	 * @param template Whether the resources variable is actually a reference to
	 *            a tepmlate config
	 * @param folder The folder that the config is saved in
	 * @param fileName The name of the config file
	 */
	public CustomConfig(Plugin plugin, String resoures, boolean template, File folder, String fileName) {
		Validate.notNull(plugin, "Plugin cannot be null");
		Validate.notNull(resoures, "Resource folder cannot be null");
		Validate.notNull(folder, "Folder cannot be null");
		Validate.notEmpty(fileName, "File name cannot be null");
		this.plugin = plugin;
		this.resources = resoures;
		this.template = template;
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
			InputStream resource = null;
			if (template)
				resource = MinigameManager.getPlugin().getResource(resources);
			else
				resource = MinigameManager.getPlugin().getResource(resources + fileName);
			if (resource == null)
				return true; // no template config
			defConfigStream = new InputStreamReader(resource, "UTF8");
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
			if (plugin instanceof MinigameManagerPlugin)
				plugin.getLogger().log(Level.SEVERE, "[Config] Could not save config to " + configFile, ex);
			else
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
				if (template) {
					plugin.saveResource(resources, false);
					String templateName = new File(resources).getName();
					String path = folder + File.separator;
					new File(path + templateName).renameTo(new File(path + fileName));
				} else
					plugin.saveResource(resources + fileName, false);
			} catch (Throwable t) {
				t.printStackTrace();
				try {
					configFile.createNewFile();
				} catch (Throwable t2) {
					return false;
				}
			}
		} else
			return false;
		return true;
	}
	
}
