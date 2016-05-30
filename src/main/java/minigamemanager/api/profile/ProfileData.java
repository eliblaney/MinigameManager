package minigamemanager.api.profile;

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
	
}
