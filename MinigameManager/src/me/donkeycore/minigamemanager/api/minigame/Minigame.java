package me.donkeycore.minigamemanager.api.minigame;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scoreboard.Scoreboard;

import me.donkeycore.minigamemanager.api.rotation.Rotation;
import me.donkeycore.minigamemanager.api.teams.Team;
import me.donkeycore.minigamemanager.core.MinigameManager;

/**
 * The main minigame API class; all minigames should extend this class
 * 
 * @author DonkeyCore
 */
public abstract class Minigame {
	
	private final Rotation r;
	private final Scoreboard blankScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
	protected final Random random = new Random();
	
	/**
	 * Initialize the minigame
	 * 
	 * @param r The rotation that the minigame is in
	 */
	public Minigame(Rotation r) {
		this.r = r;
	}
	
	/**
	 * Get the current instance of MinigameManager.<br>
	 * <b>Warning:</b> The MinigameManager must be enabled for this to return
	 * correctly
	 * 
	 * @return The current MinigameManager instance, or null if not enabled
	 */
	public static final MinigameManager getMinigameManager() {
		return MinigameManager.getMinigameManager();
	}
	
	/**
	 * Get the rotation that this minigame is associated with
	 * 
	 * @return The rotation that the minigame is associated with
	 */
	public final Rotation getRotation() {
		return r;
	}
	
	/**
	 * Called when the minigame starts and all players have been teleported to
	 * their starting locations
	 */
	public abstract void onStart();
	
	/**
	 * Called when the minigame ends
	 * 
	 * @param error The error code (0 = no error)
	 */
	public void onEnd(int error) {}
	
	/**
	 * Call this to end the minigame and continue to the next rotation<br>
	 * <b>Note:</b> If this method is being overriden, make sure there is a call
	 * to {@link Rotation#finish()} or a call to
	 * 
	 * <pre>
	 * super.end()
	 * </pre>
	 * 
	 * @param error The error code (0 = no error)
	 */
	public void end(int error) {
		r.finish(error);
	}
	
	/**
	 * End the minigame with nothing wrong. Equivalent to {@code}end(0){@code}
	 */
	public final void end() {
		end(0);
	}
	
	/**
	 * Get the attributes belonging to this minigame
	 * 
	 * @return An instance of {@link MinigameAttributes}, or null if the
	 *         annotation is not present (bad!)
	 */
	public final MinigameAttributes getAttributes() {
		return getClass().getAnnotation(MinigameAttributes.class);
	}
	
	/**
	 * Get the minigame name as it will be displayed in chat
	 * 
	 * @return The minigame's friendly name, or "Unnamed" if not specified
	 */
	public final String getName() {
		MinigameAttributes attributes = getAttributes();
		if (attributes == null)
			return "Unnamed";
		return attributes.name();
	}
	
	/**
	 * Get the spawn location for the beginning of the minigame
	 * 
	 * @return The spawn location, can be random
	 */
	public abstract Location getStartingLocation();
	
	/**
	 * Get the players that are currently playing
	 * 
	 * @return An array of {@link Player} instances that are playing
	 */
	public Player[] getPlayers() {
		UUID[] uuids = getPlayerUUIDs();
		Player[] players = new Player[uuids.length];
		int i = 0;
		for (UUID u : uuids)
			players[i++] = Bukkit.getPlayer(u);
		return players;
	}
	
	/**
	 * Get the UUIDs of players that are currently playing
	 * 
	 * @return An array of UUIDs that represent currently playing players
	 */
	public UUID[] getPlayerUUIDs() {
		List<UUID> uuids = getRotation().getInGame();
		return uuids.toArray(new UUID[uuids.size()]);
	}
	
	/**
	 * Get the names of players that are currently playing
	 * 
	 * @return A array of player names that are playing
	 */
	public String[] getPlayerNames() {
		List<UUID> uuids = getRotation().getInGame();
		String[] players = new String[uuids.size()];
		int i = 0;
		for (UUID u : uuids)
			players[i++] = Bukkit.getPlayer(u).getName();
		return players;
	}
	
	/**
	 * Get the names of players that are currently playing colored with a
	 * certain chatcolor
	 * 
	 * @param color The ChatColor to color the names
	 * 			
	 * @return An array of strings of the player names, prefixed with
	 *         \u00a7 and the color
	 */
	public String[] getPlayerNamesWithColor(final ChatColor color) {
		String[] names = getPlayerNames();
		for (int i = 0; i < names.length; i++)
			names[i] = color + names[i];
		return names;
	}
	
	/**
	 * Teleport all players to a location
	 * 
	 * @param supplier A {@link LocationSupplier} that, when given a player,
	 *            returns the location to teleport that player.
	 */
	public void teleportAll(LocationSupplier supplier) {
		for (Player player : getPlayers())
			player.teleport(supplier.apply(player));
	}
	
