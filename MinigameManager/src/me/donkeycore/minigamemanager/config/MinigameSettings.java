package me.donkeycore.minigamemanager.config;

import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;

import me.donkeycore.minigamemanager.core.MinigameManager;

public class MinigameSettings {
	
	private final MinigameManager manager;
	
	public MinigameSettings(MinigameManager manager) {
		this.manager = manager;
	}
	
	private FileConfiguration getConfig() {
		return this.manager.getConfig();
	}
	
	public void reload() {
		this.manager.reloadConfig();
	}
	
	// Configuration section: Messages
	
	public String getMessage(MessageType type) {
		return getConfig().getConfigurationSection("messages").getString(type.toString());
	}
	
	// Configuration section: Rotations
	
	public int getNumberOfRotations() {
		return getConfig().getConfigurationSection("rotations").getInt("amount");
	}
	
	public int getMinimumPlayers() {
		return getConfig().getConfigurationSection("rotations").getInt("minimum-players");
	}
	
	public int getMaximumPlayers() {
		return getConfig().getConfigurationSection("rotations").getInt("maximum-players");
	}
	
	public int getCountdownSeconds() {
		return getConfig().getConfigurationSection("rotations").getInt("countdown-seconds");
	}
	
	// Configuration section: Minigames
	
	public boolean defaultsEnabled() {
		return getConfig().getConfigurationSection("minigames").getConfigurationSection("default-minigames").getBoolean("enabled");
	}
	
	public List<String> getEnabledDefaultMinigames() {
		return getConfig().getConfigurationSection("minigames").getStringList("default-minigames");
	}
	
}
