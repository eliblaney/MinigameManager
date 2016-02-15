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

import me.donkeycore.minigamemanager.core.MinigameManager;

/**
 * Class for finding locations of spawns, lobbies, and default minigames
 * 
 * @author DonkeyCore
 */
public class MinigameLocations {
	
	/**
	 * The FileConfiguration instance that can be manipulated
	 */
	private FileConfiguration config;
	/**
	 * The file that the config is saved to
	 */
	private File configFile;
	
	/**
	 * Create a new instance of MinigameLocations
	 */
	public MinigameLocations() {
		saveDefaultConfig();
		reloadConfig();
	}
	
	/**
	 * Reload the locations.yml configuration file
	 */
	public void reloadConfig() {
		if (configFile == null)
			configFile = new File(MinigameManager.getPlugin().getDataFolder(), "locations.yml");
		config = YamlConfiguration.loadConfiguration(configFile);
		Reader defConfigStream = null;
		try {
			defConfigStream = new InputStreamReader(MinigameManager.getPlugin().getResource("locations.yml"), "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			config.setDefaults(defConfig);
		}
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
			MinigameManager.getPlugin().getLogger().log(Level.SEVERE, "Could not save config to " + configFile, ex);
		}
	}
	
	/**
	 * Save the default config if the config file does not exist
	 */
	public void saveDefaultConfig() {
		if (configFile == null)
			configFile = new File(MinigameManager.getPlugin().getDataFolder(), "locations.yml");
		if (!configFile.exists())
			MinigameManager.getPlugin().saveResource("locations.yml", false);
	}
	
	/**
	 * Get the specified rotation location
	 * 
	 * @param key The type of location to retrieve
	 * 			
	 * @return A location for the specified key
	 */
	public Location getRotationLocation(String key) {
		ConfigurationSection cs = getConfig().getConfigurationSection("rotations").getConfigurationSection(key);
		if (cs == null)
			throw new IllegalArgumentException(key + " is not a valid key for a location");
		World world = Bukkit.getWorld(cs.getString("world"));
		if (world == null)
			throw new RuntimeException("Invalid world for " + key);
		double x = cs.getDouble("x");
		double y = cs.getDouble("y");
		double z = cs.getDouble("z");
		float yaw = Float.parseFloat(cs.getString("yaw"));
		float pitch = Float.parseFloat(cs.getString("pitch"));
		return new Location(world, x, y, z, yaw, pitch);
	}
	
	/**
	 * Get an array of Locations representing spawn locations for a default
	 * minigame
	 * 
	 * @param minigame The minigame to look for (must be a default minigame!)
	 * @param map The map name that the spawns are located in
	 * 			
	 * @return An array of Location elements representing spawns
	 */
	public Location[] getMinigameSpawns(String minigame, String map) {
		ConfigurationSection mcs = getConfig().getConfigurationSection("default-minigames").getConfigurationSection(minigame);
		if (mcs == null)
			throw new IllegalArgumentException(minigame + " is not a valid default minigame");
		ConfigurationSection mapcs = mcs.getConfigurationSection(map);
		if(mapcs == null)
			throw new IllegalArgumentException(map + " is not a valid map");
		ConfigurationSection spawns = mapcs.getConfigurationSection("spawns");
		Set<String> keys = spawns.getKeys(false);
		Location[] locations = new Location[keys.size()];
		int i = 0;
		for (String key : keys) {
			ConfigurationSection cs = spawns.getConfigurationSection(key);
			World world = Bukkit.getWorld(cs.getString("world"));
			if (world == null)
				throw new RuntimeException("Invalid world for " + key + " (from " + minigame + " spawns)");
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
	 * Get the map info for a default minigame's specified map
	 * 
	 * @param minigame The minigame to look for (must be default!)
	 * @param map The map corresponding with the mapinfo
	 * 			
	 * @return A string array of length 2 with the name in the first index and the author in the second
	 */
	public String[] getMapInfo(String minigame, String map) {
		ConfigurationSection mcs = getConfig().getConfigurationSection("default-minigames").getConfigurationSection(minigame);
		if (mcs == null)
			throw new IllegalArgumentException(minigame + " is not a valid default minigame");
		ConfigurationSection kcs = mcs.getConfigurationSection(map);
		if (kcs == null)
			throw new IllegalArgumentException(map + " is not a valid map (minigame: " + minigame + ")");
		ConfigurationSection cs = kcs.getConfigurationSection("mapinfo");
		if (cs == null)
			return new String[0];
		String[] mapinfo = new String[2];
		mapinfo[0] = cs.getString("name");
		mapinfo[1] = cs.getString("author");
		return mapinfo;
	}
	
}
