package minigamemanager.commands;

import java.util.UUID;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import minigamemanager.api.minigame.Minigame;
import minigamemanager.api.minigame.MinigameAttributes;
import minigamemanager.api.minigame.MinigameErrors;
import minigamemanager.api.rotation.Rotation;
import minigamemanager.api.rotation.RotationManager;
import minigamemanager.api.rotation.RotationState;
import minigamemanager.config.MessageType;
import minigamemanager.config.MinigameMessages;
import minigamemanager.core.MinigameManager;

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
		MinigameMessages messages = MinigameManager.getMinigameManager().getMessages();
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
					manager.getMinigameSettings().reloadConfig();
					manager.getMessages().reloadConfig();
					manager.getPlayerProfileConfig().reloadConfig();
					MinigameManager.getPlugin().loadDefaultMinigames();
					sender.sendMessage(messages.getMessage(MessageType.CONFIG_RELOADED));
				} else if (args[0].equalsIgnoreCase("list") && sender.hasPermission("minigamemanager.admin.list")) {
					String minigames = "";
					for (Class<? extends Minigame> mclazz : manager.getMinigames()) {
						MinigameAttributes attr = mclazz.getAnnotation(MinigameAttributes.class);
						if (attr == null)
							continue;
						minigames += ChatColor.YELLOW + ", " + ChatColor.GOLD + attr.name();
					}
					String enabled = messages.getMessage(MessageType.ENABLED_MINIGAMES);
					if (minigames.length() > 4)
						sender.sendMessage(enabled.replace("%minigames%", minigames.substring(4)));
					else
						sender.sendMessage(enabled.replace("%minigames%", ChatColor.RED + "(none)"));
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
						sender.sendMessage(messages.getMessage(MessageType.NOT_VALID_ROTATION_ID));
					else {
						if (r.getState() == RotationState.STOPPED) {
							r.resume();
							sender.sendMessage(messages.getMessage(MessageType.STARTED_ROTATION).replace("%id%", id + ""));
						} else
							sender.sendMessage(messages.getMessage(MessageType.ALREADY_RUNNING));
					}
				} else if (args[0].equalsIgnoreCase("stop") && sender.hasPermission("minigamemanager.admin.stop")) {
					if (!valid)
						sender.sendMessage(messages.getMessage(MessageType.NOT_VALID_ROTATION_ID));
					else {
						if (r.getState() == RotationState.STOPPED)
							sender.sendMessage(messages.getMessage(MessageType.ALREADY_STOPPED));
						else {
							r.stop(MinigameErrors.INTERRUPT);
							sender.sendMessage(messages.getMessage(MessageType.STOPPED_ROTATION).replace("%id%", id + ""));
						}
					}
				} else if (args[0].equalsIgnoreCase("force") && sender.hasPermission("minigamemanager.admin.force")) {
					if (!valid)
						sender.sendMessage(messages.getMessage(MessageType.NOT_VALID_ROTATION_ID));
					else {
						if (r.getState() == RotationState.LOBBY) {
							rm.force(r);
							sender.sendMessage(messages.getMessage(MessageType.FORCED_ROTATION).replace("%id%", id + ""));
						} else
							sender.sendMessage(messages.getMessage(MessageType.NOT_IN_LOBBY));
					}
				} else if (args[0].equalsIgnoreCase("status") && sender.hasPermission("minigamemanager.admin.status")) {
					if (!valid)
						sender.sendMessage(messages.getMessage(MessageType.NOT_VALID_ROTATION_ID));
					else {
						String status = messages.getMessage(MessageType.ROTATION_STATUS);
						status = status.replace("%id%", id + "");
						status = status.replace("%state%", r.getState().toColoredString());
						status = status.replace("%playersize%", r.getPlayers().size() + "");
						if (r.getState() == RotationState.INGAME) {
							status = status.replace("%minigame%", r.getCurrentMinigame().getName().replace('_', ' '));
							status = status.replace("%ingamesize%", r.getInGame().size() + "");
							if (r.getInGame().size() > 0) {
								String players = "";
								for (UUID u : r.getPlayers())
									players += ", " + Bukkit.getPlayer(u).getName();
								status = status.replace("%ingame%", players.substring(2));
							} else
								status = status.replace("%ingame%", ChatColor.RED + "(none)");
						} else
							status = Pattern.compile("<minigame>.*</minigame>", Pattern.DOTALL).matcher(status).replaceAll("");
						status = status.replaceAll("</?minigame>", "");
						if (r.getPlayers().size() > 0) {
							String players = "";
							for (UUID u : r.getPlayers())
								players += ", " + Bukkit.getPlayer(u).getName();
							status = status.replace("%players%", players.substring(2));
						} else
							status = status.replace("%players%", ChatColor.RED + "(none)");
						sender.sendMessage(status);
					}
				} else if (args[0].equalsIgnoreCase("info")) {
					for (Class<? extends Minigame> mclazz : manager.getMinigames()) {
						MinigameAttributes attr = mclazz.getAnnotation(MinigameAttributes.class);
						if (attr == null)
							continue;
						if (args[1].replace(" ", "").replace("_", "").replace("-", "").equalsIgnoreCase(attr.name().replace(" ", "").replace("_", "").replace("-", ""))) {
							String info = messages.getMessage(MessageType.MINIGAME_INFO);
							info = info.replace("%name%", attr.name().replace('_', ' '));
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
								info = info.replace("%authors%", authors.substring(2));
							} else
								info = Pattern.compile("<author>.*</author>", Pattern.DOTALL).matcher(info).replaceAll("");
							info = info.replaceAll("</?author>", "");
							info = info.replace("%type%", attr.type().toString());
							sender.sendMessage(info);
							return true;
						}
					}
					sender.sendMessage(messages.getMessage(MessageType.COULDNT_FIND_MINIGAME).replace("%name%", args[1]));
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
						sender.sendMessage(messages.getMessage(MessageType.NOT_VALID_ROTATION_ID));
						return true;
					}
					if (r.getState() != RotationState.STOPPED && r.getState() != RotationState.INGAME) {
						sender.sendMessage(messages.getMessage(MessageType.ERROR_NEXT));
						return true;
					}
					for (Class<? extends Minigame> mclazz : manager.getMinigames()) {
						MinigameAttributes attr = mclazz.getAnnotation(MinigameAttributes.class);
						if (attr == null)
							continue;
						if (args[2].replace(" ", "").replace("_", "").replace("-", "").equalsIgnoreCase(attr.name().replace(" ", "").replace("_", "").replace("-", ""))) {
							rm.setNext(id - 1, mclazz);
							sender.sendMessage(messages.getMessage(MessageType.SET_NEXT).replace("%id%", id + "").replace("%minigame%", attr.name().replace('_', ' ')));
							return true;
						}
					}
					sender.sendMessage(messages.getMessage(MessageType.COULDNT_FIND_MINIGAME).replace("%name%", args[2]));
				} else
					return false;
			} else
				return false;
			return true;
		} else
			return false;
	}
	
}
