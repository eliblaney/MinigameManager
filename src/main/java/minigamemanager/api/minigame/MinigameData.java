package minigamemanager.api.minigame;

import minigamemanager.api.config.MinigameConfig;

/**
 * Holds information about a minigame, including its minimum player count,
 * config class, and location class
 *
 * @author DonkeyCore
 */
public class MinigameData {
	
	/**
	 * Minimum players needed for the minigame to start
	 */
	private final int minimumPlayers;
	
	/**
	 * The MinigameConfig class associated with this minigame
	 */
	private final MinigameConfig config;
	
	/**
	 * Initialize this MinigameData with the minimum player count, config class,
	 * and location class
	 * 
	 * @param minimumPlayers Minimum players needed for the minigame to start
	 * @param config The MinigameConfig class associated with this minigame
	 */
	public MinigameData(int minimumPlayers, MinigameConfig config) {
		this.minimumPlayers = minimumPlayers;
		this.config = config;
	}
	
	/**
	 * Get the minimum player count needed for the minigame to start
	 * 
	 * @return The minimum player count
	 */
	public int getMinimumPlayers() {
		return minimumPlayers;
	}
	
	/**
	 * Get the MinigameConfig class associated with this minigame
	 * 
	 * @return The config class
	 */
	public MinigameConfig getConfig() {
		return config;
	}
	
}
