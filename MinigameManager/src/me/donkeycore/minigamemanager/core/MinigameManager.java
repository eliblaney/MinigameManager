package me.donkeycore.minigamemanager.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.plugin.java.JavaPlugin;

import me.donkeycore.minigamemanager.api.Minigame;
import me.donkeycore.minigamemanager.api.RotationManager;
import me.donkeycore.minigamemanager.api.SubstitutionHandler;
import me.donkeycore.minigamemanager.config.MinigameLocations;
import me.donkeycore.minigamemanager.config.MinigameSettings;

/*
 * TODO:
 * - /mm [help|info minigame|start #|stop #|reload]
 *   - info minigame: display information from the MinigameAttributes annotation
 * - sign support
 */
/**
 * Main MinigameManager plugin class with API methods
 * 
 * @author DonkeyCore
 * @version 0.1
 */
public final class MinigameManager extends JavaPlugin {
	
	static MinigameManager instance = null;
	MinigameSettings config;
	MinigameLocations locations;
	RotationManager rotationManager;
	private final Map<Class<? extends Minigame>, Integer> minigames = new HashMap<>();
	
	static {
		SubstitutionHandler.getInstance();
	}
	
	MinigameManager() {
		if (instance != null)
			throw new IllegalStateException("MinigameManager has already been initialized!");
	}
	
	/**
	 * Get the instance of this class if it has been enabled (null if disabled)
	 * 
	 * @return The instance of {@link MinigameManager}
	 */
	public static MinigameManager getMinigameManager() {
		return instance;
	}
	
	/**
	 * Get the rotation manager for working with rotations
	 * 
	 * @return An instance of {@link RotationManager}
	 */
	public RotationManager getRotationManager() {
		return rotationManager;
	}
	
	/**
	 * Get the configuration class for the plugin
	 * 
	 * @return An instance of {@link MinigameSettings}
	 */
	public MinigameSettings getMinigameConfig() {
		return config;
	}
	
	/**
	 * Get the configuration class for rotation locations and the default
	 * minigames' locations
	 * 
	 * @return An instance of {@link MinigameLocations}
	 */
	public MinigameLocations getMinigameLocations() {
		return locations;
	}
	
	/**
	 * Register a minigame with MinigameManager and insert it into the rotations
	 * 
	 * @param minigame The minigame to register
	 * @param minimumPlayers The minimum players required to start this minigame
	 */
	public void registerMinigame(Class<? extends Minigame> minigame, int minimumPlayers) {
		this.minigames.put(minigame, minimumPlayers);
	}
	
	/**
	 * Get a set of minigame classes that have been registered
	 * 
	 * @return A {@link Set} of minigames that have been registered
	 */
	public Set<Class<? extends Minigame>> getMinigames() {
		return minigames.keySet();
	}
	
	/**
	 * Get a map of minigame classes with their corresponding minimum player
	 * values
	 * 
	 * @return A {@link Map} of minigame classes with minimum player count
	 *         values
	 */
	public Map<Class<? extends Minigame>, Integer> getMinigamesWithMinimums() {
		Map<Class<? extends Minigame>, Integer> m = new HashMap<>();
		m.putAll(minigames);
		return m;
	}
	
}
