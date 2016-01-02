package me.donkeycore.minigamemanager.api;

import org.bukkit.entity.Player;

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
	void chooseMinigame(Rotation rotation);
	
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
	 * Finish a minigame in a rotation and proceed to selecting the next one
	 * 
	 * @param id The id of the rotation to finish the minigame for
	 */
	void finish(int id);
	
	/**
	 * Stop all rotations, kick everyone from rotations, and stop next rotation
	 * cycles
	 */
	void shutdown();
	
}
