package minigamemanager.events.rotation;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import minigamemanager.api.rotation.Rotation;

/**
 * Called when somebody joins a rotation
 * 
 * @author DonkeyCore
 */
public class RotationJoinEvent extends RotationEvent {
	
	private static final HandlerList handlers = new HandlerList();
	
	private final Player player;
	
	public RotationJoinEvent(Rotation rotation, Player player) {
		super(rotation);
		this.player = player;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
}
