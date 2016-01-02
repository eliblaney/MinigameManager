package me.donkeycore.minigamemanager.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.donkeycore.minigamemanager.config.MessageType;
import me.donkeycore.minigamemanager.core.MinigameManager;
import me.donkeycore.minigamemanager.events.RotationLeaveEvent;
import net.md_5.bungee.api.ChatColor;

public class CommandLeave implements CommandExecutor {
	
	private final MinigameManager manager;
	
	public CommandLeave(MinigameManager manager) {
		this.manager = manager;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("leave")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("§cOnly players can run this command!");
				return true;
			}
			if (args.length > 0) {
				sender.sendMessage("§cToo many arguments!");
				return false;
			}
			Player player = (Player) sender;
			if(manager.getRotationManager().leave(player))
				Bukkit.getPluginManager().callEvent(new RotationLeaveEvent(player));
			else
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', manager.getMinigameConfig().getMessage(MessageType.NOT_IN_ROTATION)));
		}
		return true;
	}
	
}
