package me.donkeycore.minigamemanager.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.donkeycore.minigamemanager.api.rotation.RotationManager;
import me.donkeycore.minigamemanager.config.MessageType;
import me.donkeycore.minigamemanager.core.MinigameManager;
import me.donkeycore.minigamemanager.events.rotation.RotationJoinEvent;

/**
 * "/join" command - Joins a rotation
 * 
 * @author DonkeyCore
 */
public class CommandJoin implements CommandExecutor {
	
	private final MinigameManager manager;
	
	public CommandJoin(MinigameManager manager) {
		this.manager = manager;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("join")) {
			if(manager.getMinigameSettings().entireServer()) {
				sender.sendMessage(ChatColor.RED + "This command is disabled.");
				return true;
			}
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Only players can run this command!");
				return true;
			}
			if (args.length > 1) {
				sender.sendMessage(ChatColor.RED + "Too many arguments!");
				return false;
			}
			Player player = (Player) sender;
			if(manager.getRotationManager().getRotation(player) != null) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', manager.getMinigameSettings().getMessage(MessageType.ALREADY_IN_ROTATION)));
				return true;
			}
			int rotation;
			try {
				rotation = args.length == 1 ? Integer.parseInt(args[0]) : Integer.MIN_VALUE;
			} catch (NumberFormatException e) {
				player.sendMessage(ChatColor.RED + "That is not a valid number!");
				return true;
			}
			int maxRotations = manager.getMinigameSettings().getNumberOfRotations();
			if ((rotation < 1 && args.length == 1) || rotation > maxRotations) {
				String error = ChatColor.RED + "That is not a valid rotation ID! Available rotations: 1";
				if (maxRotations > 1)
					error += "-" + maxRotations;
				player.sendMessage(error);
				return true;
			}
			RotationManager rm = manager.getRotationManager();
			if (rotation == Integer.MIN_VALUE) {
				if (rm.join(player))
					Bukkit.getPluginManager().callEvent(new RotationJoinEvent(rm.getRotation(player), player));
				else
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', manager.getMinigameSettings().getMessage(MessageType.FULL_ROTATIONS)));
			} else {
				if (rm.join(player, rotation - 1))
					Bukkit.getPluginManager().callEvent(new RotationJoinEvent(rm.getRotation(player), player));
				else
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', manager.getMinigameSettings().getMessage(MessageType.FULL_ROTATION)));
			}
			return true;
		} else
			return false;
	}
	
}
