package me.donkeycore.minigamemanager.rotations;

import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitTask;

import me.donkeycore.minigamemanager.api.minigame.Minigame;
import me.donkeycore.minigamemanager.api.rotation.RotationState;
import me.donkeycore.minigamemanager.config.MessageType;
import me.donkeycore.minigamemanager.core.MinigameManager;

public class Countdown implements Runnable {
	
	private final MinigameManager manager;
	private final DefaultRotationManager rm;
	private final DefaultRotation r;
	private final Minigame minigame;
	private BukkitTask bt;
	private int secondsLeft = getTotalSecondsLeft();
	
	Countdown(MinigameManager manager, DefaultRotationManager rm, DefaultRotation r, Minigame minigame) {
		this.manager = manager;
		this.rm = rm;
		this.r = r;
		this.minigame = minigame;
	}
	
	void setTask(BukkitTask bt) {
		this.bt = bt;
	}
	
	// Get config number of seconds to wait or 30 if seconds <= 0
	private int getTotalSecondsLeft() {
		// Subtract one because timer is delayed 1 second (for the next-minigame message)
		int seconds = manager.getMinigameConfig().getCountdownSeconds() - 1;
		if (seconds < 0)
			return 30;
		return seconds;
	}
	
	public void run() {
		if (r.getPlayers().size() < manager.getMinigamesWithMinimums().get(minigame)) {
			r.announce(ChatColor.translateAlternateColorCodes('&', manager.getMinigameConfig().getMessage(MessageType.NOT_ENOUGH_PLAYERS)));
			if (bt != null)
				bt.cancel();
			return;
		}
		if (r.getState() == RotationState.STOPPED) { // abandon ship!
			r.announce(ChatColor.translateAlternateColorCodes('&', manager.getMinigameConfig().getMessage(MessageType.ROTATION_STOPPED)));
			if (bt != null)
				bt.cancel();
			return;
		}
		// Start when timer hits 0
		if (secondsLeft <= 0) {
			bt.cancel();
			rm.start(r, minigame);
			// Announce to all players that a game is starting every 10 seconds
		} else if (secondsLeft % 10 == 0 || secondsLeft <= 5)
			r.announce(ChatColor.translateAlternateColorCodes('&', manager.getMinigameConfig().getMessage(MessageType.COUNTDOWN).replace("%minigame%", minigame.getName()).replace("%time%", getTimeLeft(secondsLeft--))));
	}
	
	// Get friendly string for amount of time left
	private static String getTimeLeft(int seconds) {
		if (seconds < 60) {
			if (seconds == 1)
				return "1 second";
			else
				return seconds + " seconds";
		} else {
			int minutes = seconds / 60;
			int remainder = seconds % 60;
			if (remainder == 0) {
				if (minutes == 1)
					return "1 minute";
				else
					return minutes + " minutes";
			} else {
				if (minutes != 1 && remainder != 1)
					return minutes + " minutes and " + remainder + " seconds";
				else if (minutes != 1)
					return minutes + " minutes and 1 second";
				else if (remainder != 1)
					return "1 minute and " + remainder + " seconds";
				else
					return "1 minute and 1 second";
			}
		}
	}
	
}
