package me.donkeycore.minigamemanager.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.donkeycore.minigamemanager.api.minigame.Minigame;
import me.donkeycore.minigamemanager.api.minigame.MinigameAttributes;
import me.donkeycore.minigamemanager.api.minigame.MinigameErrors;
import me.donkeycore.minigamemanager.api.rotation.Rotation;
import me.donkeycore.minigamemanager.api.rotation.RotationManager;
import me.donkeycore.minigamemanager.api.rotation.RotationState;
import me.donkeycore.minigamemanager.core.MinigameManager;

/**
 * "/minigamemanager" command - Handles administrator manipulation
 * 
 * @author DonkeyCore
 */
public class CommandMinigame implements CommandExecutor {
	
	private final MinigameManager manager;
	
	public CommandMinigame(MinigameManager manager) {
		this.manager = manager;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("minigamemanager")) {
			if (args.length == 0) {
				sender.sendMessage("\u00a7e========================");
				sender.sendMessage("\u00a7e>      Minigame\u00a76Manager");
				sender.sendMessage("\u00a7e>        Version \u00a76" + MinigameManager.getPlugin().getDescription().getVersion());
				sender.sendMessage("\u00a7e>       by \u00a76DonkeyCore");
				sender.sendMessage("\u00a7e>      Usage: \u00a76/mm help");
				sender.sendMessage("\u00a7e========================");
			} else if (args.length == 1) {
				if (args[0].equalsIgnoreCase("help") && sender.hasPermission("minigamemanager.admin.help")) {
					sender.sendMessage("\u00a7e===<\u00a76MinigameManager Help\u00a7e>===");
					sender.sendMessage("\u00a7e> \u00a76/mm help \u00a7e- View this help page");
					sender.sendMessage("\u00a7e> \u00a76/mm reload \u00a7e- Reload the configs");
					sender.sendMessage("\u00a7e> \u00a76/mm list \u00a7e- Get a list of the minigames");
					sender.sendMessage("\u00a7e> \u00a76/mm info <minigame> \u00a7e- View information about <minigame>");
					sender.sendMessage("\u00a7e> \u00a76/mm start <rotation> \u00a7e- Start the rotation cycle; <rotation>: Rotation ID");
					sender.sendMessage("\u00a7e> \u00a76/mm stop <rotation> \u00a7e- Stop the rotation cycle; <rotation>: Rotation ID");
					sender.sendMessage("\u00a7e> \u00a76/mm status <rotation> \u00a7e- View the status of rotation; <rotation>: Rotation ID");
					sender.sendMessage("\u00a7e> \u00a7e===<\u00a76MinigameManager Help\u00a7e>===");
				} else if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("minigamemanager.admin.reload")) {
					MinigameManager.getPlugin().reloadConfig();
					MinigameManager.getMinigameManager().getMinigameConfig().reloadConfig();
					MinigameManager.getMinigameManager().getMinigameLocations().reloadConfig();
					MinigameManager.getPlugin().loadDefaultMinigames();
					sender.sendMessage("\u00a7aThe configs have been reloaded!");
				} else if (args[0].equalsIgnoreCase("list") && sender.hasPermission("minigamemanager.admin.list")) {
					String minigames = "";
					for (Class<? extends Minigame> mclazz : manager.getMinigames()) {
						MinigameAttributes attr = mclazz.getAnnotation(MinigameAttributes.class);
						if (attr == null)
							continue;
						minigames += "\u00a7e, \u00a76" + attr.name();
					}
					if (minigames.length() > 4)
						sender.sendMessage("\u00a7eEnabled minigames: " + minigames.substring(4));
					else
						sender.sendMessage("\u00a7eEnabled minigames: \u00a7c(none)");
				} else
					return false;
			} else if (args.length == 2) {
				int id = -1;
				try {
					id = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {}
				RotationManager rm = manager.getRotationManager();
				boolean valid = id > 0 && id <= rm.getRotations().length;
				Rotation r = valid ? rm.getRotation(id - 1) : null;
				if (args[0].equalsIgnoreCase("start") && sender.hasPermission("minigamemanager.admin.start")) {
					if (!valid)
						sender.sendMessage("\u00a7cThat is not a valid rotation id!");
					else {
						if (r.getState() == RotationState.STOPPED) {
							r.resume();
							sender.sendMessage("\u00a7aStarted rotation #" + id);
						} else
							sender.sendMessage("\u00a7cRotation is already running");
					}
				} else if (args[0].equalsIgnoreCase("stop") && sender.hasPermission("minigamemanager.admin.stop")) {
					if (!valid)
						sender.sendMessage("\u00a7cThat is not a valid rotation id!");
					else {
						if (r.getState() == RotationState.STOPPED)
							sender.sendMessage("\u00a7cRotation is already stopped");
						else {
							r.stop(MinigameErrors.INTERRUPT);
							sender.sendMessage("\u00a7aStopped rotation #" + id);
						}
					}
				} else if (args[0].equalsIgnoreCase("force") && sender.hasPermission("minigamemanager.admin.force")) {
					if (!valid)
						sender.sendMessage("\u00a7cThat is not a valid rotation id!");
					else {
						if (r.getState() == RotationState.LOBBY) {
							rm.force(r);
							sender.sendMessage("\u00a7aForced rotation #" + id + " to start the countdown");
						} else
							sender.sendMessage("\u00a7cRotation #" + id + " is not currently in the lobby.");
					}
				} else if (args[0].equalsIgnoreCase("status") && sender.hasPermission("minigamemanager.admin.status")) {
					if (!valid)
						sender.sendMessage("\u00a7cThat is not a valid rotation id!");
					else {
						sender.sendMessage("\u00a7e===<\u00a76Rotation Information\u00a7e>===");
						sender.sendMessage("\u00a7e> ID: \u00a76" + id);
						sender.sendMessage("\u00a7e> State: " + r.getState().toColoredString());
						if (r.getState() == RotationState.INGAME) {
							sender.sendMessage("\u00a7e> Minigame: \u00a76" + r.getCurrentMinigame().getName().replace("_", ""));
							sender.sendMessage("\u00a7e> Ingame: \u00a76" + r.getInGame().size());
						}
						sender.sendMessage("\u00a7e> Players (" + r.getPlayers().size() + "):");
						if (r.getPlayers().size() > 0) {
							String players = "";
							for (UUID u : r.getPlayers())
								players += ", " + Bukkit.getPlayer(u).getName();
							sender.sendMessage("  \u00a76> " + players.substring(2));
						} else
							sender.sendMessage("  \u00a76> \u00a7e(none)");
						sender.sendMessage("\u00a7e===<\u00a76Rotation Information\u00a7e>===");
					}
				} else if (args[0].equalsIgnoreCase("info")) {
					for (Class<? extends Minigame> mclazz : manager.getMinigames()) {
						MinigameAttributes attr = mclazz.getAnnotation(MinigameAttributes.class);
						if (attr == null)
							continue;
						if (args[1].replace(" ", "").replace("_", "").replace("-", "").equalsIgnoreCase(attr.name().replace(" ", "").replace("_", "").replace("-", ""))) {
							sender.sendMessage("\u00a7e===<\u00a76Minigame Information\u00a7e>===");
							sender.sendMessage("\u00a7e> Name: \u00a76" + attr.name().replace("_", " "));
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
								sender.sendMessage("\u00a7e> Author(s): \u00a76" + authors.substring(2));
							}
							sender.sendMessage("\u00a7e> Type: \u00a76" + attr.type().toString());
							sender.sendMessage("\u00a7e===<\u00a76Minigame Information\u00a7e>===");
							return true;
						}
					}
					sender.sendMessage("\u00a7cCould not find minigame with name: \u00a74" + args[1]);
				} else
					return false;
			} else if (args.length == 3) {
				int id = -1;
				try {
					id = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {}
				RotationManager rm = manager.getRotationManager();
				boolean valid = id > 0 && id <= rm.getRotations().length;
				Rotation r = valid ? rm.getRotation(id - 1) : null;
				if (args[0].equalsIgnoreCase("next") && sender.hasPermission("minigamemanager.admin.next")) {
					if (!valid) {
						sender.sendMessage("\u00a7cThat is not a valid rotation id!");
						return true;
					}
					if(r.getState() != RotationState.STOPPED && r.getState() != RotationState.INGAME) {
						sender.sendMessage("\u00a7cRotation state must either be stopped or already ingame to set the next minigame");
						return true;
					}
					for (Class<? extends Minigame> mclazz : manager.getMinigames()) {
						MinigameAttributes attr = mclazz.getAnnotation(MinigameAttributes.class);
						if (attr == null)
							continue;
						if (args[2].replace(" ", "").replace("_", "").replace("-", "").equalsIgnoreCase(attr.name().replace(" ", "").replace("_", "").replace("-", ""))) {
							rm.setNext(mclazz);
							sender.sendMessage("\u00a7aNext minigame for rotation #" + id + " has been set to: " + attr.name());
							return true;
						}
					}
					sender.sendMessage("\u00a7cCould not find minigame with name: \u00a74" + args[2]);
				} else
					return false;
			} else
				return false;
			return true;
		} else
			return false;
	}
	
}
