package me.donkeycore.minigamemanager.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.donkeycore.minigamemanager.core.MinigameManager;

/**
 * Handles players joining/quitting so that rotations are safe 
 * 
 * @author DonkeyCore
 */
public class JoinQuitListener implements Listener {
	
	private final MinigameManager manager;
	
	public JoinQuitListener(MinigameManager manager) {
		this.manager = manager;
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		if(manager.getMinigameConfig().entireServer())
			manager.getRotationManager().join(event.getPlayer());
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		manager.getRotationManager().leave(event.getPlayer(), false);
	}
	
}
