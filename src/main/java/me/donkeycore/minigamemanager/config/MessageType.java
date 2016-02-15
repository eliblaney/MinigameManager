package me.donkeycore.minigamemanager.config;

public enum MessageType {
	
	JOIN, LEAVE, KICK, JOIN_AFTER_START, NEXT_MINIGAME, COUNTDOWN, NOT_ENOUGH_PLAYERS, FULL_ROTATION, FULL_ROTATIONS, ALREADY_IN_ROTATION, NOT_IN_ROTATION, MAPINFO, ROTATION_STOPPED;
	
	@Override
	public String toString() {
		return name().replace("_", "-").toLowerCase();
	}
	
}
