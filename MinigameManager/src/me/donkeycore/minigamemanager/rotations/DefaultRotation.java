package me.donkeycore.minigamemanager.rotations;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.donkeycore.minigamemanager.api.minigame.Minigame;
import me.donkeycore.minigamemanager.api.minigame.MinigameErrors;
import me.donkeycore.minigamemanager.api.rotation.Rotation;
import me.donkeycore.minigamemanager.api.rotation.RotationManager;
import me.donkeycore.minigamemanager.api.rotation.RotationState;
import me.donkeycore.minigamemanager.config.MessageType;
import me.donkeycore.minigamemanager.core.MinigameManager;

public final class DefaultRotation implements Rotation {
	
	// All players in the rotation, both playing and not
	private final List<UUID> players = new ArrayList<>();
	// Separate array for those in-game to separate newly joining from currently playing
	private final List<UUID> inGame = new ArrayList<>();
	private final RotationManager rm;
	private final int id;
	private Minigame minigame = null;
	private RotationState state = RotationState.LOBBY;
	private final org.bukkit.scoreboard.Scoreboard blankScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
	
	public DefaultRotation(RotationManager rm, int id) {
		this.rm = rm;
		this.id = id;
	}
	
	protected void join(UUID uuid) {
		Player p = Bukkit.getPlayer(uuid);
		if (p != null) {
			// only add if they're not already in there
			if (players.contains(uuid))
				throw new IllegalArgumentException("Player was already in rotation!");
			// add them and teleport to lobby with a welcoming message
			players.add(uuid);
			p.teleport(MinigameManager.getMinigameManager().getMinigameLocations().getRotationLocation("lobby"));
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MinigameManager.getMinigameManager().getMinigameConfig().getMessage(MessageType.JOIN).replace("%rotation%", "" + (id + 1))));
			// send a sorry message if the rotation is in-game
			if (getState() == RotationState.INGAME)
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', MinigameManager.getMinigameManager().getMinigameConfig().getMessage(MessageType.JOIN_AFTER_START).replace("%rotation%", "" + (id + 1))));
			// heal them
			p.setHealth(p.getMaxHealth());
			p.setFoodLevel(20);
		}
	}
	
	protected void leave(UUID uuid) {
		leave(uuid, false);
	}
	
	protected void leave(UUID uuid, boolean kicked) {
		// only let them leave if they're already in there
		if (players.contains(uuid)) {
			players.remove(uuid);
			if (inGame.contains(uuid))
				inGame.remove(uuid);
			// clear the scoreboard, teleport to spawn, and send a message
			Player p = Bukkit.getPlayer(uuid);
			if (p != null) {
				p.setScoreboard(blankScoreboard);
				p.teleport(MinigameManager.getMinigameManager().getMinigameLocations().getRotationLocation("spawn"));
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', MinigameManager.getMinigameManager().getMinigameConfig().getMessage(kicked ? MessageType.KICK : MessageType.LEAVE)));
			}
			// sad, sad times
			if (inGame.size() < 2)
				minigame.end(MinigameErrors.NOT_ENOUGH_PLAYERS);
		} else
			throw new IllegalArgumentException("Player was not in rotation!");
	}
	
	@Override
	public boolean hasPlayer(UUID uuid) {
		Validate.notNull(uuid);
		return players.contains(uuid);
	}
	
	@Override
	public List<UUID> getPlayers() {
		return players;
	}
	
	@Override
	public boolean isInGame(UUID uuid) {
		Validate.notNull(uuid);
		return inGame.contains(uuid);
	}
	
	@Override
	public List<UUID> getInGame() {
		return inGame;
	}
	
	protected boolean beginMinigame(Minigame minigame) {
		Validate.notNull(minigame);
		// players must always be at least 1 for testing, and at least 2 for releases
		if (getInGame().size() < 1 || (getInGame().size() < 2 && MinigameManager.isRelease()))
			return false;
		this.minigame = minigame;
		// set state, add all players to ingame list, set default gamemode, and start the fun!
		setState(RotationState.INGAME);
		inGame.addAll(players);
		for (UUID player : getInGame())
			Bukkit.getPlayer(player).setGameMode(GameMode.ADVENTURE);
		minigame.onStart();
		return true;
	}
	
	@Override
	public void finish(int error) {
		// stop everything with an optional error, then restart the countdown
		stop(error);
		resume();
	}
	
	@Override
	public void stop(int error) {
		// don't stop several times in a row
		if (getState() == RotationState.STOPPED)
			throw new IllegalStateException("Cannot stop if already stopped!");
		setState(RotationState.STOPPED);
		// stop any minigames if they're going
		if (minigame != null) {
			minigame.onEnd(error);
			minigame = null;
		}
		// clear scoreboards, clear ingame list, and teleport everybody to the lobby
		for (UUID u : inGame)
			Bukkit.getPlayer(u).setScoreboard(blankScoreboard);
		inGame.clear();
		teleportAll(MinigameManager.getMinigameManager().getMinigameLocations().getRotationLocation("lobby"));
	}
	
	@Override
	public void resume() {
		// only resume if stopped
		if (getState() != RotationState.STOPPED)
			throw new IllegalStateException("Cannot resume if not stopped!");
		// set state to lobby and start the countdown via RotationManager implementation
		setState(RotationState.LOBBY);
		rm.start(this);
	}
	
	@Override
	public void announce(String message) {
		Validate.notNull(message);
		for (UUID u : players)
			Bukkit.getPlayer(u).sendMessage(message);
	}
	
	@Override
	public void teleportAll(Location loc) {
		Validate.notNull(loc);
		for (UUID u : getPlayers()) {
			Player player = Bukkit.getPlayer(u);
			if (player == null)
				leave(u);
			else
				player.teleport(loc);
		}
	}
	
	@Override
	public RotationState getState() {
		return state;
	}
	
	protected void setState(RotationState state) {
		this.state = state;
	}
	
	@Override
	public Minigame getCurrentMinigame() {
		return minigame;
	}
	
}
