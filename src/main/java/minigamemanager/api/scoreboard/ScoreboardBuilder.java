package minigamemanager.api.scoreboard;

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
	
	private final ScoreboardHelper advanced;
	
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
		advanced = new ScoreboardHelper(s, o);
	}
	
	/**
	 * Get the scoreboard
	 * 
	 * @return The scoreboard
	 */
	public Scoreboard build() {
		return advanced.getScoreboard();
	}
	
	/**
	 * Get the advanced scoreboard settings
	 * 
	 * @return The advanced scoreboard settings
	 */
	public ScoreboardHelper getAdvanced() {
		return advanced;
	}
	
	/**
	 * Set the lines of the scoreboard to what is specified in order
	 * 
	 * @param lines The new lines of the scoreboard
	 * 
	 * @return The builder instance
	 */
	public ScoreboardBuilder setOrderedLines(String... lines) {
		advanced.setOrderedLines(lines);
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
		advanced.setLines(lines);
		return this;
	}
	
	/**
	 * Set the lines of the scoreboard to what is specified with scores
	 * 
	 * @param scores A map representing the new lines of the scoreboard
	 * 
	 * @return The builder instance
	 */
	public ScoreboardBuilder setLines(Map<String, Integer> scores) {
		advanced.setLines(scores);
		return this;
	}
	
	/**
	 * Clear all the lines in the scorebaord
	 * 
	 * @return The builder instance
	 */
	public ScoreboardBuilder clear() {
		advanced.clear();
		return this;
	}
	
}
