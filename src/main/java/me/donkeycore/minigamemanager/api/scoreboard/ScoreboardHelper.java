package me.donkeycore.minigamemanager.api.scoreboard;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import me.donkeycore.minigamemanager.core.MinigameManager;

public class ScoreboardHelper {
	
	private final Scoreboard scoreboard;
	private Player[] recipients;
	private BukkitTask timer;
	private Objective objective;
	
	public ScoreboardHelper(Scoreboard scoreboard, Objective objective) {
		this.scoreboard = scoreboard;
		this.objective = objective;
	}
	
	public Scoreboard getScoreboard() {
		return scoreboard;
	}
	
	public Objective getObjective() {
		return objective;
	}
	
	public ScoreboardHelper setRecipients(Player... recipients) {
		this.recipients = recipients;
		return this;
	}
	
	public ScoreboardHelper setUpdateInterval(int tickDelay, final Runnable onUpdate) {
		stopUpdating();
		timer = new BukkitRunnable() {
			
			@Override
			public void run() {
				onUpdate.run();
				for(Player player : recipients)
					player.setScoreboard(scoreboard);
			}
		}.runTaskTimer(MinigameManager.getPlugin(), 0, tickDelay);
		return this;
	}
	
	public ScoreboardHelper stopUpdating() {
		if(timer != null)
			timer.cancel();
		return this;
	}
	
	/**
	 * Set the lines of the scoreboard to what is specified in order
	 * 
	 * @param lines The new lines of the scoreboard
	 * 
	 * @return The helper instance
	 */
	public ScoreboardHelper setOrderedLines(String... lines) {
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
	public ScoreboardHelper setLines(String... lines) {
		clear();
		for (String str : lines)
			objective.getScore(str).setScore(0);
		return this;
	}
	
	/**
	 * Set the lines of the scoreboard to what is specified with scores
	 * 
	 * @param lines The new lines of the scoreboard
	 * 
	 * @return The helper instance
	 */
	public ScoreboardHelper setLines(Map<String, Integer> scores) {
		clear();
		for (Entry<String, Integer> e : scores.entrySet())
			objective.getScore(e.getKey()).setScore(e.getValue());
		return this;
	}
	
	/**
	 * Clear all the lines in the scorebaord
	 *  
	 * @return The helper instance
	 */
	public ScoreboardHelper clear() {
		for (String str : scoreboard.getEntries())
			scoreboard.resetScores(str);
		return this;
	}
	
}
