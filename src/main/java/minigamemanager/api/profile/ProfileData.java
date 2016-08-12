package minigamemanager.api.profile;

import org.apache.commons.lang.Validate;

public class ProfileData {
	
	/**
	 * Player's ELO rating
	 */
	private long elo;
	/**
	 * Amount of money belonging to the player
	 */
	private double currency;
	/**
	 * The amount of games this player has played
	 */
	private long gamesPlayed;
	/**
	 * The player's list of achievements, stored as integers
	 */
	private int[] achievements = new int[0];
	
	/**
	 * Get the amount of games this player has played
	 * 
	 * @return The amount of games played
	 */
	public long getGamesPlayed() {
		return gamesPlayed;
	}
	
	/**
	 * Set the number of games this player has played
	 * 
	 * @param gamesPlayed The new number of games played
	 */
	public void setGamesPlayed(long gamesPlayed) {
		this.gamesPlayed = gamesPlayed;
	}
	
	/**
	 * Get the ELO rating for this player
	 * 
	 * @return The player's ELO rating
	 */
	public long getELO() {
		return elo;
	}
	
	/**
	 * Set the player's ELO rating
	 * 
	 * @param elo The player's new ELO rating
	 */
	public void setELO(long elo) {
		this.elo = elo;
	}
	
	/**
	 * Set the amount of money this player has to a specific price
	 * 
	 * @param money The amount of money for the player to have
	 */
	public void setCurrency(double money) {
		this.currency = money;
	}
	
	/**
	 * Get the amount of money this player has
	 * 
	 * @return The amount of money owned by the player
	 */
	public double getCurrency() {
		return currency;
	}
	
	/**
	 * Set the list of achievements the player currently has
	 * 
	 * @param achievements The list of achievement hash codes to replace the old
	 *            one
	 * 
	 * @return The old list of achievement hash codes
	 */
	public int[] setAchievements(int[] achievements) {
		int[] old = this.achievements;
		this.achievements = achievements;
		return old;
	}
	
	/**
	 * Get the list of achievements the player currently has
	 * 
	 * @return An array of achievement hash codes
	 */
	public int[] getAchievements() {
		return achievements;
	}
	
	/**
	 * Get the list of achievements the player currently has represented as a
	 * String
	 * 
	 * @return The achievements that the player has represented a string
	 */
	public String getAchievementString() {
		int[] hc = getAchievements();
		if(hc.length == 0)
			return "null";
		StringBuilder sb = new StringBuilder();
		for (int h : hc)
			sb.append("," + h);
		return sb.substring(1).toString();
	}
	
	/**
	 * Transform a string of achievement hash codes separated by commas into an
	 * array of ints
	 * 
	 * @param achievements The string representing the achievements
	 * 
	 * @return An int array of the achievement hash codes
	 */
	public static int[] getAchievementsFromString(String achievements) {
		Validate.notEmpty(achievements, "Achievements cannot be empty!");
		if(achievements.equals("null"))
			return new int[0];
		String[] hcStrs = achievements.split(",");
		int[] h = new int[hcStrs.length];
		for (int i = 0; i < h.length; i++)
			h[i] = Integer.parseInt(hcStrs[i]);
		return h;
	}
	
}
