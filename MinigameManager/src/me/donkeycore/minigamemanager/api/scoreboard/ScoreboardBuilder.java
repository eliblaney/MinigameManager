package me.donkeycore.minigamemanager.api.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardBuilder {
	
	private final Scoreboard s;
	private final Objective o;
	
	public ScoreboardBuilder(String objective) {
		this(objective, objective);
	}
	
	public ScoreboardBuilder(String objective, String displayName) {
		this.s = Bukkit.getScoreboardManager().getNewScoreboard();
		s.registerNewObjective(objective, "dummy");
		this.o = s.getObjective(objective);
		o.setDisplaySlot(DisplaySlot.SIDEBAR);
		o.setDisplayName(displayName);
	}
	
	public Scoreboard build() {
		return s;
	}
	
	public ScoreboardBuilder setLines(String... lines) {
		clear();
		int n = lines.length;
		for(String str : lines)
			o.getScore(str).setScore(--n);
		return this;
	}
	
	public ScoreboardBuilder clear() {
		for(String str : s.getEntries())
			s.resetScores(str);
		return this;
	}
	
}
