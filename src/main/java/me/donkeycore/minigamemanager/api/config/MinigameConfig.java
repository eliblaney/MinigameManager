package me.donkeycore.minigamemanager.api.config;

import java.io.File;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import me.donkeycore.minigamemanager.api.minigame.Minigame;
import me.donkeycore.minigamemanager.api.minigame.MinigameAttributes;
import me.donkeycore.minigamemanager.core.MinigameManager;

/**
 * Configuration file for minigames
 * 
 * @author DonkeyCore
 */
public class MinigameConfig extends CustomConfig {
	
	/**
	 * Name of the resource folder and the folder on disk
	 */
	private static final String folderName = "minigame_configs";
	
	/**
	 * The class of the minigame that owns this config
	 */
	private final Class<? extends Minigame> minigame;
	
	/**
	 * Create a new instance of MinigameConfig for a specified Minigame
	 * 
	 * @param minigame The class of the minigame that owns this config
	 */
	public MinigameConfig(Class<? extends Minigame> minigame) {
		super(MinigameManager.getPlugin(), folderName + File.separator + "configTemplate.yml", true, new File(MinigameManager.getPlugin().getDataFolder() + File.separator + folderName), minigame.getAnnotation(MinigameAttributes.class).name().replace(' ', '_') + ".yml");
		this.minigame = minigame;
	}
	
	/**
	 * Get the class of the minigame that owns this config
	 * 
	 * @return The minigame class
	 */
	public Class<? extends Minigame> getMinigame() {
		return minigame;
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
