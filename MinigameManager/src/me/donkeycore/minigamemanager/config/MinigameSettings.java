package me.donkeycore.minigamemanager.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import me.donkeycore.minigamemanager.core.MinigameManager;

/**
 * MinigameManager settings
 * 
 * @author DonkeyCore
 */
public class MinigameSettings {
	
	/**
	 * Get the FileConfiguration representing the config.yml file
	 * 
	 * @return An instance of FileConfiguration
	 */
	public FileConfiguration getConfig() {
		return MinigameManager.getPlugin().getConfig();
	}
	
	/**
	 * Reload the config file
	 */
	public void reloadConfig() {
		MinigameManager.getPlugin().reloadConfig();
	}
	
	// Configuration section: Messages
	
	/**
	 * Get a message based on its type
	 * 
	 * @param type The type of message to retrieve
	 * 			
	 * @return The message
	 */
	public String getMessage(MessageType type) {
		return getConfig().getConfigurationSection("messages").getString(type.toString());
	}
	
	// Configuration section: Rotations
	
	/**
	 * Get the number of rotations that will be running
	 * 
	 * @return An integer number of rotations
	 */
	public int getNumberOfRotations() {
		return getConfig().getConfigurationSection("rotations").getInt("amount");
	}
	
	/**
	 * Get the minimum amount of players that must be in a rotation to start
	 * 
	 * @return The minimum number of players
	 */
	public int getMinimumPlayers() {
		return getConfig().getConfigurationSection("rotations").getInt("minimum-players");
	}
	
	/**
	 * Get the maximum amount of players that can be in a rotation at a time
	 * 
	 * @return The maximum number of players
	 */
	public int getMaximumPlayers() {
		return getConfig().getConfigurationSection("rotations").getInt("maximum-players");
	}
	
	/**
	 * Get the number of seconds to count down before starting a minigame
	 * 
	 * @return The number of seconds
	 */
	public int getCountdownSeconds() {
		return getConfig().getConfigurationSection("rotations").getInt("countdown-seconds");
	}
	
	/**
	 * Whether to hide players from other lobbies in the same lobby location
	 * from each other
	 * 
	 * @return Whether to hide players
	 */
	public boolean hidePlayersInLobby() {
		return getConfig().getConfigurationSection("rotations").getBoolean("lobby-hide-players");
	}
	
	/**
	 * Whether to have players join a rotation when the join the server and leave on quit
	 * 
	 * @return Whether to automatically assign players a rotation
	 */
	public boolean entireServer() {
		return getConfig().getConfigurationSection("rotations").getBoolean("entire-server");
	}
	
	// Configuration section: Minigames
	
	/**
	 * Whether default minigames are enabled
	 * 
	 * @return Whether default minigames are enabled
	 */
	public boolean defaultsEnabled() {
		return getConfig().getConfigurationSection("minigames").getConfigurationSection("default-minigames").getBoolean("enabled");
	}
	
	/**
	 * Get the default minigames
	 * 
	 * @return A set of names of default minigames
	 */
	public Set<String> getDefaultMinigames() {
		ConfigurationSection cs = getConfig().getConfigurationSection("minigames").getConfigurationSection("default-minigames").getConfigurationSection("defaults");
		return cs.getKeys(false);
	}
	
	/**
	 * Get the enabled default minigames
	 * 
	 * @return A list of names of default minigames that are enabled
	 */
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
	
	/**
	 * Get the minimum number of players for a minigame
	 * 
	 * @param minigame The minigame
	 * 			
	 * @return The minimum number of players
	 */
	public int getMinimumForMinigame(String minigame) {
		return getConfig().getConfigurationSection("minigames").getConfigurationSection("default-minigames").getConfigurationSection("defaults").getConfigurationSection(minigame).getInt("minimum-players");
	}
	
	// Configuration Section: Profiles
	
	/**
	 * Determine whether ELO ratings are enabled
	 * 
	 * @return Whether ELO ratings are enabled
	 */
	public boolean eloEnabled() {
		return getConfig().getConfigurationSection("profiles").getConfigurationSection("elo").getBoolean("enabled");
	}
	
	/**
	 * The the default ELO rating for new players
	 * 
	 * @return The default ELO rating
	 */
	public long defaultELO() {
		return getConfig().getConfigurationSection("profiles").getConfigurationSection("elo").getLong("default");
	}
	
	/**
	 * Get the K-Factor for ELO calculations
	 * 
	 * @return The K-Factor
	 */
	public int kFactor() {
		return getConfig().getConfigurationSection("profiles").getConfigurationSection("elo").getInt("K-factor");
	}
	
	/**
	 * Determine if vault support is enabled
	 * 
	 * @return Whether vault support is enabled AND enabled correctly
	 */
	public boolean vaultEnabled() {
		return getConfig().getConfigurationSection("profiles").getBoolean("vault") && MinigameManager.getMinigameManager().getVaultEconomy() != null;
	}
	
}
