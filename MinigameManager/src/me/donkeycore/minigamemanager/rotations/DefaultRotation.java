package me.donkeycore.minigamemanager.rotations;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.donkeycore.minigamemanager.api.Minigame;
import me.donkeycore.minigamemanager.api.Rotation;
import me.donkeycore.minigamemanager.api.RotationManager;
import me.donkeycore.minigamemanager.api.RotationState;
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
	
	public DefaultRotation(RotationManager rm, int id) {
		this.rm = rm;
		this.id = id;
	}
	
	protected void join(UUID uuid) {
		if (players.contains(uuid))
			throw new IllegalArgumentException("Player was already in rotation!");
		players.add(uuid);
		Player p = Bukkit.getPlayer(uuid);
		if (p != null)
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MinigameManager.getMinigameManager().getMinigameConfig().getMessage(MessageType.JOIN).replace("%rotation%", "" + id)));
	}
	
	protected void leave(UUID uuid) {
		if (players.contains(uuid)) {
			players.remove(uuid);
			Player p = Bukkit.getPlayer(uuid);
			if (p != null) {
				// TODO: Teleport to lobby
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', MinigameManager.getMinigameManager().getMinigameConfig().getMessage(MessageType.LEAVE)));
			}
		} else
			throw new IllegalArgumentException("Player was not in rotation!");
	}
	
	protected void kick(UUID uuid) {
		if (players.contains(uuid)) {
			players.remove(uuid);
			Player p = Bukkit.getPlayer(uuid);
			if (p != null) {
				// TODO: Teleport to lobby
				p.sendMessage(MinigameManager.getMinigameManager().getMinigameConfig().getMessage(MessageType.KICK));
			}
		} else
			throw new IllegalArgumentException("Player was not in rotation!");
	}
	
	/**
	 * Check if a player is currently in the rotation
	 * 
	 * @param uuid The UUID of the player to check
	 * @return Whether the player is in the rotation
	 */
	@Override
	public boolean hasPlayer(UUID uuid) {
		Validate.notNull(uuid);
		return players.contains(uuid);
	}
	
	/**
	 * Get all players in the rotation, both playing and not
	 * 
	 * @return A list of UUIDs of all players in the rotation
	 */
	@Override
	public List<UUID> getPlayers() {
		return players;
	}
	
	/**
	 * Check if a player is currently in-game
	 * 
	 * @param uuid The UUID of the player to check
	 * @return Whether the player is in-game
	 */
	@Override
	public boolean isInGame(UUID uuid) {
		Validate.notNull(uuid);
		return inGame.contains(uuid);
	}
	
	/**
	 * Get all currently in-game players
	 * 
	 * @return A list of UUIDs of currently in-game players
	 */
	@Override
	public List<UUID> getInGame() {
		return inGame;
	}
	
	protected void beginMinigame(Minigame minigame) {
		Validate.notNull(minigame);
		this.minigame = minigame;
		this.state = RotationState.INGAME;
		inGame.addAll(players);
	}
	
	/**
	 * Finish the current minigame and proceed the rotation to the lobby<br>
	 */
	@Override
	public void finish() {
		this.state = RotationState.LOBBY;
		if (minigame != null)
			minigame.onEnd();
		minigame = null;
		inGame.clear();
		rm.finish(id);
	}
	
	/**
	 * Announce a message to everybody in the rotation
	 * 
	 * @param message The message to announce
	 */
	@Override
	public void announce(String message) {
		Validate.notNull(message);
		for (UUID u : players)
			Bukkit.getPlayer(u).sendMessage(message);
	}
	
	/**
	 * Teleport everybody in the rotation to a certain location
	 * 
	 * @param loc The location to teleport to
	 */
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
	
	/**
	 * Get the current state of this rotation
	 * 
	 * @return The current state
	 */
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
