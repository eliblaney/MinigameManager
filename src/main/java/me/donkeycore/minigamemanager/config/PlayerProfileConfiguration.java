package me.donkeycore.minigamemanager.config;

import org.bukkit.configuration.ConfigurationSection;

import me.donkeycore.minigamemanager.api.config.CustomConfig;
import me.donkeycore.minigamemanager.api.player.PlayerProfile;
import me.donkeycore.minigamemanager.api.player.ProfileData;
import me.donkeycore.minigamemanager.core.MinigameManager;

/**
 * Class for profiles of players
 * 
 * @author DonkeyCore
 */
public class PlayerProfileConfiguration extends CustomConfig {
	
	/**
	 * Create a new instance of MinigameLocations
	 */
	public PlayerProfileConfiguration() {
		super(MinigameManager.getPlugin(), MinigameManager.getPlugin().getDataFolder(), "profiles.yml");
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
		if (cs == null) {
			data.setELO(settings.defaultELO());
			return data;
		}
		if (settings.eloEnabled())
			data.setELO(cs.getLong("elo"));
		if (!settings.vaultEnabled())
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
		if (cs == null) {
			getConfig().createSection(profile.getUUID().toString());
			cs = getConfig().getConfigurationSection(profile.getUUID().toString());
		}
		MinigameSettings settings = MinigameManager.getMinigameManager().getMinigameSettings();
		if (settings.eloEnabled())
			cs.set("elo", data.getELO());
		if (!settings.vaultEnabled())
			cs.set("currency", data.getCurrency());
		cs.set("gamesPlayed", data.getGamesPlayed());
		saveConfig();
	}
	
}
