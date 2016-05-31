package minigamemanager.rotations;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import minigamemanager.api.minigame.Minigame;
import minigamemanager.api.minigame.MinigameErrors;
import minigamemanager.api.rotation.Rotation;
import minigamemanager.api.rotation.RotationManager;
import minigamemanager.api.rotation.RotationState;
import minigamemanager.config.MessageType;
import minigamemanager.core.MinigameManager;
import minigamemanager.events.rotation.RotationJoinEvent;
import minigamemanager.events.rotation.RotationLeaveEvent;

/**
 * The default rotation manager to handle all rotations
 * 
 * @author DonkeyCore
 */
public final class DefaultRotationManager implements RotationManager {
	
	/**
	 * The parent MinigameManager instance
	 */
	private final MinigameManager manager;
	/**
	 * The list of rotations belonging to this rotation manager
	 */
	private final List<DefaultRotation> rotations = new ArrayList<>();
	/**
	 * The list of players playing, and their respective rotation
	 */
	private final Map<UUID, DefaultRotation> players = new HashMap<>();
	/**
	 * Whether rotations should continue to run
	 */
	private boolean running = true;
	/**
	 * Whether to force the countdown to start
	 */
	private boolean force = false;
	/**
	 * The next minigame to be played if told
	 */
	private Map<Integer, Class<? extends Minigame>> next = new HashMap<>();
	
	/**
	 * Create a new default rotation manager
	 * 
	 * @param manager An instance of MinigameManager that owns this rotation
	 *            manager
	 * @param rotations The number of rotations to create
	 */
	public DefaultRotationManager(MinigameManager manager, int rotations) {
		this.manager = manager;
		for (int i = 0; i < rotations; i++)
			this.rotations.add(new DefaultRotation(this, i));
	}
	
	@Override
	public boolean join(Player player) {
		Validate.notNull(player);
		int id = findAvailableRotation();
		if (id < 0)
			return false;
		DefaultRotation r = rotations.get(id);
		r.join(player.getUniqueId());
		players.put(player.getUniqueId(), r);
		Bukkit.getPluginManager().callEvent(new RotationJoinEvent(r, player));
		if (r.getState() == RotationState.LOBBY && r.getPlayers().size() >= manager.getMinigameSettings().getMinimumPlayers())
			start(r);
		r.setLobbyScoreboard();
		return true;
	}
	
	// find lobbies to join
	private int findAvailableRotation() {
		int maxPlayers = manager.getMinigameSettings().getMaximumPlayers();
		for (int i = 0; i < rotations.size(); i++) {
			DefaultRotation r = rotations.get(i);
			// skip if reached max players
			if (r.getPlayers().size() >= maxPlayers)
				continue;
			// prefer ready lobbies rather than already started ones
			if (r.getState() != RotationState.INGAME)
				continue;
			return i;
		}
		// lower standards and try again
		for (int i = 0; i < rotations.size(); i++) {
			DefaultRotation r = rotations.get(i);
			// still can't join lobby if bigger than max size
			if (r.getPlayers().size() >= maxPlayers)
				continue;
			return i;
		}
		// :(
		return -1;
	}
	
	@Override
	public boolean join(Player player, int id) {
		Validate.notNull(player, "Player cannot be null!");
		Validate.isTrue(id >= 0 && id < rotations.size(), id + " is not a valid rotation ID! Current number of rotations: " + rotations.size());
		DefaultRotation r = rotations.get(id);
		int maxPlayers = manager.getMinigameSettings().getMaximumPlayers();
		if (r.getPlayers().size() >= maxPlayers)
			return false;
		r.join(player.getUniqueId());
		players.put(player.getUniqueId(), r);
		Bukkit.getPluginManager().callEvent(new RotationJoinEvent(r, player));
		if (r.getState() == RotationState.LOBBY && r.getPlayers().size() >= manager.getMinigameSettings().getMinimumPlayers())
			start(r);
		return true;
	}
	
	@Override
	public boolean leave(Player player, boolean kicked) {
		Validate.notNull(player, "Player cannot be null!");
		UUID uuid = player.getUniqueId();
		for (DefaultRotation r : rotations) {
			if (r.hasPlayer(uuid)) {
				r.leave(uuid, kicked);
				players.remove(uuid);
				Bukkit.getPluginManager().callEvent(new RotationLeaveEvent(r, player));
				return true;
			}
		}
		return false;
	}
	
	@Override
	public Rotation[] getRotations() {
		return rotations.toArray(new Rotation[rotations.size()]);
	}
	
	@Override
	public Rotation getRotation(int id) {
		Validate.isTrue(id >= 0 && id < rotations.size(), id + " is not a valid rotation ID! Current number of rotations: " + rotations.size());
		return rotations.get(id);
	}
	
	@Override
	public Rotation getRotation(Player player) {
		Validate.notNull(player, "Player cannot be null!");
		return getRotation(player.getUniqueId());
	}
	
	public Rotation getRotation(UUID player) {
		Validate.notNull(player, "Player UUID cannot be null!");
		return players.get(player);
	}
	
