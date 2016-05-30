package minigamemanager.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import minigamemanager.core.MinigameManager;

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
	 *//*
		 * public String getMessage(MessageType type) {
		 * return
		 * getConfig().getConfigurationSection("messages").getString(type.
		 * toString());
		 * }
		 */
	
	/**
	 * Get the language to be used for messages
	 * 
	 * @return The language
	 */
	public String getLanguage() {
		return getConfig().getString("language");
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
	 * Whether to have players join a rotation when the join the server and
	 * leave on quit
	 * 
	 * @return Whether to automatically assign players a rotation
	 */
	public boolean entireServer() {
		return getConfig().getConfigurationSection("rotations").getBoolean("entire-server");
	}
	
	// Configuration section: Lobby
	
	/**
	 * Whether players can take damage in the lobby
	 * 
	 * @return Whether players can take damage in the lobby
	 */
	public boolean lobbyDamage() {
		return getConfig().getConfigurationSection("lobby").getBoolean("damage");
	}
	
	/**
	 * Whether players can lose hunger in the lobby
	 * 
	 * @return Whether players can lose hunger in the lobby
	 */
	public boolean lobbyHunger() {
		return getConfig().getConfigurationSection("lobby").getBoolean("hunger");
	}
	
	/**
	 * Whether to show a scoreboard to players in the lobby, as specified in the
	 * language configuration file
	 * 
	 * @return Whether to show a scoreboard to players in the lobby
	 */
	public boolean lobbyScoreboard() {
		return getConfig().getConfigurationSection("lobby").getBoolean("scoreboard");
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
	
	// Configuration Section: Currency
	
	/**
	 * Determine whether to use a prefix for currency or suffix
	 *
	 * @return Whether to use prefixes or not
	 */
	public boolean useCurrencyPrefix() {
		return getConfig().getConfigurationSection("currency").getBoolean("use-prefix");
	}
	
	/**
	 * Get the prefix to use for currency
	 * 
	 * @return The currency prefix
	 */
	public String getCurrencyPrefix() {
		return getConfig().getConfigurationSection("currency").getString("prefix");
	}
	
	/**
	 * Get the suffix to use for currency
	 * 
	 * @return The currency suffix
	 */
	public String getCurrencySuffix() {
		return getConfig().getConfigurationSection("currency").getString("suffix");
	}
	
	/**
	 * Get the name of the currency
	 * 
	 * @return The name of the currency
	 */
	public String getCurrencyName() {
		return getConfig().getConfigurationSection("currency").getString("name");
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
	 * Determine if vault support is enabled
	 * 
	 * @return Whether vault support is enabled AND enabled correctly
	 */
	public boolean vaultEnabled() {
		return getConfig().getConfigurationSection("profiles").getBoolean("vault") && MinigameManager.getMinigameManager().useVaultEconomy();
	}
	
	// Configuration Section: MySQL
	
	/**
	 * Get whether MySQL is enabled for this server
	 * 
	 * @return Whether MySQL is enabled
	 */
	public boolean mysqlEnabled() {
		return getConfig().getConfigurationSection("mysql").getBoolean("enabled");
	}
	
	/**
	 * Get the IP to be used for connecting to the MySQL database
	 * 
	 * @return The database IP
	 */
	public String mysqlIP() {
		return getConfig().getConfigurationSection("mysql").getString("ip");
	}
	
	/**
	 * Get the port to be used for connecting to the MySQL database
	 * 
	 * @return The database port
	 */
	public int mysqlPort() {
		return getConfig().getConfigurationSection("mysql").getInt("port");
	}
	
	/**
	 * Get the database to connect to
	 * 
	 * @return The database name
	 */
	public String mysqlDatabase() {
		return getConfig().getConfigurationSection("mysql").getString("database");
	}
	
	/**
	 * Get the username to use when connecting to the database
	 * 
	 * @return The database username
	 */
	public String mysqlUsername() {
		return getConfig().getConfigurationSection("mysql").getString("username");
	}
	
	/**
	 * Get the password to use when connecting to the database
	 * 
	 * @return The database password
	 */
	public String mysqlPassword() {
		String pass = getConfig().getConfigurationSection("mysql").getString("password");
		if (pass == null || pass.trim().isEmpty())
			return null;
		return pass;
	}
	
	/**
	 * Get a map of strings to objects of table names
	 * 
	 * @return A map of table names
	 */
	public Map<String, Object> mysqlTables() {
		return getConfig().getConfigurationSection("mysql").getConfigurationSection("tables").getValues(false);
	}
	
}
