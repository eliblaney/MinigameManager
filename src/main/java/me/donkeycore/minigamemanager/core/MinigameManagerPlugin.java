package me.donkeycore.minigamemanager.core;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.donkeycore.minigamemanager.api.minigame.Minigame;
import me.donkeycore.minigamemanager.api.minigame.MinigameAttributes;
import me.donkeycore.minigamemanager.api.player.PlayerProfile;
import me.donkeycore.minigamemanager.api.rotation.Rotation;
import me.donkeycore.minigamemanager.api.rotation.RotationManager;
import me.donkeycore.minigamemanager.api.rotation.SubstitutionHandler;
import me.donkeycore.minigamemanager.commands.CommandJoin;
import me.donkeycore.minigamemanager.commands.CommandLeave;
import me.donkeycore.minigamemanager.commands.CommandMinigame;
import me.donkeycore.minigamemanager.config.MinigameLocations;
import me.donkeycore.minigamemanager.config.MinigameSettings;
import me.donkeycore.minigamemanager.config.PlayerProfileConfiguration;
import me.donkeycore.minigamemanager.listeners.JoinQuitListener;
import me.donkeycore.minigamemanager.listeners.MinigameListener;
import me.donkeycore.minigamemanager.rotations.DefaultRotationManager;
import net.milkbowl.vault.economy.Economy;

/**
 * The JavaPlugin for MinigameManager
 * 
 * @author DonkeyCore
 */
public class MinigameManagerPlugin extends JavaPlugin {
	
	/**
	 * The MinigameManager instance being used
	 */
	private MinigameManager manager;
	/**
	 * Whether the server is starting up or there is simply a plugin reload
	 */
	private boolean serverStartup = false;
	
	/**
	 * <b>Bukkit implementation method</b><br>
	 * Do not call this
	 */
	@Override
	public void onEnable() {
		if (manager != null)
			throw new IllegalStateException("MinigameManager has already been enabled!");
		manager = new MinigameManager(this);
		PluginDescriptionFile description = getDescription();
		getLogger().info("Enabling " + description.getName() + " v" + description.getVersion() + "...");
		// not as malicious as this sounds - custom plugin manager is just used to handle events more easily
		getLogger().info("Injecting custom plugin manager...");
		// get server instance and class
		Server server = Bukkit.getServer();
		Class<?> clazz = server.getClass();
		try {
			// attempt to get the pluginManager field and edit it
			Field pluginManager = clazz.getDeclaredField("pluginManager");
			pluginManager.setAccessible(true);
			// the current plugin manager will be used to do the actual processing
			PluginManager pm = (PluginManager) pluginManager.get(server);
			// there shouldn't already be a MinigamePluginManagerWrapper instance
			if (serverStartup = !pm.getClass().getName().equals(MinigamePluginManagerWrapper.class.getName()))
				// inject the wrapper so that the plugin can send all events to minigames
				pluginManager.set(server, new MinigamePluginManagerWrapper(pm));
			else {
				getLogger().info("Previous MinigamePluginManagerWrapper found, finding root...");
				while (pm.getClass().getName().equals(MinigamePluginManagerWrapper.class.getName())) {
					Field f = pm.getClass().getDeclaredField("pm");
					f.setAccessible(true);
					pm = (PluginManager) f.get(pm);
				}
				getLogger().info("Found root of type " + pm.getClass().getSimpleName());
				pluginManager.set(server, new MinigamePluginManagerWrapper(pm));
			}
		} catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
			e.printStackTrace();
			getLogger().severe("Failed to inject plugin manager! EVENTS WILL NOT WORK PROPERLY!");
		}
		getLogger().info("Initializing config... (Part 1: General)");
		saveDefaultConfig();
		manager.config = new MinigameSettings();
		getLogger().info("Initializing config... (Part 2: Locations)");
		manager.locations = new MinigameLocations();
		getLogger().info("Initializing config... (Part 3: Profiles)");
		manager.profileConf = new PlayerProfileConfiguration();
		// Optional Vault economy support
		if (manager.config.getConfig().getConfigurationSection("profiles").getBoolean("vault")) {
			if (Bukkit.getPluginManager().getPlugin("Vault") == null)
				getLogger().warning("Vault is enabled in config but the Vault plugin was not found!");
			else {
				getLogger().info("Hooking into Vault...");
				RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
				if (economyProvider != null)
					manager.economy = economyProvider.getProvider();
				if (manager.economy == null)
					getLogger().warning("Failed to hook into Vault economy! Will use internal currency instead.");
				else
					manager.vaultEcon = true;
			}
		}
		getLogger().info("Registering commands...");
		getCommand("minigamemanager").setExecutor(new CommandMinigame(manager));
		getCommand("join").setExecutor(new CommandJoin(manager));
		getCommand("leave").setExecutor(new CommandLeave(manager));
		getLogger().info("Registering listeners...");
		Bukkit.getPluginManager().registerEvents(new JoinQuitListener(manager), this);
		Bukkit.getPluginManager().registerEvents(new MinigameListener(manager), this);
		getLogger().info("Creating rotation manager...");
		// lock to prevent further editing
		SubstitutionHandler.lock();
		// this is the rotation manager to be used
		Class<? extends RotationManager> rmClass = SubstitutionHandler.getRotationManager();
		try {
			// instantiate it and set it as the main rotation manager 
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
					manager.rotationManager.start(rotation);
			}
		});
		// load all of the default minigames if enabled
		loadDefaultMinigames();
		getLogger().info(description.getName() + " v" + description.getVersion() + " by DonkeyCore has been enabled!");
	}
	
	/**
	 * <b>Bukkit implementation method</b><br>
	 * Do not call this
	 */
	@Override
	public void onDisable() {
		if (manager == null)
			throw new IllegalStateException("MinigameManager has not been enabled!");
		// Prepare everything for shutdown
		getLogger().info("Stopping rotations...");
		manager.rotationManager.shutdown();
		// Save everybody's profiles
		getLogger().info("Saving player profiles...");
		for(Player player : Bukkit.getOnlinePlayers())
			PlayerProfile.getPlayerProfile(player.getUniqueId()).saveProfile();
		getLogger().info(getDescription().getName() + " v" + getDescription().getVersion() + " by DonkeyCore has been disabled!");
	}
	
	/**
	 * Helpful method to determine whether the server is starting up or the
	 * plugins are simply being reloaded<br>
	 * Determined by the plugin being able to successfully inject the custom
	 * plugin manager
	 * 
	 * @return Whether the server is starting up
	 */
	public boolean isServerStartup() {
		return serverStartup;
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
					// all default minigames MUST be in the me.donkeycore.minigamemanager.minigames package
					clazz = Class.forName("me.donkeycore.minigamemanager.minigames." + minigameStr.replace("_", ""));
				} catch (ClassNotFoundException e) {
					getLogger().warning("Uh oh! " + minigameStr.replace("_", " ") + " is not a default minigame. Skipping!");
					continue;
				}
				// all minigames must be a subclass of Minigame
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
				MinigameAttributes attr = minigame.getAnnotation(MinigameAttributes.class);
				if (attr != null && attr.isDefault())
					manager.unregisterMinigame(minigame);
			}
		}
	}
	
}
