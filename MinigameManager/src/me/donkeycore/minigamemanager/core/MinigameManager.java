package me.donkeycore.minigamemanager.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;

import me.donkeycore.minigamemanager.api.minigame.Minigame;
import me.donkeycore.minigamemanager.api.minigame.MinigameAttributes;
import me.donkeycore.minigamemanager.api.rotation.RotationManager;
import me.donkeycore.minigamemanager.api.rotation.SubstitutionHandler;
import me.donkeycore.minigamemanager.config.MinigameLocations;
import me.donkeycore.minigamemanager.config.MinigameSettings;

/*
 * TODO:
 * - sign support
 * - team API, chest API (and other minigame APIs) in the
 * me.donkeycore.minigamemanager.api.minigame package
 * - stuff to make minigames EASIER to make
 * - config options:
 * * server-wide (join rotation on join server, leave rotation on quit server)
 * - beta test at some point
 * - future: create entire minigame from config or lua/python
 * - gems:
 * * config:
 * # format ($x, x gems)
 * # boolean: use vault currency as gems
 * - scoreboard:
 * * set scoreboard lines using string array
 * - ELO rating system
 */
/**
 * Main MinigameManager plugin class with API methods
 * 
 * @author DonkeyCore
 * @version 0.0.1
 */
public final class MinigameManager {
	
	static MinigameManager instance = null;
	MinigameSettings config;
	MinigameLocations locations;
	RotationManager rotationManager;
	private static MinigameManagerPlugin plugin;
	private final Map<Class<? extends Minigame>, Integer> minigames = new HashMap<>();
	
	static {
		SubstitutionHandler.getInstance();
	}
	
	MinigameManager(MinigameManagerPlugin plugin) {
		MinigameManager.plugin = plugin;
		if (instance != null)
			throw new IllegalStateException("MinigameManager has already been initialized!");
		instance = this;
	}
	
	/**
	 * Get the instance of this class if the plugin has been enabled (null if
	 * disabled)
	 * 
	 * @return The instance of {@link MinigameManager}
	 */
	public static MinigameManager getMinigameManager() {
		return instance;
	}
	
	/**
	 * Get the instance of the plugin if it has been enabled (null if disabled)
	 * 
	 * @return The instance of {@link MinigameManagerPlugin}
	 */
	public static MinigameManagerPlugin getPlugin() {
		return plugin;
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
	 * <br>
	 * <b>Note:</b> The class <i>must</i> have a {@link MinigameAttributes}
	 * annotation
	 * 
	 * @param minigame The minigame to register
	 * @param minimumPlayers The minimum players required to start this minigame
	 */
	public void registerMinigame(Class<? extends Minigame> minigame, int minimumPlayers) {
		Validate.notNull(minigame, "Minigame must not be null");
		Validate.isTrue(minimumPlayers > 0, "Minimum players must be above 0");
		Validate.notNull(minigame.getAnnotation(MinigameAttributes.class), "Minigame must have a @MinigameAttributes annotation");
		this.minigames.put(minigame, minimumPlayers);
		plugin.getLogger().info("Registered: " + minigame.getSimpleName());
	}
	
	/**
	 * Unregister a minigame with MinigameManager and remove it from the
	 * rotations
	 * 
	 * @param minigame The minigame to unregister
	 * @return Whether the minigame was removed
	 */
	public boolean unregisterMinigame(Class<? extends Minigame> minigame) {
		Validate.notNull(minigame);
		boolean b = this.minigames.remove(minigame) != null;
		if (b)
			plugin.getLogger().info("Unregistered: " + minigame.getSimpleName());
		return b;
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
