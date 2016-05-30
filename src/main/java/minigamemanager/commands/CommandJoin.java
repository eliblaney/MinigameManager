package minigamemanager.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import minigamemanager.api.rotation.RotationManager;
import minigamemanager.config.MessageType;
import minigamemanager.config.MinigameMessages;
import minigamemanager.core.MinigameManager;
import minigamemanager.events.rotation.RotationJoinEvent;

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
		MinigameMessages messages = MinigameManager.getMinigameManager().getMessages();
		if (cmd.getName().equalsIgnoreCase("join")) {
			if (manager.getMinigameSettings().entireServer()) {
				sender.sendMessage(messages.getMessage(MessageType.COMMAND_DISABLED));
				return true;
			}
			if (!(sender instanceof Player)) {
				sender.sendMessage(messages.getMessage(MessageType.ONLY_PLAYERS));
				return true;
			}
			if (args.length > 1) {
				sender.sendMessage(messages.getMessage(MessageType.TOO_MANY_ARGUMENTS));
				return false;
			}
			Player player = (Player) sender;
			if (manager.getRotationManager().getRotation(player) != null) {
				player.sendMessage(messages.getMessage(MessageType.ALREADY_IN_ROTATION));
				return true;
			}
			int rotation;
			try {
				rotation = args.length == 1 ? Integer.parseInt(args[0]) : Integer.MIN_VALUE;
			} catch (NumberFormatException e) {
				player.sendMessage(messages.getMessage(MessageType.NOT_VALID_NUMBER));
				return true;
			}
			int maxRotations = manager.getMinigameSettings().getNumberOfRotations();
			if ((rotation < 1 && args.length == 1) || rotation > maxRotations) {
				String error = messages.getMessage(MessageType.NOT_VALID_ROTATION_ID_LIST);
				String rotations = "1";
				if (maxRotations > 1)
					rotations += "-" + maxRotations;
				player.sendMessage(error.replace("%rotations%", rotations));
				return true;
			}
			RotationManager rm = manager.getRotationManager();
			if (rotation == Integer.MIN_VALUE) {
				if (rm.join(player))
					Bukkit.getPluginManager().callEvent(new RotationJoinEvent(rm.getRotation(player), player));
				else
					player.sendMessage(messages.getMessage(MessageType.FULL_ROTATIONS));
			} else {
				if (rm.join(player, rotation - 1))
					Bukkit.getPluginManager().callEvent(new RotationJoinEvent(rm.getRotation(player), player));
				else
					player.sendMessage(messages.getMessage(MessageType.FULL_ROTATION));
			}
			return true;
		} else
			return false;
	}
	
}
