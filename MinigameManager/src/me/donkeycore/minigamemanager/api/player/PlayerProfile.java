package me.donkeycore.minigamemanager.api.player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import me.donkeycore.minigamemanager.core.MinigameManager;

public class PlayerProfile {
	
	/**
	 * The player's UUID
	 */
	private final UUID uuid;
	/**
	 * The class that handles manipulation of data for this profile
	 */
	private final ProfileData data;
	/**
	 * All active player profiles - only 1 per player
	 */
	private static final List<PlayerProfile> profiles = new ArrayList<>();

	/**
	 * Create a new PlayerProfile
	 * 
	 * @param uuid The player's UUID
	 */
	private PlayerProfile(UUID uuid){
		this.uuid = uuid;
		this.data = MinigameManager.getMinigameManager().getPlayerProfileConfig().getProfileData(this);
		profiles.add(this);
	}
	
	/**
	 * Get a player's corresponding PlayerProfile
	 * 
	 * @param uuid The player's UUID
	 */
	public static PlayerProfile getPlayerProfile(UUID uuid) {
		for(PlayerProfile profile : profiles) {
			if(profile.getUUID().equals(uuid))
				return profile;
		}
		return new PlayerProfile(uuid);
	}
	
	/**
	 * Get the UUID of the player representing this profile
	 * 
	 * @return A UUID of the player representing this profile
	 */
	public UUID getUUID() {
		return uuid;
	}
	
	/**
	 * Get the player object representing this profile
	 * 
	 * @return An OfflinePlayer representing this profile
	 */
	public OfflinePlayer getPlayer() {
		return Bukkit.getOfflinePlayer(uuid);
	}
	
	/**
	 * Get the class that handles the manipulation of data for this player
	 * 
	 * @return The player's PlayerData instance
	 */
	public ProfileData getData() {
		return data;
	}
	
	/**
	 * Update the player's ELO if they have won against an opponent
	 * 
	 * @param otherELO The other player's ELO rating
	 * 
	 * @return Whether the ELO of the player successfully updated
	 */
	public boolean winELO(long otherELO) {
		return updateELO(otherELO, 1);
	}
	
	/**
	 * Update the player's ELO if they have lost against an opponent
	 * 
	 * @param otherELO The other player's ELO rating
	 * 
	 * @return Whether the ELO of the player successfully updated
	 */
	public boolean loseELO(long otherELO) {
		return updateELO(otherELO, 0);
	}
	
	/**
	 * Update the player's ELO if they have drawn against an opponent
	 * 
	 * @param otherELO The other player's ELO rating
	 * 
	 * @return Whether the ELO of the player successfully updated
	 */
	public boolean drawELO(long otherELO) {
		return updateELO(otherELO, 0.5);
	}
	
	/**
	 * Update the player's ELO based on their opponent and the game's outcome
	 * 
	 * @param otherELO The other player's ELO rating
	 * @param playerScore Whether the player won, lost, or tied. Can only be 1, 0, or 0.5 respectively.
	 * 
	 * @return Whether the ELO of the player successfully updated
	 */
	private boolean updateELO(long otherELO, double playerScore) {
		if(playerScore != 1 && playerScore != 0 && playerScore != 0.5)
			throw new IllegalArgumentException("playerScore can only be 1, 0, or 0.5 but " + playerScore + " was given");
		if(!MinigameManager.getMinigameManager().getMinigameSettings().eloEnabled())
			return false;
		double playerRating = Math.pow(10, data.getELO() / 400);
		double otherRating = Math.pow(10, otherELO / 400);
		double playerExpected = playerRating / (playerRating + otherRating);
		data.setELO((long) (playerRating + MinigameManager.getMinigameManager().getMinigameSettings().kFactor() * (playerScore - playerExpected)));
		return true;
	}
	
	/**
	 * Give the player money
	 * 
	 * @param money The amount of money to give
	 */
	public void deposit(double money) {
		data.setCurrency(data.getCurrency() + money);
	}
	
	/**
	 * Take money away from the player
	 * 
	 * @param money How much money to take
	 * 
	 * @return Whether the money was successfully taken or the player did not have enough
	 */
	public boolean withdraw(double money) {
		double currency = data.getCurrency();
		if(!canAfford(money))
			return false;
		data.setCurrency(currency - money);
		return true;
	}
	
	/**
	 * Determine whether the player can afford the specified amount of money
	 * 
	 * @param money The price of something
	 * 
	 * @return Whether the player can afford it
	 */
	public boolean canAfford(double money) {
		return data.getCurrency() - money >= 0;
	}
	
	/**
	 * Save the player's profile to the config
	 */
	public void saveProfile() {
		MinigameManager.getMinigameManager().getPlayerProfileConfig().saveProfile(this);
	}
	
}
