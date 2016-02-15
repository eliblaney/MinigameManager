package me.donkeycore.minigamemanager.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Configuration file for minigames
 * 
 * @author DonkeyCore
 */
public class MinigameConfig {
	
	/**
	 * The JavaPlugin owning the config
	 */
	private final JavaPlugin plugin;
	/**
	 * The FileConfiguration instance that can be manipulated
	 */
	private FileConfiguration config;
	/**
	 * The file that the config is saved to
	 */
	private File configFile;
	/**
	 * The name of the config file
	 */
	private String fileName;
	
	/**
	 * Create a new instance of MinigameConfig for an existing FileConfiguration
	 * 
	 * @param plugin The plugin that should own this config
	 * @param config The FileConfiguration to be used
	 * @param configFile The file to be saved as
	 */
	public MinigameConfig(JavaPlugin plugin, FileConfiguration config, File configFile) {
		this.plugin = plugin;
		this.config = config;
		this.configFile = configFile;
		this.fileName = configFile.getName();
	}
	
	/**
	 * Create a new instance of MinigameConfig
	 * 
	 * @param plugin The plugin that should own this config
	 * @param configFile The file to be saved as
	 */
	public MinigameConfig(JavaPlugin plugin, File configFile) {
		this.plugin = plugin;
		this.configFile = configFile;
		this.fileName = configFile.getName();
		reloadConfig();
	}
	
	/**
	 * Reload the configuration file
	 */
	public void reloadConfig() {
		if (configFile == null)
			configFile = new File(plugin.getDataFolder(), fileName);
		config = YamlConfiguration.loadConfiguration(configFile);
		Reader defConfigStream = null;
		try {
			defConfigStream = new InputStreamReader(plugin.getResource(fileName), "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			config.setDefaults(defConfig);
		}
	}
	
	/**
	 * Get the configuration file, ready for editing
	 * 
	 * @return An instance of {@link FileConfiguration} representing the config
	 */
	public FileConfiguration getConfig() {
		if (config == null)
			reloadConfig();
		return config;
	}
	
	/**
	 * Save any changes to the config made by the plugin
	 */
	public void saveConfig() {
		if (config == null || configFile == null)
			return;
		try {
			getConfig().save(configFile);
		} catch (IOException ex) {
			plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, ex);
		}
	}
	
	/**
	 * Save the default config if the config file has not been created yet
	 */
	public void saveDefaultConfig() {
		if (configFile == null)
			configFile = new File(plugin.getDataFolder(), fileName);
		if (!configFile.exists())
			plugin.saveResource(fileName, false);
	}
	
	/**
	 * Get the locations for the minigame spawns, as specified by the config
	 * 
	 * @param map The map name that spawns are located in
	 * 
	 * @return An array of {@link Location} objects for each spawn location
	 */
	public Location[] getMinigameSpawns(String map) {
		ConfigurationSection spawns = getConfig().getConfigurationSection(map).getConfigurationSection("spawns");
		Set<String> keys = spawns.getKeys(false);
		Location[] locations = new Location[keys.size() - 1];
		int i = 0;
		for (String key : keys) {
			if (key.equalsIgnoreCase("mapinfo"))
				continue;
			ConfigurationSection cs = spawns.getConfigurationSection(key);
			World world = Bukkit.getWorld(cs.getString("world"));
			if (world == null)
				throw new RuntimeException("Invalid world for " + key);
			double x = cs.getDouble("x");
			double y = cs.getDouble("y");
			double z = cs.getDouble("z");
			float yaw = Float.parseFloat(cs.getString("yaw"));
			float pitch = Float.parseFloat(cs.getString("pitch"));
			locations[i++] = new Location(world, x, y, z, yaw, pitch);
		}
		return locations;
	}
	
	/**
	 * Get the map info information for the arena
	 * 
	 * @param map The map corresponding with the mapinfo
	 * 
	 * @return An array of length 2 with the name of the arena in the first
	 *         index and the author(s) in the second
	 */
	public String[] getMapInfo(String map) {
		ConfigurationSection kcs = getConfig().getConfigurationSection(map);
		if (kcs == null)
			throw new IllegalArgumentException(map + " is not a valid map");
		ConfigurationSection cs = kcs.getConfigurationSection("mapinfo");
		if (cs == null)
			return new String[0];
		String[] mapinfo = new String[2];
		mapinfo[0] = cs.getString("name");
		mapinfo[1] = cs.getString("author");
		return mapinfo;
	}
	
}
