package me.donkeycore.minigamemanager.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import me.donkeycore.minigamemanager.api.minigame.MinigameAttributes;
import me.donkeycore.minigamemanager.api.rotation.Rotation;
import me.donkeycore.minigamemanager.api.rotation.RotationState;
import me.donkeycore.minigamemanager.core.MinigameManager;

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
				if (attr.alwaysFullHunger())
					event.setCancelled(true);
			} else if(rotation != null && rotation.getState() == RotationState.LOBBY)
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
			} else if(rotation != null && rotation.getState() == RotationState.LOBBY)
				event.setCancelled(true);
		}
	}
	
}
