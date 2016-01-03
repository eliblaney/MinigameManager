package me.donkeycore.minigamemanager.core;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import me.donkeycore.minigamemanager.api.Minigame;
import me.donkeycore.minigamemanager.api.Rotation;
import me.donkeycore.minigamemanager.api.RotationManager;
import me.donkeycore.minigamemanager.api.SubstitutionHandler;
import me.donkeycore.minigamemanager.commands.CommandJoin;
import me.donkeycore.minigamemanager.commands.CommandLeave;
import me.donkeycore.minigamemanager.commands.CommandMinigame;
import me.donkeycore.minigamemanager.config.MinigameLocations;
import me.donkeycore.minigamemanager.config.MinigameSettings;
import me.donkeycore.minigamemanager.rotations.DefaultRotationManager;

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
	
	private static MinigameManager instance = null;
	private MinigameSettings config;
	private MinigameLocations locations;
	private RotationManager rotationManager;
	private final Map<Class<? extends Minigame>, Integer> minigames = new HashMap<>();
	
	static {
		SubstitutionHandler.getInstance();
	}
	
	/**
	 * <b>Bukkit implementation method</b><br>
	 * Do not call this
	 */
	public MinigameManager() {
		if (instance != null)
			throw new IllegalStateException("MinigameManager has already been initialized!");
	}
	
	/**
	 * <b>Bukkit implementation method</b><br>
	 * Do not call this
	 */
	public void onEnable() {
		if (instance != null)
			throw new IllegalStateException("MinigameManager has already been enabled!");
		instance = this;
		PluginDescriptionFile description = getDescription();
		getLogger().info("Enabling " + description.getName() + " v" + description.getVersion() + "...");
		getLogger().info("Initializing config... (Part 1: General)");
		saveDefaultConfig();
		this.config = new MinigameSettings(this);
		getLogger().info("Initializing config... (Part 2: Locations)");
		this.locations = new MinigameLocations(this);
		getLogger().info("Registering commands...");
		getCommand("minigamemanager").setExecutor(new CommandMinigame(this));
		getCommand("join").setExecutor(new CommandJoin(this));
		getCommand("leave").setExecutor(new CommandLeave(this));
		getLogger().info("Creating rotation manager...");
		Class<? extends RotationManager> rmClass = SubstitutionHandler.getInstance().getRotationManager();
		try {
			rotationManager = rmClass.getConstructor(MinigameManager.class, int.class).newInstance(this, config.getNumberOfRotations());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			getLogger().warning("Could not load rotation manager: " + rmClass.getSimpleName() + ", using DefaultRotationManager instead.");
			rotationManager = new DefaultRotationManager(this, config.getNumberOfRotations());
		}
		// Start the rotations after all plugins are finished loading
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				for (Rotation rotation : rotationManager.getRotations())
					rotationManager.chooseMinigame(rotation);
			}
		});
		if (config.defaultsEnabled()) {
			getLogger().info("Registering default minigames...");
			for (String minigameStr : config.getEnabledDefaultMinigames()) {
				Class<?> clazz = null;
				try {
					clazz = Class.forName("me.donkeycore.minigamemanager.minigames." + minigameStr);
				} catch (ClassNotFoundException e) {
					getLogger().warning("Uh oh! " + minigameStr + " is not a default minigame. Skipping!");
					continue;
				}
				if (clazz.getSuperclass() != Minigame.class) {
					getLogger().warning(minigameStr + " is not a minigame. Skipping!");
					continue;
				}
				@SuppressWarnings("unchecked")
				Class<? extends Minigame> minigameClass = (Class<? extends Minigame>) clazz;
				registerMinigame(minigameClass, config.getMinimumForMinigame(minigameStr));
				getLogger().info("Registered: " + minigameStr);
			}
		}
		getLogger().info(description.getName() + " v" + description.getVersion() + " by DonkeyCore has been enabled!");
	}
	
	/**
	 * <b>Bukkit implementation method</b><br>
	 * Do not call this
	 */
	public void onDisable() {
		if (instance == null)
			throw new IllegalStateException("MinigameManager has not been enabled!");
		rotationManager.shutdown();
		// Prepare everything for shutdown
		getLogger().info(getDescription().getName() + " v" + getDescription().getVersion() + " by DonkeyCore has been disabled!");
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
