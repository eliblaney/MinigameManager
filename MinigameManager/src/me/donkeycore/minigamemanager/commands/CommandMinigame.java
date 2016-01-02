package me.donkeycore.minigamemanager.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.donkeycore.minigamemanager.core.MinigameManager;

public class CommandMinigame implements CommandExecutor {
	
	private final MinigameManager manager;
	
	public CommandMinigame(MinigameManager manager) {
		this.manager = manager;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return false;
	}
	
}
