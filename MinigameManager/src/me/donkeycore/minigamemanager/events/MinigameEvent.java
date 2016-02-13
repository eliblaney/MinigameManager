package me.donkeycore.minigamemanager.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * An event for minigames
 * 
 * @author DonkeyCore
 */
public abstract class MinigameEvent extends Event {
	
	protected static final HandlerList handlers = new HandlerList();
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
}
