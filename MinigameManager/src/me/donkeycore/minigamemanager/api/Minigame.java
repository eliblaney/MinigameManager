package me.donkeycore.minigamemanager.api;

import org.bukkit.Location;

import me.donkeycore.minigamemanager.core.MinigameManager;

/**
 * The main minigame API class; all minigames should extend this class
 * 
 * @author DonkeyCore
 */
public abstract class Minigame {
	
	private final Rotation r;
	
	/**
	 * Initialize the minigame
	 * 
	 * @param r The rotation that the minigame is in
	 */
	public Minigame(Rotation r) {
		this.r = r;
	}
	
	/**
	 * Get the current instance of MinigameManager.<br>
	 * <b>Warning:</b> The MinigameManager must be enabled for this to return
	 * correctly
	 * 
	 * @return The current MinigameManager instance, or null if not enabled
	 */
	public static final MinigameManager getMinigameManager() {
		return MinigameManager.getMinigameManager();
	}
	
	/**
	 * Get the rotation that this minigame is associated with
	 * 
	 * @return The rotation that the minigame is associated with
	 */
	public final Rotation getRotation() {
		return r;
	}
	
	/**
	 * Called when the minigame has been registered
	 */
	public void onLoad() {}
	
	/**
	 * Called when the minigame is being unregistered
	 */
	public void onUnload() {}
	
	/**
	 * Called when the minigame starts and all players have been teleported to
	 * their starting locations
	 */
	public abstract void onStart();
	
	/**
	 * Called when the minigame ends
	 */
	public void onEnd() {}
	
	/**
	 * Call this to end the minigame and continue to the next rotation<br>
	 * <b>Note:</b> If this method is being overriden, make sure there is a call
	 * to {@link Rotation#finish()}
	 */
	public void end() {
		r.finish();
	}
	
	/**
	 * Get the attributes belonging to this minigame
	 * 
	 * @return An instance of {@link MinigameAttributes}, or null if the annotation is not present (bad!)
	 */
	public final MinigameAttributes getAttributes() {
		return getClass().getAnnotation(MinigameAttributes.class);
	}
	
	/**
	 * Get the minigame name as it will be displayed in chat
	 * 
	 * @return The minigame's friendly name, or "Unnamed" if not specified
	 */
	public final String getName() {
		MinigameAttributes attributes = getAttributes();
		if(attributes == null)
			return "Unnamed";
		return attributes.name();
	}
	
	/**
	 * Get the spawn location for the beginning of the minigame
	 * 
	 * @return The spawn location, can be random
	 */
	public abstract Location getStartingLocation();
	
}
