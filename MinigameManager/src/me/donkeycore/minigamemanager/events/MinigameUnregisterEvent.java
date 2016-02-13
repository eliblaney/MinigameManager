package me.donkeycore.minigamemanager.events;

import org.bukkit.event.HandlerList;

import me.donkeycore.minigamemanager.api.minigame.Minigame;

/**
 * Called when a minigame is unregistered
 * 
 * @author DonkeyCore
 */
public class MinigameUnregisterEvent extends MinigameEvent {
	
	private static final HandlerList handlers = new HandlerList();
	private final Class<? extends Minigame> minigame;
	
	public MinigameUnregisterEvent(Class<? extends Minigame> minigame) {
		this.minigame = minigame;
	}
	
	public Class<? extends Minigame> getMinigame() {
		return minigame;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}

}
