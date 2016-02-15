package me.donkeycore.minigamemanager.api.rotation;

import java.util.List;
import java.util.UUID;

import org.bukkit.Location;

import me.donkeycore.minigamemanager.api.minigame.Minigame;

/**
 * The main rotation class; represents a single rotation lobby
 * 
 * @author DonkeyCore
 */
public interface Rotation {
	
	/**
	 * Check if a player is currently in the rotation
	 * 
	 * @param uuid The UUID of the player to check
	 * @return Whether the player is in the rotation
	 */
	boolean hasPlayer(UUID uuid);
	
	/**
	 * Check if a player is currently in-game
	 * 
	 * @param uuid The UUID of the player to check
	 * @return Whether the player is in-game
	 */
	boolean isInGame(UUID uuid);
	
	/**
	 * Get all players in the rotation, both playing and not
	 * 
	 * @return A list of UUIDs of all players in the rotation
	 */
	List<UUID> getPlayers();
	
	/**
	 * Get all currently in-game players
	 * 
	 * @return A list of UUIDs of currently in-game players
	 */
	List<UUID> getInGame();
	
	/**
	 * Finish the current minigame and proceed the rotation to the lobby<br>
	 * <b>Note:</b> This method should not be called by minigames; they should
	 * use {@link Minigame#end} instead so that alternate implementations can be
	 * regarded
	 * 
	 * @param error The error code (0 = no error)
	 */
	void finish(int error);
	
	/**
	 * Announce a message to everybody in the rotation
	 * 
	 * @param message The message to announce
	 */
	void announce(String message);
	
	/**
	 * Teleport everybody in the rotation to a certain location
	 * 
	 * @param loc The location to teleport to
	 */
	void teleportAll(Location loc);
	
	/**
	 * Get the current state of this rotation
	 * 
	 * @return The current state
	 */
	RotationState getState();
	
	/**
	 * Get the current minigame
	 * 
	 * @return The current minigame instance, or null if there is no minigame
	 *         running yet
	 */
	Minigame getCurrentMinigame();
	
	/**
	 * Start playing again if the rotation has been stopped by an admin
	 */
	void resume();
	
	/**
	 * Stop the rotation, return players to the lobby, but do NOT start a new minigame
	 * 
	 * @param error The error code (0 = no error)
	 */
	void stop(int error);
	
	/**
	 * Get the id number that represents this rotation
	 * 
	 * @return The id of the rotation
	 */
	int getId();
	
}