	/**
	 * Teleport all players to a location
	 * 
	 * @param location A {@link Location} to teleport to
	 */
	public void teleportAll(Location location) {
		getRotation().teleportAll(location);
	}
	
	/**
	 * Give all the players an item. Equivalent to
	 * 
	 * <pre>
	 * giveAll(func, null);
	 * </pre>
	 * 
	 * @param supplier An {@link ItemStackSupplier} that, when given a player,
	 *            returns
	 *            a pair
	 *            of itemstack to give to that player and the slot (-1 for any
	 *            available slot)
	 * 			
	 * @see #giveAll(ItemStackSupplier, PlayerConsumer)
	 */
	public void giveAll(ItemStackSupplier supplier) {
		giveAll(supplier, null);
	}
	
	/**
	 * Give all the players an item
	 * 
	 * @param supplier An {@link ItemStackSupplier} that, when given a player,
	 *            returns
	 *            a pair
	 *            of itemstack to give to that player and the slot (-1 for any
	 *            available slot)
	 * @param backup An instance of {@link PlayerConsumer} that says what to do
	 *            in case the player can't receive the item (null
	 *            to just ignore)
	 */
	public void giveAll(ItemStackSupplier supplier, PlayerConsumer backup) {
		for (Player player : getPlayers()) {
			Pair<ItemStack, Integer> pair = supplier.apply(player);
			ItemStack itemstack = pair.getKey();
			int slot = pair.getValue();
			PlayerInventory inv = player.getInventory();
			if (slot == -1) {
				HashMap<Integer, ItemStack> error = inv.addItem(itemstack);
				if (error != null && !error.isEmpty())
					backup.apply(player);
			} else {
				ItemStack i = inv.getItem(slot);
				if (i == null)
					inv.setItem(slot, itemstack);
				else
					backup.apply(player);
			}
		}
	}
	
	/**
	 * Give all the players an item. Equivalent to
	 * 
	 * <pre>
	 * giveAll(itemstack, slot, null);
	 * </pre>
	 * 
	 * @param itemstack An {@link ItemStack} to give to each player
	 * 			
	 * @see #giveAll(ItemStackSupplier, PlayerConsumer)
	 */
	public void giveAll(ItemStack itemstack, int slot) {
		giveAll(itemstack, slot, null);
	}
	
	/**
	 * Give all the players an item
	 * 
	 * @param itemstack An {@link ItemStack} to give to each player
	 * @param backup An instance of {@link PlayerConsumer} that says what to do
	 *            in case the player can't receive the item (null to just
	 *            ignore)
	 */
	public void giveAll(ItemStack itemstack, int slot, PlayerConsumer backup) {
		for (Player player : getPlayers()) {
			PlayerInventory inv = player.getInventory();
			if (slot == -1) {
				HashMap<Integer, ItemStack> error = inv.addItem(itemstack);
				if (error != null && !error.isEmpty())
					backup.apply(player);
			} else {
				ItemStack i = inv.getItem(slot);
				if (i == null)
					inv.setItem(slot, itemstack);
				else
					backup.apply(player);
			}
		}
	}
	
	/**
	 * Apply some operation to all players in the minigame
	 * 
	 * @param operation The operation to apply to all players
	 */
	public void applyAll(PlayerConsumer operation) {
		for (Player player : getPlayers())
			operation.apply(player);
	}
	
	/**
	 * Clear inventories of all players in the minigame
	 */
	public void clearAll() {
		applyAll(new PlayerConsumer() {
			
			@Override
			public void apply(Player player) {
				player.getInventory().clear();
			}
		});
	}
	
	/**
	 * Heal all players' health and saturation
	 */
	public void healAll() {
		applyAll(new PlayerConsumer() {
			
			@Override
			public void apply(Player player) {
				player.setHealth(player.getMaxHealth());
				player.setFoodLevel(20);
			}
			
		});
	}
	
	/**
	 * Send a message to every in-game player
	 * 
	 * @param message The message to send to each player
	 */
	public void announce(String message) {
		for (Player player : getPlayers())
			player.sendMessage(message);
	}
	
	/**
	 * Set all players' gamemodes
	 * 
	 * @param mode the {@link GameMode} to change all players to
	 */
	public void setGamemode(final GameMode mode) {
		applyAll(new PlayerConsumer() {
			@Override
			public void apply(Player player) {
				player.setGameMode(mode);
			}
		});
	}
	
