package me.donkeycore.minigamemanager.core;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import me.donkeycore.minigamemanager.api.minigame.Minigame;
import me.donkeycore.minigamemanager.api.rotation.Rotation;
import me.donkeycore.minigamemanager.api.rotation.RotationManager;
import me.donkeycore.minigamemanager.api.rotation.SubstitutionHandler;
import me.donkeycore.minigamemanager.commands.CommandJoin;
import me.donkeycore.minigamemanager.commands.CommandLeave;
import me.donkeycore.minigamemanager.commands.CommandMinigame;
import me.donkeycore.minigamemanager.config.MinigameLocations;
import me.donkeycore.minigamemanager.config.MinigameSettings;
import me.donkeycore.minigamemanager.listeners.MinigameListener;
import me.donkeycore.minigamemanager.listeners.QuitListener;
import me.donkeycore.minigamemanager.minigames.DefaultMinigame;
import me.donkeycore.minigamemanager.rotations.DefaultRotationManager;

public class MinigameManagerPlugin extends JavaPlugin {
	
	private MinigameManager manager;
	
	/**
	 * <b>Bukkit implementation method</b><br>
	 * Do not call this
	 */
	public void onEnable() {
		if (manager != null)
			throw new IllegalStateException("MinigameManager has already been enabled!");
		manager = new MinigameManager(this);
		PluginDescriptionFile description = getDescription();
		getLogger().info("Enabling " + description.getName() + " v" + description.getVersion() + "...");
		getLogger().info("Initializing config... (Part 1: General)");
		saveDefaultConfig();
		manager.config = new MinigameSettings();
		getLogger().info("Initializing config... (Part 2: Locations)");
		manager.locations = new MinigameLocations();
		getLogger().info("Registering commands...");
		getCommand("minigamemanager").setExecutor(new CommandMinigame(manager));
		getCommand("join").setExecutor(new CommandJoin(manager));
		getCommand("leave").setExecutor(new CommandLeave(manager));
		getLogger().info("Registering listeners...");
		Bukkit.getPluginManager().registerEvents(new QuitListener(manager), this);
		Bukkit.getPluginManager().registerEvents(new MinigameListener(manager), this);
		getLogger().info("Creating rotation manager...");
		Class<? extends RotationManager> rmClass = SubstitutionHandler.getInstance().getRotationManager();
		try {
			manager.rotationManager = rmClass.getConstructor(MinigameManager.class, int.class).newInstance(manager, manager.config.getNumberOfRotations());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			getLogger().warning("Could not load rotation manager: " + rmClass.getSimpleName() + ", using DefaultRotationManager instead.");
			manager.rotationManager = new DefaultRotationManager(manager, manager.config.getNumberOfRotations());
		}
		// Start the rotations after all plugins are finished loading
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				for (Rotation rotation : manager.rotationManager.getRotations())
					manager.rotationManager.chooseMinigame(rotation);
			}
		});
		loadDefaultMinigames();
		getLogger().info(description.getName() + " v" + description.getVersion() + " by DonkeyCore has been enabled!");
	}
	
	/**
	 * <b>Bukkit implementation method</b><br>
	 * Do not call this
	 */
	public void onDisable() {
		if (manager == null)
			throw new IllegalStateException("MinigameManager has not been enabled!");
		manager.rotationManager.shutdown();
		// Prepare everything for shutdown
		getLogger().info(getDescription().getName() + " v" + getDescription().getVersion() + " by DonkeyCore has been disabled!");
	}
	
	/**
	 * Insert the default minigames into the rotation. Automatically called on
	 * enable and '/mm reload'
	 */
	public void loadDefaultMinigames() {
		if (manager.config.defaultsEnabled()) {
			// load minigames from the config
			getLogger().info("Registering default minigames...");
			for (String minigameStr : manager.config.getDefaultMinigames()) {
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
				if (manager.config.getEnabledDefaultMinigames().contains(minigameStr))
					manager.registerMinigame(minigameClass, manager.config.getMinimumForMinigame(minigameStr));
				else
					manager.unregisterMinigame(minigameClass);
			}
		} else {
			// disable any existing ones (in case of config reload)
			for (Class<? extends Minigame> minigame : manager.getMinigames()) {
				if (minigame.getAnnotation(DefaultMinigame.class) != null)
					manager.unregisterMinigame(minigame);
			}
		}
	}
	
}
