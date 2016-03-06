package me.donkeycore.minigamemanager.api.player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import me.donkeycore.minigamemanager.api.util.ELO;
import me.donkeycore.minigamemanager.api.util.ELO.GameResult;
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
	private PlayerProfile(UUID uuid) {
		this.uuid = uuid;
		this.data = MinigameManager.getMinigameManager().getPlayerProfileConfig().getProfileData(this);
		profiles.add(this);
	}
	
	/**
	 * Get a player's corresponding PlayerProfile
	 * 
	 * @param uuid The player's UUID
	 * 
	 * @return A PlayerProfile instance corresponding with the UUID
	 */
	public static PlayerProfile getPlayerProfile(UUID uuid) {
		for (PlayerProfile profile : profiles) {
			if (profile.getUUID().equals(uuid))
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
		return updateELO(otherELO, GameResult.WON);
	}
	
	/**
	 * Update the player's ELO if they have lost against an opponent
	 * 
	 * @param otherELO The other player's ELO rating
	 * 
	 * @return Whether the ELO of the player successfully updated
	 */
	public boolean loseELO(long otherELO) {
		return updateELO(otherELO, GameResult.LOST);
	}
	
	/**
	 * Update the player's ELO if they have drawn against an opponent
	 * 
	 * @param otherELO The other player's ELO rating
	 * 
	 * @return Whether the ELO of the player successfully updated
	 */
	public boolean drawELO(long otherELO) {
		return updateELO(otherELO, GameResult.DRAW);
	}
	
	/**
	 * Update the player's ELO based on their opponent and the game's outcome
	 * 
	 * @param otherELO The other player's ELO rating
	 * @param score Whether the player won, lost, or drew
	 * 
	 * @return Whether the ELO of the player successfully updated
	 */
	private boolean updateELO(long otherELO, GameResult score) {
		if (!MinigameManager.getMinigameManager().getMinigameSettings().eloEnabled())
			return false;
		data.setELO(ELO.getUpdatedELO(data.getELO(), otherELO, data.getGamesPlayed(), score));
		return true;
	}
	
	/**
	 * Mark that a player has played a game. Increments their total games played
	 * by 1.
	 */
	public void playedGame() {
		data.setGamesPlayed(data.getGamesPlayed() + 1);
	}
	
	/**
	 * Determine whether the player is a pro
	 * 
	 * @return true if their ELO rating is above 2400, false otherwise
	 */
	public boolean isPro() {
		return ELO.isPro(data.getELO());
	}
	
	/**
	 * Give the player money
	 * 
	 * @param money The amount of money to give
	 */
	public void deposit(double money) {
		MinigameManager manager = MinigameManager.getMinigameManager();
		if (manager.useVaultEconomy())
			manager.getVaultEconomy().depositPlayer(getPlayer(), money);
		else
			data.setCurrency(data.getCurrency() + money);
	}
	
	/**
	 * Take money away from the player
	 * 
	 * @param money How much money to take
	 * 
	 * @return Whether the money was successfully taken or the player did not
	 *         have enough
	 */
	public boolean withdraw(double money) {
		if (!canAfford(money))
			return false;
		MinigameManager manager = MinigameManager.getMinigameManager();
		if (manager.useVaultEconomy())
			manager.getVaultEconomy().withdrawPlayer(getPlayer(), money);
		else
			data.setCurrency(data.getCurrency() - money);
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
		MinigameManager manager = MinigameManager.getMinigameManager();
		if (manager.useVaultEconomy())
			return manager.getVaultEconomy().has(getPlayer(), money);
		else
			return data.getCurrency() - money >= 0;
	}
	
	/**
	 * Get the amount of money a player has
	 * 
	 * @return The player's balance
	 */
	public double getCurrency() {
		MinigameManager manager = MinigameManager.getMinigameManager();
		if (manager.useVaultEconomy())
			return manager.getVaultEconomy().getBalance(getPlayer());
		else
			return data.getCurrency();
	}
	
	/**
	 * Save the player's profile to the config
	 */
	public void saveProfile() {
		MinigameManager.getMinigameManager().getPlayerProfileConfig().saveProfile(this);
	}
	
}
