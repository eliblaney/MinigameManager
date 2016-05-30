package minigamemanager.rotations;

import org.bukkit.scheduler.BukkitTask;

import minigamemanager.api.minigame.Minigame;
import minigamemanager.api.rotation.RotationState;
import minigamemanager.config.MessageType;
import minigamemanager.core.MinigameManager;

/**
 * Countdown for rotations
 * 
 * @author DonkeyCore
 */
public class Countdown implements Runnable {
	
	private final MinigameManager manager;
	private final DefaultRotationManager rm;
	private final DefaultRotation r;
	private final Minigame minigame;
	private final boolean force;
	private BukkitTask bt;
	private int secondsLeft = 30;
	
	Countdown(MinigameManager manager, DefaultRotationManager rm, DefaultRotation r, Minigame minigame, boolean force) {
		this.manager = manager;
		this.rm = rm;
		this.r = r;
		this.minigame = minigame;
		this.force = force;
		this.secondsLeft = getTotalSecondsLeft();
	}
	
	void setTask(BukkitTask bt) {
		this.bt = bt;
	}
	
	// Get config number of seconds to wait or 30 if seconds <= 0
	private int getTotalSecondsLeft() {
		// Subtract one because timer is delayed 1 second (for the next-minigame message)
		int seconds = manager.getMinigameSettings().getCountdownSeconds() - 1;
		if (seconds < 0)
			return 30;
		return seconds;
	}
	
	public void run() {
		if (!force && r.getPlayers().size() < manager.getMinigamesWithMinimums().get(minigame.getClass())) {
			r.announce(manager.getMessages().getMessage(MessageType.NOT_ENOUGH_PLAYERS));
			if (bt != null) {
				r.setLobbyScoreboard();
				bt.cancel();
			}
			return;
		}
		if (r.getState() == RotationState.STOPPED) { // abandon ship!
			r.announce(manager.getMessages().getMessage(MessageType.ROTATION_STOPPED));
			if (bt != null) {
				r.setMinigame(null);
				bt.cancel();
			}
			return;
		}
		// Start when timer hits 0
		if (secondsLeft <= 0) {
			bt.cancel();
			rm.start(r, minigame);
			// Announce to all players that a game is starting every 10 seconds
		} else if (secondsLeft % 10 == 0 || secondsLeft <= 5)
			r.announce(manager.getMessages().getMessage(MessageType.COUNTDOWN).replace("%minigame%", minigame.getName().replace("_", " ")).replace("%time%", getTimeLeft(secondsLeft--)));
		else
			secondsLeft--;
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
