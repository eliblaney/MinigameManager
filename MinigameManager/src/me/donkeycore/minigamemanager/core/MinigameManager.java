package me.donkeycore.minigamemanager.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.event.Event;

import me.donkeycore.minigamemanager.api.minigame.Minigame;
import me.donkeycore.minigamemanager.api.minigame.Minigame.EventListener;
import me.donkeycore.minigamemanager.api.minigame.MinigameAttributes;
import me.donkeycore.minigamemanager.api.rotation.RotationManager;
import me.donkeycore.minigamemanager.config.MinigameLocations;
import me.donkeycore.minigamemanager.config.MinigameSettings;
import me.donkeycore.minigamemanager.listeners.MinigameListener;

/*
 * TODO:
 * - sign support
 * - stuff to make minigames EASIER to make
 * - config options:
 * * server-wide (join rotation on join server, leave rotation on quit server)
 * - beta test at some point
 * - future: create entire minigame from config or lua/python
 * - gems:
 * * rename-able
 * * minigames can customize amounts after each game per player
 * * config:
 * # format ($x, x gems)
 * # boolean: use vault currency as gems
 * - scoreboard:
 * * set scoreboard lines using string array
 * * lobby scoreboard
 * - commands to edit locations
 * - ELO rating system
 * - multiserver support
 * - multilanguage support
 */
/**
 * Main MinigameManager plugin class with API methods
 * 
 * @author DonkeyCore
 * @version 0.0.1
 */
public final class MinigameManager {
	
	/**
	 * The instance of MinigameManager
	 */
	static MinigameManager instance = null;
	/**
	 * The rotation manager to be used
	 */
	RotationManager rotationManager;
	/**
	 * The config for MinigameManager
	 */
	MinigameSettings config;
	/**
	 * The locations for lobbies, spawn, and minigame spawns
	 */
	MinigameLocations locations;
	/**
	 * The listener for health/hunger changes for minigames/lobbies
	 */
	MinigameListener listener;
	/**
	 * The list of minigames requesting events
	 */
	List<ListenerEntry> listeners = new ArrayList<>();
	/**
	 * The plugin owning MinigameManager
	 */
	private static MinigameManagerPlugin plugin;
	/**
	 * The list of minigames as well as their minimum player requirement
	 */
	private final Map<Class<? extends Minigame>, Integer> minigames = new HashMap<>();
	
	/**
	 * Create a new instance of MinigameManager
	 * 
	 * @param plugin The plugin instance to use
	 */
	MinigameManager(MinigameManagerPlugin plugin) {
		if (instance != null)
			throw new IllegalStateException("MinigameManager has already been initialized!");
		MinigameManager.plugin = plugin;
		MinigameManager.instance = this;
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
	 * Check if the current version of MinigameManager is a release. Should
	 * always be true for public distribution, and false for testing.
	 * 
	 * @return Whether the current version is a release or a testing version
	 */
	public static boolean isRelease() {
		return false;
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
	 * Get the listener for minigames
	 * 
	 * @return An instance of MinigameListener being used to send data to
	 *         minigames
	 */
	public MinigameListener getListener() {
		return listener;
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
	 * Add listeners for a minigame for a certain event
	 * 
	 * @param minigame The minigame that hosts this listener
	 * @param event The event to listen for
	 * @param listener What to do when the event happens
	 */
	public void addListener(Minigame minigame, Class<? extends Event> event, EventListener<? extends Event> listener) {
		Validate.notNull(minigame, "Minigame may not be null!");
		Validate.notNull(event, "Event may not be null!");
		Validate.notNull(listener, "Listener may not be null!");
		listeners.add(new ListenerEntry(minigame, event, listener));
	}
	
	/**
	 * Clear all listeners for a certain minigame
	 * 
	 * @param minigame The minigame to clear listeners forO
	 */
	public void clearListeners(Minigame minigame) {
		Iterator<ListenerEntry> it = listeners.iterator();
		while (it.hasNext()) {
			ListenerEntry e = it.next();
			if (e.minigame.equals(minigame))
				it.remove();
		}
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
	
	/**
	 * Represents a minigame that is listening to an event
	 * 
 	 * @author DonkeyCore
	 */
	public static class ListenerEntry {
		
		public final Minigame minigame;
		public final Class<? extends Event> event;
		public final EventListener<? extends Event> listener;
		
		public ListenerEntry(Minigame minigame, Class<? extends Event> event, EventListener<? extends Event> listener) {
			this.minigame = minigame;
			this.event = event;
			this.listener = listener;
		}
		
	}
	
}
