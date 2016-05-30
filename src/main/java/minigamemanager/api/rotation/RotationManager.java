package minigamemanager.api.rotation;

import org.bukkit.entity.Player;

import minigamemanager.api.minigame.Minigame;

/**
 * The main rotation manager; controls all rotations
 * 
 * @author DonkeyCore
 */
public interface RotationManager {
	
	/**
	 * Have a player join any available rotation
	 * 
	 * @param player The player to join
	 * @return Whether there were any available rotations to put the player in
	 */
	boolean join(Player player);
	
	/**
	 * Have a player join a specific rotation
	 * 
	 * @param player The player to join
	 * @param id The ID of the rotation to be joined
	 * @return Whether the rotation was available to put the player in
	 */
	boolean join(Player player, int id);
	
	/**
	 * Have a player leave the rotation they are in
	 * 
	 * @param player The player to leave
	 * @param kicked Whether the player was kicked
	 * @return Whether the player was found in a rotation and removed
	 */
	boolean leave(Player player, boolean kicked);
	
	/**
	 * Called to choose a minigame and begin the countdown if allowed<br>
	 * 
	 * @param rotation The rotation to execute the process for
	 */
	void start(Rotation rotation);
	
	/**
	 * Get all of the currently active rotations
	 * 
	 * @return An array of {@link Rotation} instances
	 */
	Rotation[] getRotations();
	
	/**
	 * Get a rotation by its ID
	 * 
	 * @param id The ID of the rotation
	 * @return An instance of {@link Rotation}
	 */
	Rotation getRotation(int id);
	
	/**
	 * Get a rotation based on a player who is currently in it
	 * 
	 * @param player The player inside the rotation
	 * @return An instance of {@link Rotation} or null if the player is not in a
	 *         rotation
	 */
	Rotation getRotation(Player player);
	
	/**
	 * Stop all rotations, kick everyone from rotations, and stop next rotation
	 * cycles
	 */
	void shutdown();
	
	/**
	 * Force the countdown to start, no matter how many players there are
	 * 
	 * @param r The rotation to force
	 */
	void force(Rotation r);
	
	/**
	 * Set the next minigame to be played
	 * 
	 * @param id The ID of the rotation to change the next minigame for
	 * @param mclazz The class of the next minigame
	 */
	void setNext(int id, Class<? extends Minigame> mclazz);
	
}
