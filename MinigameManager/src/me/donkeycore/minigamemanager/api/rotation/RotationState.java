package me.donkeycore.minigamemanager.api.rotation;

public enum RotationState {
	
	LOBBY, COUNTDOWN, INGAME, STOPPED;
	
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
	
	public String toColoredString() {
		switch (this) {
			default:
				return "§6" + toString();
			case INGAME:
				return "§a" + toString();
			case STOPPED:
				return "§c" + toString();
		}
	}
	
}
