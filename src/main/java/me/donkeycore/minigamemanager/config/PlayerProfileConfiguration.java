package me.donkeycore.minigamemanager.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.donkeycore.minigamemanager.api.player.PlayerProfile;
import me.donkeycore.minigamemanager.api.player.ProfileData;
import me.donkeycore.minigamemanager.core.MinigameManager;

/**
 * Class for profiles of players
 * 
 * @author DonkeyCore
 */
public class PlayerProfileConfiguration {
	
	/**
	 * The FileConfiguration instance that can be manipulated
	 */
	private FileConfiguration config;
	/**
	 * The file that the config is saved to
	 */
	private File configFile;
	
	/**
	 * Create a new instance of MinigameLocations
	 */
	public PlayerProfileConfiguration() {
		saveDefaultConfig();
		reloadConfig();
	}
	
	/**
	 * Reload the locations.yml configuration file
	 */
	public void reloadConfig() {
		if (configFile == null)
			configFile = new File(MinigameManager.getPlugin().getDataFolder(), "profiles.yml");
		config = YamlConfiguration.loadConfiguration(configFile);
		Reader defConfigStream = null;
		try {
			defConfigStream = new InputStreamReader(MinigameManager.getPlugin().getResource("profiles.yml"), "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			config.setDefaults(defConfig);
		}
	}
	
	/**
	 * Get the instance of FileConfiguration that handles the config
	 * 
	 * @return An instance of FileConfiguration representing the config
	 */
	public FileConfiguration getConfig() {
		if (config == null)
			reloadConfig();
		return config;
	}
	
	/**
	 * Save any changes to the config to disk
	 */
	public void saveConfig() {
		if (config == null || configFile == null)
			return;
		try {
			getConfig().save(configFile);
		} catch (IOException ex) {
			MinigameManager.getPlugin().getLogger().log(Level.SEVERE, "Could not save config to " + configFile, ex);
		}
	}
	
	/**
	 * Save the default config if the config file does not exist
	 */
	public void saveDefaultConfig() {
		if (configFile == null)
			configFile = new File(MinigameManager.getPlugin().getDataFolder(), "profiles.yml");
		if (!configFile.exists())
			MinigameManager.getPlugin().saveResource("profiles.yml", false);
	}
	
	/**
	 * Create data for a profile based on config
	 * 
	 * @param profile The PlayerProfile instance to retrieve data for
	 * 
	 * @return An instance of ProfileData
	 */
	public ProfileData getProfileData(PlayerProfile profile) {
		ProfileData data = new ProfileData();
		ConfigurationSection cs = getConfig().getConfigurationSection(profile.getUUID().toString());
		MinigameSettings settings = MinigameManager.getMinigameManager().getMinigameSettings();
		if(cs == null) {
			data.setELO(settings.defaultELO());
			return data;
		}
		if(settings.eloEnabled())
			data.setELO(cs.getLong("elo"));
		if(!settings.vaultEnabled())
			data.setCurrency(cs.getDouble("currency"));
		data.setGamesPlayed(cs.getLong("gamesPlayed"));
		return data;
	}
	
	/**
	 * Save a profile to the config
	 * 
	 * @param profile The profile to save
	 */
	public void saveProfile(PlayerProfile profile) {
		ProfileData data = profile.getData();
		ConfigurationSection cs = getConfig().getConfigurationSection(profile.getUUID().toString());
		if(cs == null) {
			getConfig().createSection(profile.getUUID().toString());
			cs = getConfig().getConfigurationSection(profile.getUUID().toString());
		}
		MinigameSettings settings = MinigameManager.getMinigameManager().getMinigameSettings();
		if(settings.eloEnabled())
			cs.set("elo", data.getELO());
		if(!settings.vaultEnabled())
			cs.set("currency", data.getCurrency());
		cs.set("gamesPlayed", data.getGamesPlayed());
		saveConfig();
	}
	
}
