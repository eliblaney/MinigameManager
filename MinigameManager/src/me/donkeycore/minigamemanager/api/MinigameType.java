package me.donkeycore.minigamemanager.api;

public enum MinigameType {
	
	LAST_MAN_STANDING, TEAMS, TOWER_DEFENSE, OTHER;
	
	@Override
	public String toString() {
		// Capitalize First Letter Of Each Word
		String str = "";
		for (String s : name().split("_")) {
			if (s.length() > 1)
				str += Character.toString(s.charAt(0)).toUpperCase() + s.substring(1).toLowerCase() + " ";
			else if (s.length() > 0)
				str += s.toUpperCase() + " ";
		}
		return str.trim();
	}
	
}
