package me.donkeycore.minigamemanager.api.scoreboard;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

/**
 * Used to build a custom Bukkit scoreboard
 * 
 * @author DonkeyCore
 */
public class ScoreboardBuilder {
	
	private final ScoreboardHelper helper;
	
	/**
	 * Create an instance of the scoreboard builder
	 * 
	 * @param objective The objective name to use
	 */
	public ScoreboardBuilder(String objective) {
		this(objective, objective);
	}
	
	/**
	 * Create an instance of the scoreboard builder
	 * 
	 * @param objective The objective name to use
	 * @param displayName The text to appear above all scores on the sidebar
	 */
	public ScoreboardBuilder(String objective, String displayName) {
		Scoreboard s = Bukkit.getScoreboardManager().getNewScoreboard();
		s.registerNewObjective(objective, "dummy");
		Objective o = s.getObjective(objective);
		o.setDisplaySlot(DisplaySlot.SIDEBAR);
		o.setDisplayName(displayName);
		helper = new ScoreboardHelper(s, o);
	}
	
	/**
	 * Get the scoreboard
	 * 
	 * @return The scoreboard
	 */
	public Scoreboard build() {
		return helper.getScoreboard();
	}
	
	/**
	 * Get the scoreboard helper being used to create the scoreboard
	 * 
	 * @return The scoreboard helper
	 */
	public ScoreboardHelper getHelper() {
		return helper;
	}
	
	/**
	 * Set the lines of the scoreboard to what is specified in order
	 * 
	 * @param lines The new lines of the scoreboard
	 * 
	 * @return The builder instance
	 */
	public ScoreboardBuilder setOrderedLines(String... lines) {
		helper.setOrderedLines(lines);
		return this;
	}
	
	/**
	 * Set the lines of the scoreboard to what is specified in any order
	 * 
	 * @param lines The new lines of the scoreboard
	 * 
	 * @return The builder instance
	 */
	public ScoreboardBuilder setLines(String... lines) {
		helper.setLines(lines);
		return this;
	}
	
	/**
	 * Set the lines of the scoreboard to what is specified with scores
	 * 
	 * @param lines The new lines of the scoreboard
	 * 
	 * @return The builder instance
	 */
	public ScoreboardBuilder setLines(Map<String, Integer> scores) {
		helper.setLines(scores);
		return this;
	}
	
	/**
	 * Clear all the lines in the scorebaord
	 *  
	 * @return The builder instance
	 */
	public ScoreboardBuilder clear() {
		helper.clear();
		return this;
	}
	
}
