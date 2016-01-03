package me.donkeycore.minigamemanager.core;

import java.lang.reflect.InvocationTargetException;

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

public class MinigameManagerPlugin extends JavaPlugin {
	
	private MinigameManager manager;
	
	/**
	 * <b>Bukkit implementation method</b><br>
	 * Do not call this
	 */
	public void onEnable() {
		if (manager != null)
			throw new IllegalStateException("MinigameManager has already been enabled!");
		manager = new MinigameManager();
		PluginDescriptionFile description = getDescription();
		getLogger().info("Enabling " + description.getName() + " v" + description.getVersion() + "...");
		getLogger().info("Initializing config... (Part 1: General)");
		saveDefaultConfig();
		manager.config = new MinigameSettings(manager);
		getLogger().info("Initializing config... (Part 2: Locations)");
		manager.locations = new MinigameLocations(manager);
		getLogger().info("Registering commands...");
		getCommand("minigamemanager").setExecutor(new CommandMinigame(manager));
		getCommand("join").setExecutor(new CommandJoin(manager));
		getCommand("leave").setExecutor(new CommandLeave(manager));
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
		if (manager.config.defaultsEnabled()) {
			getLogger().info("Registering default minigames...");
			for (String minigameStr : manager.config.getEnabledDefaultMinigames()) {
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
				manager.registerMinigame(minigameClass, manager.config.getMinimumForMinigame(minigameStr));
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
		if (manager == null)
			throw new IllegalStateException("MinigameManager has not been enabled!");
		manager.rotationManager.shutdown();
		// Prepare everything for shutdown
		getLogger().info(getDescription().getName() + " v" + getDescription().getVersion() + " by DonkeyCore has been disabled!");
	}
	
}
