package me.donkeycore.minigamemanager.api.scoreboard;

import java.util.Map;
import java.util.Map.Entry;

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
	
	/**
	 * The scoreboard object being manipulated
	 */
	private final Scoreboard s;
	/**
	 * The objective to use
	 */
	private final Objective o;
	
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
		this.s = Bukkit.getScoreboardManager().getNewScoreboard();
		s.registerNewObjective(objective, "dummy");
		this.o = s.getObjective(objective);
		o.setDisplaySlot(DisplaySlot.SIDEBAR);
		o.setDisplayName(displayName);
	}
	
	/**
	 * Get the scoreboard
	 * 
	 * @return The scoreboard
	 */
	public Scoreboard build() {
		return s;
	}
	
	/**
	 * Set the lines of the scoreboard to what is specified in order
	 * 
	 * @param lines The new lines of the scoreboard
	 * 
	 * @return The builder instance
	 */
	public ScoreboardBuilder setOrderedLines(String... lines) {
		clear();
		int n = lines.length;
		for (String str : lines)
			o.getScore(str).setScore(--n);
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
		clear();
		for (String str : lines)
			o.getScore(str).setScore(0);
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
		clear();
		for (Entry<String, Integer> e : scores.entrySet())
			o.getScore(e.getKey()).setScore(e.getValue());
		return this;
	}
	
	/**
	 * Clear all the lines in the scorebaord
	 *  
	 * @return The builder instance
	 */
	public ScoreboardBuilder clear() {
		for (String str : s.getEntries())
			s.resetScores(str);
		return this;
	}
	
}