	/**
	 * Generate up to a specified amount of teams from the players in the
	 * rotation
	 * 
	 * @param amount The maximum and ideal number of teams
	 * @return An array of {@link Team} objects generated with random colors and
	 *         names that represent the color
	 */
	public Team[] generateRandomTeams(int amount) {
		Player[] players = getPlayers();
		int length = players.length;
		if (amount <= 0 || length <= 0)
			return new Team[0];
		if (amount == 1) {
			Team[] teams = new Team[1];
			for (Player player : players) {
				ChatColor color = randomChatColor();
				teams[0] = new Team.Builder().players(player).color(color).name(color.name().toLowerCase().replaceFirst(color.name().substring(0, 1).toLowerCase(), color.name().substring(0, 1).toUpperCase())).build();
			}
			return teams;
		}
		if (amount > length)
			amount /= 2;
		if (amount == length) {
			Team[] teams = new Team[amount];
			for (int i = 0; i < amount; i++) {
				ChatColor color = randomChatColor();
				teams[i] = new Team.Builder().players(players[i]).color(color).name(color.name().toLowerCase().replaceFirst(color.name().substring(0, 1).toLowerCase(), color.name().substring(0, 1).toUpperCase())).build();
			}
			return teams;
		} else {
			// get GCF/GCD, stored in x
			int w = 0;
			int x = amount;
			int y = length;
			int z = 0;
			do {
				x = amount;
				y = length - z;
				while (y > 0) {
					w = y;
					y = x % y;
					x = w;
				}
				z++;
			} while (y <= 1);
			Team[] teams = new Team[x];
			List<Player> playersLeft = new LinkedList<>(Arrays.asList(players));
			Collections.shuffle(playersLeft);
			for (int i = 1; i <= x; i++) {
				ChatColor color = randomChatColor();
				Team.Builder builder = new Team.Builder();
				// get an even amount of players per team, except the last one which gets the even amount plus any remainders
				for (int j = 0; j < (i == x ? ((length / amount) + (length % amount)) : (length / amount)); j++)
					builder.players(playersLeft.get(j));
				teams[i] = builder.color(color).name(color.name().toLowerCase().replaceFirst(color.name().substring(0, 1).toLowerCase(), color.name().substring(0, 1).toUpperCase())).build();
			}
			return teams;
		}
	}
	
	/**
	 * Set the scoreboard for all the players.<br>
	 * Equivalent to a PlayerConsumer iterating through all players and setting
	 * their scoreboards.<br>
	 * To do this for a single player, just use
	 * {@code}player.setScoreboard(scoreboard);{@code}
	 * 
	 * @param scoreboard The scoreboard to set for all players
	 */
	public void setScoreboard(final Scoreboard scoreboard) {
		applyAll(new PlayerConsumer() {
			
			@Override
			public void apply(Player player) {
				player.setScoreboard(scoreboard);
			}
			
		});
	}
	
	/**
	 * Generate a random chat color
	 * 
	 * @return A random {@link ChatColor}
	 */
	public ChatColor randomChatColor() {
		ChatColor[] c = ChatColor.values();
		return c[random.nextInt(c.length)];
	}
	
	/**
	 * Generate a color with random RGB values
	 * 
	 * @return A random {@link Color}
	 */
	public Color randomColor() {
		return Color.fromRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255));
	}
	
	/**
	 * Clear the scoreboard for a player
	 * 
	 * @param player The player to clear the scoreboard of
	 */
	public void clearScoreboard(Player player) {
		player.setScoreboard(blankScoreboard);
	}
	
	/**
	 * Supplies a location for every player given
	 */
	public static interface LocationSupplier extends Function<Player, Location> {}
	
	/**
	 * Supplies an itemstack and a slot to place the itemstack for every player
	 * given
	 */
	public static interface ItemStackSupplier extends Function<Player, Pair<ItemStack, Integer>> {}
	
	/**
	 * Performs some operation for every player given
	 */
	public static interface PlayerConsumer {
		
		/**
		 * Perform an operation on a player
		 * 
		 * @param player The {@link Player} to perform an operation on
		 */
		public void apply(Player player);
		
	}
	
	/**
	 * Represents a function; very useful for Java 8 plugins
	 *
	 * @param <T> Type to be passed in
	 * @param <R> Type to be returned
	 */
	public static interface Function<T, R> {
		
		/**
		 * Apply T and get some value back
		 * 
		 * @param t The object to apply
		 * 			
		 * @return Some instance of R
		 */
		public R apply(T t);
		
	}
	
	/**
	 * Represents a function that accepts multiple parameter types
	 *
	 * @param <T> Type to be passed in
	 * @param <R> Type to be returned
	 */
	public static interface BulkFunction<T, R> {
		
		/**
		 * Apply an array of T and get some values back
		 * 
		 * @param ts The objects to apply
		 * 			
		 * @return An array of instances of R
		 */
		public R[] apply(T[] ts);
		
	}
	
}
