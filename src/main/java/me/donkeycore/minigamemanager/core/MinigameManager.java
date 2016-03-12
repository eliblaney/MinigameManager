package me.donkeycore.minigamemanager.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;

import me.donkeycore.minigamemanager.api.minigame.Minigame;
import me.donkeycore.minigamemanager.api.minigame.Minigame.EventListener;
import me.donkeycore.minigamemanager.api.minigame.MinigameAttributes;
import me.donkeycore.minigamemanager.api.rotation.RotationManager;
import me.donkeycore.minigamemanager.config.MinigameLocations;
import me.donkeycore.minigamemanager.config.MinigameMessages;
import me.donkeycore.minigamemanager.config.MinigameSettings;
import me.donkeycore.minigamemanager.config.PlayerProfileConfiguration;
import me.donkeycore.minigamemanager.events.minigame.MinigameRegisterEvent;
import me.donkeycore.minigamemanager.events.minigame.MinigameUnregisterEvent;
import net.milkbowl.vault.economy.Economy;

/*
 * TODO:
 * before release:
 * - commands to edit locations
 * - multiserver support
 * future:
 * - create entire minigame from config or lua/python
 * - more things that make minigames easier to make
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
	 * All messages, translated in correct languages
	 */
	MinigameMessages messages;
	/**
	 * The locations for lobbies, spawn, and minigame spawns
	 */
	MinigameLocations locations;
	/**
	 * The configuration for player profiles
	 */
	PlayerProfileConfiguration profileConf;
	/**
	 * Vault economy. Null if disabled.
	 */
	Economy economy;
	/**
	 * Whether vault economy is enabled
	 */
	boolean vaultEcon = false;
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
	 * Get the vault economy
	 * 
	 * @return Vault's economy instance, or null if disabled
	 */
	public Economy getVaultEconomy() {
		return economy;
	}
	
	/**
	 * Whether to use Vault economy or not
	 * 
	 * @return Whether to use Vault economy
	 */
	public boolean useVaultEconomy() {
		return vaultEcon;
	}
	
	/**
	 * Get the configuration class for the plugin
	 * 
	 * @return An instance of {@link MinigameSettings}
	 */
	public MinigameSettings getMinigameSettings() {
		return config;
	}
	
	/**
	 * Get the configuration for messages translated to other languages
	 * 
	 * @return An instance of {@link MinigameMessages}
	 */
	public MinigameMessages getMessages() {
		return messages;
	}
	
	/**
	 * Get the configuration class for rotation locations and the default
	 * minigames' locations
	 * 
	 * @return An instance of {@link MinigameLocations}
	 */
	public MinigameLocations getDefaultMinigameLocations() {
		return locations;
	}
	
	/**
	 * Get the configuration for player profiles
	 * 
	 * @return The PlayerProfileConfiguration instance
	 */
	public PlayerProfileConfiguration getPlayerProfileConfig() {
		return profileConf;
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
		Bukkit.getPluginManager().callEvent(new MinigameRegisterEvent(minigame, minimumPlayers));
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
		if (b) {
			Bukkit.getPluginManager().callEvent(new MinigameUnregisterEvent(minigame));
			plugin.getLogger().info("Unregistered: " + minigame.getSimpleName());
		}
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
