package minigamemanager.api.rotation;

import org.bukkit.ChatColor;

/**
 * The state of a rotation
 * 
 * @author DonkeyCore
 */
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
	
	/**
	 * Get the name of the property with color added to it. Used for status
	 * messages.<br>
	 * <b>LOBBY, COUNTDOWN: </b>GOLD<br>
	 * <b>INGAME: </b>GREEN
	 * <b>STOPPED: </b>RED
	 * 
	 * @return The colored name of the property
	 */
	public String toColoredString() {
		switch (this) {
			default:
				return ChatColor.GOLD + toString();
			case INGAME:
				return ChatColor.GREEN + toString();
			case STOPPED:
				return ChatColor.RED + toString();
		}
	}
	
}
