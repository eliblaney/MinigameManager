package me.donkeycore.minigamemanager.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import me.donkeycore.minigamemanager.core.MinigameManager;

public class MinigameSettings {
	
	private FileConfiguration getConfig() {
		return MinigameManager.getPlugin().getConfig();
	}
	
	public void reloadConfig() {
		MinigameManager.getPlugin().reloadConfig();
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
	
	public boolean hidePlayersInLobby() {
		return getConfig().getConfigurationSection("rotations").getBoolean("lobby-hide-players");
	}
	
	// Configuration section: Minigames
	
	public boolean defaultsEnabled() {
		return getConfig().getConfigurationSection("minigames").getConfigurationSection("default-minigames").getBoolean("enabled");
	}
	
	public Set<String> getDefaultMinigames() {
		ConfigurationSection cs = getConfig().getConfigurationSection("minigames").getConfigurationSection("default-minigames").getConfigurationSection("defaults");
		return cs.getKeys(false);
	}
	
	public List<String> getEnabledDefaultMinigames() {
		if (!defaultsEnabled())
			return new ArrayList<>();
		ConfigurationSection cs = getConfig().getConfigurationSection("minigames").getConfigurationSection("default-minigames").getConfigurationSection("defaults");
		Set<String> minigames = cs.getKeys(false);
		List<String> enabled = new ArrayList<>();
		for (String m : minigames) {
			if (cs.getConfigurationSection(m).getBoolean("enabled"))
				enabled.add(m);
		}
		return enabled;
	}
	
	public int getMinimumForMinigame(String minigame) {
		return getConfig().getConfigurationSection("minigames").getConfigurationSection("default-minigames").getConfigurationSection("defaults").getConfigurationSection(minigame).getInt("minimum-players");
	}
	
}
