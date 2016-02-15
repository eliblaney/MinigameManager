package me.donkeycore.minigamemanager.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
				sender.sendMessage(ChatColor.YELLOW + "========================");
				sender.sendMessage(ChatColor.YELLOW + ">      Minigame" + ChatColor.GOLD + "Manager");
				sender.sendMessage(ChatColor.YELLOW + ">        Version " + ChatColor.GOLD + MinigameManager.getPlugin().getDescription().getVersion());
				sender.sendMessage(ChatColor.YELLOW + ">       by " + ChatColor.GOLD + "DonkeyCore");
				sender.sendMessage(ChatColor.YELLOW + ">      Usage: " + ChatColor.GOLD + "/mm help");
				sender.sendMessage(ChatColor.YELLOW + "========================");
			} else if (args.length == 1) {
				if (args[0].equalsIgnoreCase("help") && sender.hasPermission("minigamemanager.admin.help")) {
					sender.sendMessage(ChatColor.YELLOW + "===<" + ChatColor.GOLD + "MinigameManager Help" + ChatColor.YELLOW + ">===");
					sender.sendMessage(ChatColor.YELLOW + "> " + ChatColor.GOLD + "/mm help " + ChatColor.YELLOW + "- View this help page");
					sender.sendMessage(ChatColor.YELLOW + "> " + ChatColor.GOLD + "/mm reload " + ChatColor.YELLOW + "- Reload the configs");
					sender.sendMessage(ChatColor.YELLOW + "> " + ChatColor.GOLD + "/mm list " + ChatColor.YELLOW + "- Get a list of the minigames");
					sender.sendMessage(ChatColor.YELLOW + "> " + ChatColor.GOLD + "/mm info <minigame> " + ChatColor.YELLOW + "- View information about <minigame>");
					sender.sendMessage(ChatColor.YELLOW + "> " + ChatColor.GOLD + "/mm start <rotation> " + ChatColor.YELLOW + "- Start the rotation cycle; <rotation>: Rotation ID");
					sender.sendMessage(ChatColor.YELLOW + "> " + ChatColor.GOLD + "/mm stop <rotation> " + ChatColor.YELLOW + "- Stop the rotation cycle; <rotation>: Rotation ID");
					sender.sendMessage(ChatColor.YELLOW + "> " + ChatColor.GOLD + "/mm status <rotation> " + ChatColor.YELLOW + "- View the status of rotation; <rotation>: Rotation ID");
					sender.sendMessage(ChatColor.YELLOW + "> " + ChatColor.GOLD + "===<" + ChatColor.GOLD + "MinigameManager Help" + ChatColor.YELLOW + ">===");
				} else if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("minigamemanager.admin.reload")) {
					MinigameManager.getPlugin().reloadConfig();
					MinigameManager.getMinigameManager().getMinigameConfig().reloadConfig();
					MinigameManager.getMinigameManager().getMinigameLocations().reloadConfig();
					MinigameManager.getPlugin().loadDefaultMinigames();
					sender.sendMessage(ChatColor.GREEN + "The configs have been reloaded!");
				} else if (args[0].equalsIgnoreCase("list") && sender.hasPermission("minigamemanager.admin.list")) {
					String minigames = "";
					for (Class<? extends Minigame> mclazz : manager.getMinigames()) {
						MinigameAttributes attr = mclazz.getAnnotation(MinigameAttributes.class);
						if (attr == null)
							continue;
						minigames += ChatColor.YELLOW + ", " + ChatColor.GOLD + attr.name();
					}
					if (minigames.length() > 4)
						sender.sendMessage(ChatColor.YELLOW + "Enabled minigames: " + minigames.substring(4));
					else
						sender.sendMessage(ChatColor.YELLOW + "Enabled minigames: " + ChatColor.RED + "(none)");
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
						sender.sendMessage(ChatColor.RED + "That is not a valid rotation id!");
					else {
						if (r.getState() == RotationState.STOPPED) {
							r.resume();
							sender.sendMessage(ChatColor.GREEN + "Started rotation #" + id);
						} else
							sender.sendMessage(ChatColor.RED + "Rotation is already running");
					}
				} else if (args[0].equalsIgnoreCase("stop") && sender.hasPermission("minigamemanager.admin.stop")) {
					if (!valid)
						sender.sendMessage(ChatColor.RED + "That is not a valid rotation id!");
					else {
						if (r.getState() == RotationState.STOPPED)
							sender.sendMessage(ChatColor.RED + "Rotation is already stopped");
						else {
							r.stop(MinigameErrors.INTERRUPT);
							sender.sendMessage(ChatColor.GREEN + "Stopped rotation #" + id);
						}
					}
				} else if (args[0].equalsIgnoreCase("force") && sender.hasPermission("minigamemanager.admin.force")) {
					if (!valid)
						sender.sendMessage(ChatColor.RED + "That is not a valid rotation id!");
					else {
						if (r.getState() == RotationState.LOBBY) {
							rm.force(r);
							sender.sendMessage(ChatColor.GREEN + "Forced rotation #" + id + " to start the countdown");
						} else
							sender.sendMessage(ChatColor.RED + "Rotation #" + id + " is not currently in the lobby.");
					}
				} else if (args[0].equalsIgnoreCase("status") && sender.hasPermission("minigamemanager.admin.status")) {
					if (!valid)
						sender.sendMessage(ChatColor.YELLOW + "That is not a valid rotation id!");
					else {
						sender.sendMessage(ChatColor.YELLOW + "===<" + ChatColor.GOLD + "Rotation Information" + ChatColor.YELLOW + ">===");
						sender.sendMessage(ChatColor.YELLOW + "> ID: " + ChatColor.GOLD + id);
						sender.sendMessage(ChatColor.YELLOW + "> State: " + r.getState().toColoredString());
						if (r.getState() == RotationState.INGAME) {
							sender.sendMessage(ChatColor.YELLOW + "> Minigame: " + ChatColor.GOLD + r.getCurrentMinigame().getName().replace("_", ""));
							sender.sendMessage(ChatColor.YELLOW + "> Ingame: " + ChatColor.GOLD + r.getInGame().size());
						}
						sender.sendMessage(ChatColor.YELLOW + "> Players (" + r.getPlayers().size() + "):");
						if (r.getPlayers().size() > 0) {
							String players = "";
							for (UUID u : r.getPlayers())
								players += ", " + Bukkit.getPlayer(u).getName();
							sender.sendMessage("  " + ChatColor.GOLD + "> " + players.substring(2));
						} else
							sender.sendMessage("  " + ChatColor.GOLD + "> " + ChatColor.YELLOW + "(none)");
						sender.sendMessage(ChatColor.YELLOW + "===<" + ChatColor.GOLD + "Rotation Information" + ChatColor.YELLOW + ">===");
					}
				} else if (args[0].equalsIgnoreCase("info")) {
					for (Class<? extends Minigame> mclazz : manager.getMinigames()) {
						MinigameAttributes attr = mclazz.getAnnotation(MinigameAttributes.class);
						if (attr == null)
							continue;
						if (args[1].replace(" ", "").replace("_", "").replace("-", "").equalsIgnoreCase(attr.name().replace(" ", "").replace("_", "").replace("-", ""))) {
							sender.sendMessage(ChatColor.YELLOW + "===<" + ChatColor.GOLD + "Minigame Information" + ChatColor.YELLOW + ">===");
							sender.sendMessage(ChatColor.YELLOW + "> Name: " + ChatColor.GOLD + attr.name().replace("_", " "));
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
								sender.sendMessage(ChatColor.YELLOW + "> Author(s): " + ChatColor.GOLD + authors.substring(2));
							}
							sender.sendMessage(ChatColor.YELLOW + "> Type: " + ChatColor.GOLD + attr.type().toString());
							sender.sendMessage(ChatColor.YELLOW + "===<" + ChatColor.GOLD + "Minigame Information" + ChatColor.YELLOW + ">===");
							return true;
						}
					}
					sender.sendMessage(ChatColor.RED + "Could not find minigame with name: " + ChatColor.DARK_RED + args[1]);
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
						sender.sendMessage(ChatColor.RED + "That is not a valid rotation id!");
						return true;
					}
					if(r.getState() != RotationState.STOPPED && r.getState() != RotationState.INGAME) {
						sender.sendMessage(ChatColor.RED + "Rotation state must either be stopped or already ingame to set the next minigame");
						return true;
					}
					for (Class<? extends Minigame> mclazz : manager.getMinigames()) {
						MinigameAttributes attr = mclazz.getAnnotation(MinigameAttributes.class);
						if (attr == null)
							continue;
						if (args[2].replace(" ", "").replace("_", "").replace("-", "").equalsIgnoreCase(attr.name().replace(" ", "").replace("_", "").replace("-", ""))) {
							rm.setNext(mclazz);
							sender.sendMessage(ChatColor.GREEN + "Next minigame for rotation " + ChatColor.DARK_GREEN + "#" + id + ChatColor.GREEN + " has been set to: " + ChatColor.DARK_GREEN + attr.name());
							return true;
						}
					}
					sender.sendMessage(ChatColor.RED + "Could not find minigame with name: " + ChatColor.DARK_RED + args[2]);
				} else
					return false;
			} else
				return false;
			return true;
		} else
			return false;
	}
	
}
