package me.donkeycore.minigamemanager.api.scoreboard;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import me.donkeycore.minigamemanager.core.MinigameManager;

public class AdvancedScoreboardHelper {
	
	/**
	 * Bukkit scoreboard being used
	 */
	private final Scoreboard scoreboard;
	/**
	 * Bukkit objective being used
	 */
	private Objective objective;
	/**
	 * Recipients for repeated updates
	 */
	private Player[] recipients;
	/**
	 * Repeated tasks for updates
	 */
	private BukkitTask timer;
	
	/**
	 * Create an advanced scoreboard helper
	 * 
	 * @param scoreboard The Bukkit scoreboard to use
	 * @param objective The Bukkit objective to use
	 */
	public AdvancedScoreboardHelper(Scoreboard scoreboard, Objective objective) {
		this.scoreboard = scoreboard;
		this.objective = objective;
	}
	
	/**
	 * Get the Bukkit instance of the scoreboard
	 * 
	 * @return The Bukkit scoreboard
	 */
	public Scoreboard getScoreboard() {
		return scoreboard;
	}
	
	/**
	 * Get the objective of the scoreboard
	 * 
	 * @return The Bukkit objective
	 */
	public Objective getObjective() {
		return objective;
	}
	
	/**
	 * Set the recipients of the scoreboard that will be used for repeating
	 * tasks
	 * 
	 * @param recipients The new recipients for the scoreboard
	 * 
	 * @return The helper instance
	 */
	public AdvancedScoreboardHelper setRecipients(Player... recipients) {
		this.recipients = recipients;
		return this;
	}
	
	/**
	 * Start a repeating task
	 * 
	 * @param tickDelay The time to wait in between executions
	 * @param onUpdate What to execute
	 * 
	 * @return The helper instance
	 */
	public AdvancedScoreboardHelper setUpdateInterval(int tickDelay, final Runnable onUpdate) {
		stopUpdating();
		timer = new BukkitRunnable() {
			
			@Override
			public void run() {
				onUpdate.run();
				if (timer != null) {
					for (Player player : recipients)
						player.setScoreboard(scoreboard);
				}
			}
		}.runTaskTimer(MinigameManager.getPlugin(), 0, tickDelay);
		return this;
	}
	
	/**
	 * Stop the scheduler from updating
	 * 
	 * @return The helper instance
	 */
	public AdvancedScoreboardHelper stopUpdating() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		return this;
	}
	
	/**
	 * Set the lines of the scoreboard to what is specified in order
	 * 
	 * @param lines The new lines of the scoreboard
	 * 
	 * @return The helper instance
	 */
	public AdvancedScoreboardHelper setOrderedLines(String... lines) {
		clear();
		int n = lines.length;
		for (String str : lines)
			objective.getScore(str).setScore(--n);
		return this;
	}
	
	/**
	 * Set the lines of the scoreboard to what is specified in any order
	 * 
	 * @param lines The new lines of the scoreboard
	 * 
	 * @return The helper instance
	 */
	public AdvancedScoreboardHelper setLines(String... lines) {
		clear();
		for (String str : lines)
			objective.getScore(str).setScore(0);
		return this;
	}
	
	/**
	 * Set the lines of the scoreboard to what is specified with scores
	 * 
	 * @param scores A map representing the new lines of the scoreboard
	 * 
	 * @return The helper instance
	 */
	public AdvancedScoreboardHelper setLines(Map<String, Integer> scores) {
		clear();
		for (Entry<String, Integer> e : scores.entrySet()) {
			objective.getScore(e.getKey()).setScore(e.getValue());
		}
		return this;
	}
	
	/**
	 * Clear all the lines in the scorebaord
	 * 
	 * @return The helper instance
	 */
	public AdvancedScoreboardHelper clear() {
		for (String str : scoreboard.getEntries())
			scoreboard.resetScores(str);
		return this;
	}
	
}