	@Override
	public void start(Rotation rotation) {
		Validate.notNull(rotation, "The rotation cannot be null!");
		Validate.isTrue(rotation instanceof DefaultRotation, "Rotation type " + rotation.getClass().getSimpleName() + " is invalid for " + getClass().getSimpleName());
		final DefaultRotation r = (DefaultRotation) rotation;
		// Don't want to do anything if shutting down
		if (!running)
			return;
		// Don't run if already playing
		if (rotation.getState() != RotationState.LOBBY)
			return;
		final DefaultRotationManager rm = this;
		Minigame minigame = null;
		if (next.containsKey(r.getId())) {
			try {
				// Attempt to create a new instance of the minigame
				minigame = next.get(r.getId()).getConstructor(Rotation.class).newInstance(r);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {}
			next = null;
		} else {
			int tries = 10;
			// Try 10 times to get a random minigame before giving up
			do
				minigame = getRandomMinigame(r, r.getLastMinigame());
			while (--tries > 0 && minigame == null);
		}
		if (minigame == null) {
			// No minigame found most likely because not enough players
			r.announce(manager.getMessages().getMessage(MessageType.NOT_ENOUGH_PLAYERS));
			// Wait for more players to join
		} else {
			r.setState(RotationState.COUNTDOWN);
			// Announce next minigame
			r.announce(ChatColor.translateAlternateColorCodes('&', manager.getMessages().getMessage(MessageType.NEXT_MINIGAME).replace("%minigame%", minigame.getName().replace("_", " "))));
			// Tell the rotation about the new minigame
			r.setMinigame(minigame);
			// Async countdown timer
			Countdown countdown = new Countdown(manager, rm, r, minigame, force);
			force = false;
			BukkitTask bt = Bukkit.getScheduler().runTaskTimer(MinigameManager.getPlugin(), countdown, 20L, 20L);
			countdown.setTask(bt);
		}
	}
	
	/**
	 * Start the next minigame
	 * 
	 * @param r The rotation to be affected
	 * @param minigame The minigame to start
	 */
	void start(DefaultRotation r, Minigame minigame) {
		Validate.notNull(r, "Rotation cannot be null!");
		Validate.notNull(minigame, "Minigame cannot be null!");
		// Set the rotation minigame and copy current players to another list OR end the process if failed
		if (!r.beginMinigame(minigame)) {
			r.announce(manager.getMessages().getMessage(MessageType.NOT_ENOUGH_PLAYERS));
			r.setState(RotationState.LOBBY);
			r.teleportAll(manager.getDefaultMinigameLocations().getRotationLocation("lobby"));
			start(r);
		}
		// Teleport everybody to possibly random spawns and optionally send them a mapinfo message 
		for (UUID u : r.getPlayers()) {
			Player player = Bukkit.getPlayer(u);
			if (player == null)
				r.leave(u);
			else
				player.teleport(minigame.getStartingLocation(player));
		}
		minigame.mapinfo();
		minigame.onStart();
	}
	
	@Override
	public void force(Rotation r) {
		if (r.getState() != RotationState.LOBBY)
			return;
		force = true;
		start(r);
	}
	
	@Override
	public void shutdown() {
		this.running = false;
		for (DefaultRotation r : rotations) {
			if (r.getState() != RotationState.STOPPED)
				r.finish(MinigameErrors.INTERRUPT, null);
			// Avoid ConcurrentModificationExcpetion
			UUID[] uuids = r.getPlayers().toArray(new UUID[r.getPlayers().size()]);
			for (UUID uuid : uuids)
				r.leave(uuid, true);
		}
	}
	
	@Override
	public void setNext(int id, Class<? extends Minigame> clazz) {
		Validate.notNull(clazz, "Minigame cannot be null!");
		Validate.isTrue(id >= 0 && id < rotations.size(), id + " is not a valid rotation ID! Current number of rotations: " + rotations.size());
		this.next.put(id, clazz);
	}
	
	@SuppressWarnings("unchecked")
	private Minigame getRandomMinigame(Rotation r, Class<? extends Minigame> last) {
		Set<Class<? extends Minigame>> m = getMinigamesWithMinimum(r.getPlayers().size());
		// no minigames!
		if (m.size() < 1)
			return null;
		// remove the last minigame if possible to avoid repeated minigames
		if (last != null) {
			for (Class<? extends Minigame> c : m.toArray(new Class[m.size()])) {
				if (last.getName().equals(c.getName()))
					m.remove(c);
			}
		}
		Class<? extends Minigame> clazz;
		if (m.size() < 1)
			// there's only 1 minigame, and that was just played... guess we're going to play it again
			clazz = last;
		else
			// Get random minigame
			clazz = (Class<? extends Minigame>) m.toArray()[new Random().nextInt(m.size())];
		try {
			// Attempt to create a new instance of the minigame
			return clazz.getConstructor(Rotation.class).newInstance(r);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	private Set<Class<? extends Minigame>> getMinigamesWithMinimum(int currentPlayers) {
		Set<Class<? extends Minigame>> validMinigames = new HashSet<>();
		// Cycle through the minigames with their corresponding minimums
		for (Entry<Class<? extends Minigame>, Integer> e : manager.getMinigamesWithMinimums().entrySet()) {
			// If the current players are sufficient for the minigame, add it to the valid minigames
			if (force || currentPlayers >= e.getValue())
				validMinigames.add(e.getKey());
		}
		return validMinigames;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((players == null) ? 0 : players.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultRotationManager other = (DefaultRotationManager) obj;
		if (players == null) {
			if (other.players != null)
				return false;
		} else if (!players.equals(other.players))
			return false;
		return true;
	}
	
}
