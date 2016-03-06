package me.donkeycore.minigamemanager.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import me.donkeycore.minigamemanager.api.minigame.Minigame;
import me.donkeycore.minigamemanager.api.minigame.MinigameAttributes;
import me.donkeycore.minigamemanager.api.rotation.Rotation;
import me.donkeycore.minigamemanager.api.rotation.RotationManager;
import me.donkeycore.minigamemanager.api.rotation.RotationState;
import me.donkeycore.minigamemanager.core.MinigameManager;
import me.donkeycore.minigamemanager.events.sign.SignClickEvent;

/**
 * Listens for health/food level changes, item drops/pickups, respawns, and sign
 * clicks
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
			} else if (!manager.getMinigameSettings().lobbyHunger() && rotation != null && rotation.getState() != RotationState.INGAME)
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
			} else if (!manager.getMinigameSettings().lobbyDamage() && rotation != null && rotation.getState() != RotationState.INGAME)
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
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Action action = event.getAction();
		if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) {
			Block block = event.getClickedBlock();
			BlockState state = block.getState();
			if (block != null && state instanceof Sign) {
				Sign sign = (Sign) state;
				Bukkit.getPluginManager().callEvent(new SignClickEvent(event.getPlayer(), action, sign));
			}
		}
	}
	
	@EventHandler
	public void onSignClick(SignClickEvent event) {
		String[] lines = event.getLines();
		Player player = event.getPlayer();
		RotationManager rm = manager.getRotationManager();
		Rotation r = rm.getRotation(player);
		for (String s : lines) {
			if(s.toLowerCase().contains("status")) {
				for (String st : lines) {
					try {
						int i = Integer.parseInt(ChatColor.stripColor(st.toLowerCase()));
						Rotation rot = rm.getRotation(i - 1);
						player.sendMessage(ChatColor.YELLOW + "===<" + ChatColor.GOLD + "Rotation Information" + ChatColor.YELLOW + ">===");
						player.sendMessage(ChatColor.YELLOW + "> State: " + rot.getState().toColoredString());
						if (rot.getState() == RotationState.INGAME) {
							player.sendMessage(ChatColor.YELLOW + "> Minigame: " + ChatColor.GOLD + rot.getCurrentMinigame().getName().replace("_", ""));
							player.sendMessage(ChatColor.YELLOW + "> Ingame: " + ChatColor.GOLD + rot.getInGame().size());
						}
						player.sendMessage(ChatColor.YELLOW + "> Total Players: " + ChatColor.GOLD + rot.getPlayers().size());
						player.sendMessage(ChatColor.YELLOW + "===<" + ChatColor.GOLD + "Rotation Information" + ChatColor.YELLOW + ">===");
					} catch (NumberFormatException e) {
						continue;
					}
				}
				return;
			}
			if(s.toLowerCase().contains("info")) {
				for (String st : lines) {
					for (Class<? extends Minigame> mclazz : manager.getMinigames()) {
						MinigameAttributes attr = mclazz.getAnnotation(MinigameAttributes.class);
						if (attr == null)
							continue;
						if (ChatColor.stripColor(st).replace(" ", "").replace("_", "").replace("-", "").toLowerCase().contains(attr.name().toLowerCase().replace(" ", "").replace("_", "").replace("-", ""))) {
							player.sendMessage(ChatColor.YELLOW + "===<" + ChatColor.GOLD + "Minigame Information" + ChatColor.YELLOW + ">===");
							player.sendMessage(ChatColor.YELLOW + "> Name: " + ChatColor.GOLD + attr.name().replace("_", " "));
							String[] _authors = attr.authors();
							String authors = "";
							if (_authors.length > 0) {
								if (_authors.length == 1)
									// stripping the first 2 chars later, so the 42 is irrelevant
									authors = "42" + _authors[0];
								else {
									for (String a : _authors)
										authors += ", " + a;
								}
								player.sendMessage(ChatColor.YELLOW + "> Author(s): " + ChatColor.GOLD + authors.substring(2));
							}
							player.sendMessage(ChatColor.YELLOW + "> Type: " + ChatColor.GOLD + attr.type().toString());
							player.sendMessage(ChatColor.YELLOW + "===<" + ChatColor.GOLD + "Minigame Information" + ChatColor.YELLOW + ">===");
							return;
						}
					}
				}
				return;
			}
			if (s.toLowerCase().contains("join") && r == null) {
				for (String st : lines) {
					try {
						int i = Integer.parseInt(ChatColor.stripColor(st.toLowerCase()));
						rm.join(player, i - 1);
						return;
					} catch (NumberFormatException e) {
						continue;
					}
				}
				rm.join(player);
				return;
			}
			if (s.toLowerCase().contains("leave") && r != null)
				rm.leave(player, false);
		}
	}
	
}
