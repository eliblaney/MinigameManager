package me.donkeycore.minigamemanager.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.donkeycore.minigamemanager.api.rotation.Rotation;
import me.donkeycore.minigamemanager.config.MessageType;
import me.donkeycore.minigamemanager.core.MinigameManager;
import me.donkeycore.minigamemanager.events.rotation.RotationLeaveEvent;

/**
 * "/leave" command - Leaves a rotation
 * 
 * @author DonkeyCore
 */
public class CommandLeave implements CommandExecutor {
	
	private final MinigameManager manager;
	
	public CommandLeave(MinigameManager manager) {
		this.manager = manager;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("leave")) {
			if(manager.getMinigameSettings().entireServer()) {
				sender.sendMessage(ChatColor.RED + "This command is disabled.");
				return true;
			}
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Only players can run this command!");
				return true;
			}
			if (args.length > 0) {
				sender.sendMessage(ChatColor.RED + "Too many arguments!");
				return false;
			}
			Player player = (Player) sender;
			Rotation rotation = manager.getRotationManager().getRotation(player);
			if(manager.getRotationManager().leave(player, false))
				Bukkit.getPluginManager().callEvent(new RotationLeaveEvent(rotation, player));
			else
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', manager.getMinigameSettings().getMessage(MessageType.NOT_IN_ROTATION)));
		}
		return true;
	}
	
}
