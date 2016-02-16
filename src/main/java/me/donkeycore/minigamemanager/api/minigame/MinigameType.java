package me.donkeycore.minigamemanager.api.minigame;

/**
 * Types of minigames, used for info messages
 * 
 * @author DonkeyCore
 */
public enum MinigameType {
	
	LAST_MAN_STANDING, MOST_POINTS, TEAMS, TOWER_DEFENSE, OTHER;
	
	private String desc = name();
	
	@Override
	public String toString() {
		// Capitalize First Letter Of Each Word
		String str = "";
		for (String s : desc.split("_")) {
			if (s.length() > 1)
				str += Character.toString(s.charAt(0)).toUpperCase() + s.substring(1).toLowerCase() + " ";
			else if (s.length() > 0)
				str += s.toUpperCase() + " ";
		}
		return str.trim();
	}
	
	/**
	 * Specify the minigame type if the current type is OTHER
	 * 
	 * @param desc The description of the type
	 * 
	 * @return The type itself
	 */
	public MinigameType of(String desc) {
		if(this != OTHER)
			throw new IllegalStateException("Only OTHER types can provide a description!");
		if(desc == null || desc.trim().length() < 1)
			throw new IllegalArgumentException("Insufficient description");
		this.desc = desc;
		return this;
	}
	
}
