package me.donkeycore.minigamemanager.config;

public enum MessageType {
	
	JOIN, LEAVE, KICK, JOIN_AFTER_START, NEXT_MINIGAME, COUNTDOWN, NOT_ENOUGH_PLAYERS, FULL_ROTATION, FULL_ROTATIONS, ALREADY_IN_ROTATION, NOT_IN_ROTATION, MAPINFO, ROTATION_STOPPED, COMMAND_DISABLED, ONLY_PLAYERS, TOO_MANY_ARGUMENTS, NOT_VALID_NUMBER, NOT_VALID_ROTATION_ID, NOT_VALID_ROTATION_ID_LIST, CONFIG_RELOADED, ENABLED_MINIGAMES, STARTED_ROTATION, ALREADY_RUNNING, STOPPED_ROTATION, ALREADY_STOPPED, FORCED_ROTATION, NOT_IN_LOBBY, ROTATION_STATUS, MINIGAME_INFO, COULDNT_FIND_MINIGAME, ERROR_NEXT, SET_NEXT, AWARDED_BONUS, UPDATED_ELO, ANNOUNCE_WINNER;
	
	@Override
	public String toString() {
		return name().replace("_", "-").toLowerCase();
	}
	
}
