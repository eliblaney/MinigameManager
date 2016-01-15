package me.donkeycore.minigamemanager.rotations;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.donkeycore.minigamemanager.api.minigame.Minigame;
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
	
	public DefaultRotation(RotationManager rm, int id) {
		this.rm = rm;
		this.id = id;
	}
	
	protected void join(UUID uuid) {
		Player p = Bukkit.getPlayer(uuid);
		if (p != null) {
			if (players.contains(uuid))
				throw new IllegalArgumentException("Player was already in rotation!");
			players.add(uuid);
			p.teleport(MinigameManager.getMinigameManager().getMinigameLocations().getRotationLocation("lobby"));
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', MinigameManager.getMinigameManager().getMinigameConfig().getMessage(MessageType.JOIN).replace("%rotation%", "" + (id + 1))));
			if (getState() == RotationState.INGAME)
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', MinigameManager.getMinigameManager().getMinigameConfig().getMessage(MessageType.JOIN_AFTER_START).replace("%rotation%", "" + (id + 1))));
			p.setHealth(p.getMaxHealth());
			p.setFoodLevel(20);
		}
	}
	
	protected void leave(UUID uuid) {
		leave(uuid, false);
	}
	
	protected void leave(UUID uuid, boolean kicked) {
		if (players.contains(uuid)) {
			players.remove(uuid);
			Player p = Bukkit.getPlayer(uuid);
			if (p != null) {
				p.teleport(MinigameManager.getMinigameManager().getMinigameLocations().getRotationLocation("spawn"));
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', MinigameManager.getMinigameManager().getMinigameConfig().getMessage(kicked ? MessageType.KICK : MessageType.LEAVE)));
			}
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
	
	protected void beginMinigame(Minigame minigame) {
		Validate.notNull(minigame);
		this.minigame = minigame;
		setState(RotationState.INGAME);
		inGame.addAll(players);
		minigame.onStart();
	}
	
	@Override
	public void finish() {
		stop();
		resume();
	}
	
	@Override
	public void stop() {
		if (getState() == RotationState.STOPPED)
			throw new IllegalStateException("Cannot stop if already stopped!");
		setState(RotationState.STOPPED);
		if (minigame != null)
			minigame.onEnd();
		minigame = null;
		inGame.clear();
		teleportAll(MinigameManager.getMinigameManager().getMinigameLocations().getRotationLocation("lobby"));
	}
	
	@Override
	public void resume() {
		if (getState() != RotationState.STOPPED)
			throw new IllegalStateException("Cannot resume if not stopped!");
		setState(RotationState.LOBBY);
		rm.chooseMinigame(this);
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
