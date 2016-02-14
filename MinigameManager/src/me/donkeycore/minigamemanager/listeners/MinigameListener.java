package me.donkeycore.minigamemanager.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import me.donkeycore.minigamemanager.api.minigame.MinigameAttributes;
import me.donkeycore.minigamemanager.api.rotation.Rotation;
import me.donkeycore.minigamemanager.api.rotation.RotationState;
import me.donkeycore.minigamemanager.core.MinigameManager;

/**
 * Listens for food level changes or health changes in minigames or lobbies and
 * cancels them
 * 
 * @author DonkeyCore
 */
public class MinigameListener implements Listener {
	
	private final MinigameManager manager;
	
	public MinigameListener(MinigameManager manager) {
		this.manager = manager;
	}
	
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		HumanEntity entity = event.getEntity();
		if (entity instanceof Player) {
			Player player = (Player) entity;
			Rotation rotation = manager.getRotationManager().getRotation(player);
			if (rotation != null && rotation.getState() == RotationState.INGAME) {
				MinigameAttributes attr = rotation.getCurrentMinigame().getAttributes();
				if (attr.alwaysSaturated())
					event.setCancelled(true);
			} else if (rotation != null && rotation.getState() != RotationState.INGAME)
				event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onHealthChange(EntityDamageEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof Player) {
			Player player = (Player) entity;
			Rotation rotation = manager.getRotationManager().getRotation(player);
			if (rotation != null && rotation.getState() == RotationState.INGAME) {
				MinigameAttributes attr = rotation.getCurrentMinigame().getAttributes();
				if (attr.alwaysFullHealth())
					event.setCancelled(true);
			} else if (rotation != null && rotation.getState() != RotationState.INGAME)
				event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onItemDrop(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		Rotation rotation = manager.getRotationManager().getRotation(player);
		if (rotation != null && rotation.getState() == RotationState.INGAME) {
			MinigameAttributes attr = rotation.getCurrentMinigame().getAttributes();
			if (!attr.canDropItems())
				event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onItemPickup(PlayerPickupItemEvent event) {
		Player player = event.getPlayer();
		Rotation rotation = manager.getRotationManager().getRotation(player);
		if (rotation != null && rotation.getState() == RotationState.INGAME) {
			MinigameAttributes attr = rotation.getCurrentMinigame().getAttributes();
			if (!attr.canPickUpItems())
				event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		Rotation rotation = manager.getRotationManager().getRotation(player);
		if (rotation != null && rotation.getState() == RotationState.INGAME)
			event.setRespawnLocation(rotation.getCurrentMinigame().getStartingLocation(player));
	}
	
}
