package minigamemanager.api.winner;

import java.util.UUID;

public interface WinnerList {
	
	/**
	 * Get the object that won first place
	 * 
	 * @return Who got first place
	 */
	public UUID[] getFirstPlace();
	
	/**
	 * Set the first place winner
	 * 
	 * @param firstPlace The new first place winner
	 */
	public void setFirstPlace(UUID[] firstPlace);
	
	/**
	 * The first place winner's name (useful for teams)
	 * 
	 * @param name The first place winner's name
	 */
	public void setFirstPlaceName(String name);
	
	/**
	 * Get the first place winner's name (useful for teams)
	 * 
	 * @return The first place winner's name
	 */
	public String getFirstPlaceName();
	
	/**
	 * Get the object that won second place
	 * 
	 * @return Who got first place
	 */
	public UUID[] getSecondPlace();
	
	/**
	 * Set the second place winner
	 * 
	 * @param secondPlace The new second place winner
	 */
	public void setSecondPlace(UUID[] secondPlace);
	
	/**
	 * Get the second place winner's name (useful for teams)
	 * 
	 * @return The second place winner's name
	 */
	public String getSecondPlaceName();
	
	/**
	 * The second place winner's name (useful for teams)
	 * 
	 * @param name The second place winner's name
	 */
	public void setSecondPlaceName(String name);
	
	/**
	 * Get the object that won third place
	 * 
	 * @return Who got first place
	 */
	public UUID[] getThirdPlace();
	
	/**
	 * Set the third place winner
	 * 
	 * @param thirdPlace The new third place winner
	 */
	public void setThirdPlace(UUID[] thirdPlace);
	
	/**
	 * Get the third place winner's name (useful for teams)
	 * 
	 * @return The third place winner's name
	 */
	public String getThirdPlaceName();
	
	/**
	 * The third place winner's name (useful for teams)
	 * 
	 * @param name The third place winner's name
	 */
	public void setThirdPlaceName(String name);
	
}
