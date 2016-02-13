package me.donkeycore.minigamemanager.events;

import org.bukkit.event.HandlerList;

import me.donkeycore.minigamemanager.api.minigame.Minigame;

/**
 * Called when a minigame is registered
 * 
 * @author DonkeyCore
 */
public class MinigameRegisterEvent extends MinigameEvent {
	
	private static final HandlerList handlers = new HandlerList();
	private final Class<? extends Minigame> minigame;
	private final int minimumPlayers;
	
	public MinigameRegisterEvent(Class<? extends Minigame> minigame, int minimumPlayers) {
		this.minigame = minigame;
		this.minimumPlayers = minimumPlayers;
	}
	
	public Class<? extends Minigame> getMinigame() {
		return minigame;
	}
	
	public int getMinimumPlayers() {
		return minimumPlayers;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
}
