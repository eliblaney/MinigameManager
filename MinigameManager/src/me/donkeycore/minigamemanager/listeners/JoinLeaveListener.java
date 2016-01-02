package me.donkeycore.minigamemanager.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import me.donkeycore.minigamemanager.api.RotationManager;
import me.donkeycore.minigamemanager.core.MinigameManager;
import me.donkeycore.minigamemanager.events.RotationJoinEvent;

public class JoinLeaveListener implements Listener {
	
	private final MinigameManager manager;
	
	public JoinLeaveListener(MinigameManager manager) {
		this.manager = manager;
	}
	
	@EventHandler
	public void onRotationJoin(RotationJoinEvent event) {
		RotationManager rm = manager.getRotationManager();
		rm.chooseMinigame(rm.getRotation(event.getPlayer()));
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		manager.getRotationManager().leave(event.getPlayer(), false);
	}
	
}
