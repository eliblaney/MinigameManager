package minigamemanager.api.profile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import minigamemanager.api.achivement.Achievement;
import minigamemanager.api.util.ELO;
import minigamemanager.api.util.ELO.GameResult;
import minigamemanager.config.MessageType;
import minigamemanager.config.MinigameSettings;
import minigamemanager.core.MinigameManager;

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
	 * Create a new PlayerProfile from the given UUID and ProfileData. WARNING:
	 * This overwrites any existing profile for the given UUID, so it is not
	 * recommended to use
	 * 
	 * @param uuid The player's UUID
	 * @param data The data to use for this profile
	 */
	private PlayerProfile(UUID uuid, ProfileData data) {
		this.uuid = uuid;
		this.data = data;
		for (PlayerProfile p : profiles) {
			if (p.getUUID().equals(uuid))
				profiles.remove(p);
		}
		profiles.add(this);
	}
	
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
		MinigameSettings s = MinigameManager.getMinigameManager().getMinigameSettings();
		if (s.mysqlEnabled()) {
			try {
				ProfileDatabase pdb = new ProfileDatabase(s.mysqlIP(), s.mysqlPort(), s.mysqlDatabase(), s.mysqlUsername(), s.mysqlPassword());
				PlayerProfile p = pdb.getProfile(uuid);
				if (p == null)
					p = new PlayerProfile(uuid);
				pdb.close();
				return p;
			} catch (SQLException | IOException e) {
				e.printStackTrace();
			}
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
	 * Give the player an achievement
	 * 
	 * @param achievement The achievement to give the player
	 * @return Whether the operation was successful or if the player already had
	 *         the achievement
	 */
	public boolean giveAchievement(Achievement achievement) {
		verifyRegistered(achievement);
		if(hasAchievement(achievement)) // make sure no duplicates
			return false;
		int hashCode = achievement.hashCode();
		int[] old = data.getAchievements();
		int[] achievements = new int[old.length + 1];
		for (int i = 0; i < old.length; i++) {
			if (hashCode == (achievements[i] = old[i])) { // copy and compare, make sure no weird duplicates
				MinigameManager.getPlugin().getLogger().warning("A duplicate achievement that MinigameManager wasn't aware of exists!");
				return false;
			}
		}
		achievements[old.length] = hashCode;
		data.setAchievements(achievements);
		Player player = Bukkit.getPlayer(uuid);
		Bukkit.getPlayer(uuid).sendMessage(MinigameManager.getMinigameManager().getMessages().getMessage(MessageType.UNLOCK_ACHIEVEMENT).replace("%name%", player.getName()).replace("%display%", player.getDisplayName()).replace("%achievement%", achievement.getName()));
		return true;
	}
	
	/**
	 * Get an array of achievements that the player currently has
	 * 
	 * @return An array of achievements
	 */
	public Achievement[] getAchievements() {
		int[] h = data.getAchievements();
		Achievement[] a = new Achievement[h.length];
		for (int i = 0; i < a.length; i++)
			a[i] = getAchievement(h[i]);
		return a;
	}
	
	/**
	 * Get an achievement instance from the hash code of the instance
	 * 
	 * @param hashCode The hash code of the desired achievement
	 * 
	 * @return A registered achievement with the given hash code, or null if not
	 *         found
	 */
	private Achievement getAchievement(int hashCode) {
		for (Achievement a : MinigameManager.getMinigameManager().getAchievements()) {
			if (a.hashCode() == hashCode)
				return a;
		}
		return null;
	}
	
	/**
	 * Verify that a given achievement is registered
	 * 
	 * @param achievement The achievement to test
	 */
	private void verifyRegistered(Achievement achievement) {
		for (Achievement a : MinigameManager.getMinigameManager().getAchievements()) {
			if (a.equals(achievement))
				return;
		}
		throw new IllegalArgumentException(String.format("The achievement \"%s\" is invalid, make sure it is registered!", achievement.getName()));
	}
	
	/**
	 * Determine whether the player has the specified achievement
	 * 
	 * @param achievement The achievement to test for
	 * 
	 * @return Whether the player has the achievement
	 */
	public boolean hasAchievement(Achievement achievement) {
		verifyRegistered(achievement);
		for (Achievement a : getAchievements())
			if (a.equals(achievement))
				return true;
		return false;
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
	 * Save the player's profile to the config or MySQL database
	 */
	public void saveProfile() {
		MinigameManager manager = MinigameManager.getMinigameManager();
		MinigameSettings s = manager.getMinigameSettings();
		if (s.mysqlEnabled()) {
			try {
				ProfileDatabase pdb = new ProfileDatabase(s.mysqlIP(), s.mysqlPort(), s.mysqlDatabase(), s.mysqlUsername(), s.mysqlPassword());
				pdb.saveProfile(this);
				pdb.close();
			} catch (SQLException | IOException e) {
				e.printStackTrace();
			}
		}
		manager.getPlayerProfileConfig().saveProfile(this);
	}
	
}
