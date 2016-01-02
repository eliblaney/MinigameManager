package me.donkeycore.minigamemanager.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.donkeycore.minigamemanager.config.MessageType;
import me.donkeycore.minigamemanager.core.MinigameManager;
import me.donkeycore.minigamemanager.events.RotationJoinEvent;
import net.md_5.bungee.api.ChatColor;

public class CommandJoin implements CommandExecutor {
	
	private final MinigameManager manager;
	
	public CommandJoin(MinigameManager manager) {
		this.manager = manager;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("join")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("§cOnly players can run this command!");
				return true;
			}
			if (args.length > 1) {
				sender.sendMessage("§cToo many arguments!");
				return false;
			}
			Player player = (Player) sender;
			int rotation;
			try {
				rotation = args.length == 1 ? Integer.parseInt(args[0]) : Integer.MIN_VALUE;
			} catch (NumberFormatException e) {
				player.sendMessage("§cThat is not a valid number!");
				return true;
			}
			int maxRotations = manager.getMinigameConfig().getNumberOfRotations();
			if ((rotation < 1 && args.length == 1) || rotation > maxRotations) {
				String error = "§cThat is not a valid rotation ID! Available rotations: 1";
				if (maxRotations > 1)
					error += "-" + maxRotations;
				player.sendMessage(error);
				return true;
			}
			if (rotation == Integer.MIN_VALUE) {
				if (manager.getRotationManager().join(player))
					Bukkit.getPluginManager().callEvent(new RotationJoinEvent(player));
				else
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', manager.getMinigameConfig().getMessage(MessageType.FULL_ROTATIONS)));
			} else {
				if (manager.getRotationManager().join(player, rotation))
					Bukkit.getPluginManager().callEvent(new RotationJoinEvent(player));
				else
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', manager.getMinigameConfig().getMessage(MessageType.FULL_ROTATION)));
			}
			return true;
		} else
			return false;
	}
	
}
