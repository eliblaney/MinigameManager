package me.donkeycore.minigamemanager.api.util;

import java.util.UUID;

import org.bukkit.entity.Player;

import me.donkeycore.minigamemanager.api.player.PlayerProfile;

public final class ELO {
	
	/**
	 * ELO is a static class, so private constructor
	 */
	private ELO() {}
	
	/**
	 * Get an updated version of the player's ELO based on their opponent and
	 * the game's outcome
	 * 
	 * @param playerELO The player's current ELO to be changed
	 * @param otherELO The other player's ELO rating
	 * @param gamesPlayed The amount of games this player has played
	 * @param playerScore Whether the player won, lost, or drew
	 * 
	 * @return The player's new ELO
	 */
	public static long getUpdatedELO(long playerELO, long otherELO, long gamesPlayed, GameResult score) {
		double playerRating = Math.pow(10, playerELO / 400);
		double otherRating = Math.pow(10, otherELO / 400);
		double playerExpected = playerRating / (playerRating + otherRating);
		return (long) (playerELO + getKFactor(playerELO, gamesPlayed) * (score.getValue() - playerExpected));
	}
	
	/**
	 * Get the K-Factor for a player with a certain ELO according to the FIDE
	 * standards
	 * 
	 * @param playerELO The player's ELO
	 * @param gamesPlayed The amount of games this player has played
	 * 
	 * @return The K-Factor
	 */
	public static int getKFactor(long playerELO, long gamesPlayed) {
		// Beginner
		if (gamesPlayed < 30)
			return 25;
		// Pro (permanently pro)
		if (isPro(playerELO) && gamesPlayed >= 30)
			return 10;
		// Normal
		return 15;
	}
	
	/**
	 * Determine whether a player is a pro
	 * 
	 * @param playerELO The player's ELO rating
	 * 
	 * @return true if the ELO rating is above 2400, false otherwise
	 */
	public static boolean isPro(long playerELO) {
		return playerELO > 2400;
	}
	
	/**
	 * Get the average ELO rating for a list of players
	 * 
	 * @param players The list of players
	 * 
	 * @return The average ELO rating
	 */
	public static long averageELO(Player... players) {
		long average = 0;
		for (Player player : players)
			average += PlayerProfile.getPlayerProfile(player.getUniqueId()).getData().getELO();
		return average / players.length;
	}
	
	/**
	 * Get the average ELO rating for a list of player UUIDs
	 * 
	 * @param players The list of player UUIDs
	 * 
	 * @return The average ELO rating
	 */
	public static long averageELO(UUID... players) {
		long average = 0;
		for (UUID player : players)
			average += PlayerProfile.getPlayerProfile(player).getData().getELO();
		return average / players.length;
	}
	
	/**
	 * Get the average ELO rating for a list of PlayerProfiles
	 * 
	 * @param players The list of PlayerProfiles
	 * 
	 * @return The average ELO rating
	 */
	public static long averageELO(PlayerProfile... players) {
		long average = 0;
		for (PlayerProfile player : players)
			average += player.getData().getELO();
		return average / players.length;
	}
	
	public static enum GameResult {
		
		WON, LOST, DRAW;
		
		/**
		 * Get the numerical value of the score
		 * 
		 * @return 1 for WON, 0 for LOST, or 0.5 for DRAW
		 */
		public double getValue() {
			switch (this) {
				case WON:
					return 1;
				case LOST:
					return 0;
				case DRAW:
					return 0.5;
			}
			return -1;
		}
		
		@Override
		public String toString() {
			return Double.toString(getValue());
		}
		
	}
	
}
