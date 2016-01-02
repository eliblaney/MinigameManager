package me.donkeycore.minigamemanager.config;

public enum MessageType {
	
	JOIN, LEAVE, KICK, NEXT_MINIGAME, COUNTDOWN, NOT_ENOUGH_PLAYERS, FULL_ROTATION, FULL_ROTATIONS, NOT_IN_ROTATION, MAPINFO;
	
	@Override
	public String toString() {
		return name().replace("_", "-").toLowerCase();
	}
	
}
