package minigamemanager.api.minigame;

/**
 * Errors that could happen during a minigame
 * 
 * @author DonkeyCore
 */
public class MinigameErrors {
	
	/**
	 * No error!
	 */
	public static final int SUCCESS = 0;
	/**
	 * The minigame was interrupted by something (like a shutdown)
	 */
	public static final int INTERRUPT = 1;
	/**
	 * There was an unrecoverable exception
	 */
	public static final int EXCEPTION = 2;
	/**
	 * There weren't enough players to continue (maybe everyone quit?)
	 */
	public static final int NOT_ENOUGH_PLAYERS = 4;
	
}
