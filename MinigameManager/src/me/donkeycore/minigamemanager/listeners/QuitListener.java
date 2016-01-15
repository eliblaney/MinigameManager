package me.donkeycore.minigamemanager.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import me.donkeycore.minigamemanager.core.MinigameManager;

public class QuitListener implements Listener {
	
	private final MinigameManager manager;
	
	public QuitListener(MinigameManager manager) {
		this.manager = manager;
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		manager.getRotationManager().leave(event.getPlayer(), false);
	}
	
}
