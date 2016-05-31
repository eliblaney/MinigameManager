package minigamemanager.api.minigame;

import static minigamemanager.api.nms.ReflectionAPI.getNMSClass;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Scoreboard;

import minigamemanager.api.rotation.Rotation;
import minigamemanager.api.teams.Team;
import minigamemanager.api.winner.WinnerList;
import minigamemanager.config.MessageType;
import minigamemanager.core.MinigameManager;

/**
 * The main minigame API class; all minigames should extend this class
 * 
 * @author DonkeyCore
 */
public abstract class Minigame {
	
	/**
	 * Array of spawn locations that players can appear at
	 */
	protected Location[] spawns;
	/**
	 * The parent rotation of this minigame
	 */
	private final Rotation r;
	/**
	 * A blank scoreboard for clearing
	 */
	private final Scoreboard blankScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
	/**
	 * Helpful random object
	 */
	protected final Random random = new Random();
	/**
	 * List of players who are alive
	 */
	protected final List<UUID> alive;
	/**
	 * Mapinfo variables
	 */
	private final String mapName, mapAuthors;
	/**
	 * Bonuses to award players for reasons other than winning
	 */
	private final List<Bonus> bonuses = new ArrayList<>();
	
	/**
	 * Initialize the minigame using a helper MapInfo class
	 * 
	 * @param r The rotation that the minigame is in
	 * @param map The MapInfo class, containing the name of the map in the
	 *            config, the map's display name, and authors
	 */
	public Minigame(Rotation r, MapInfo map) {
		this(r, map.getConfigName(), map.getName(), map.getAuthor());
	}
	
	/**
	 * Initialize the minigame
	 * 
	 * @param r The rotation that the minigame is in
	 * @param map The map that the players will be playing in
	 * @param mapName The name of the map to show in mapinfo
	 * @param mapAuthors The authors of the map
	 */
	public Minigame(Rotation r, String map, String mapName, String mapAuthors) {
		this.r = r;
		this.alive = new LinkedList<>();
		// set default spawn locations
		if (isDefault())
			this.spawns = getMinigameManager().getDefaultMinigameLocations().getMinigameSpawns(getName(), map);
		else
			this.spawns = getMinigameManager().getMinigameConfig(getClass()).getMinigameSpawns(map);
		this.mapName = mapName;
		this.mapAuthors = mapAuthors;
	}
	
	/**
	 * Determine if this minigame is a default minigame
	 * 
	 * @return Whether the minigame is a default minigame
	 */
	public boolean isDefault() {
		return getAttributes().isDefault();
	}
	
	/**
	 * Send all players the mapinfo, if it exists
	 */
	public final boolean mapinfo() {
		if (mapName != null && mapAuthors != null) {
			announce(getMinigameManager().getMessages().getMessage(MessageType.MAPINFO).replace("%name%", mapName).replace("%author%", mapAuthors));
			return true;
		} else
			return false;
	}
	
	/**
	 * Get a random map for the specified minigame
	 * 
	 * @param minigame The minigame to get a random map for
	 * 
	 * @return An instance of MapInfo representing the map
	 */
	public static MapInfo randomMap(Class<? extends Minigame> minigame) {
		MinigameAttributes attr = minigame.getAnnotation(MinigameAttributes.class);
		ConfigurationSection s = null;
		if (attr.isDefault())
			s = getMinigameManager().getDefaultMinigameLocations().getConfig().getConfigurationSection("default-minigames").getConfigurationSection(attr.name());
		else
			s = getMinigameManager().getMinigameConfig(minigame).getConfig();
		Set<String> maps = s.getKeys(false);
		String map = maps.toArray(new String[maps.size()])[new Random().nextInt(maps.size())];
		ConfigurationSection mapinfo = s.getConfigurationSection(map).getConfigurationSection("mapinfo");
		return new MapInfo(map, mapinfo.getString("name"), mapinfo.getString("author"));
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
	 * Get the id of the rotation containing this minigame
	 * 
	 * @return The id of the rotation
	 */
	public int getId() {
		return r.getId();
	}
	
	/**
	 * Called when the minigame starts and all players have been teleported to
	 * their starting locations. This is called after all players are teleported
	 * to their spawns
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
	 * to {@link Rotation#finish(int, WinnerList)} or a call to
	 * 
	 * <pre>
	 * super.end()
	 * </pre>
	 * 
	 * @param error The error code (0 = no error)
	 * @param winners The winners of the minigame
	 */
	public void end(int error, WinnerList winners) {
		r.finish(error, winners);
	}
	
	/**
	 * End the minigame with nothing wrong. Equivalent to {@code}end(0){@code}
	 * 
	 * @param winners The winners of the minigame
	 */
	public final void end(WinnerList winners) {
		end(0, winners);
	}
	
	/**
	 * Register a bonus to be awarded to a player
	 * 
	 * @param uuid UUID of the player to award
	 * @param bonus Amount to award
	 * @param reason Reason for awarding the currency
	 */
	public final void awardBonus(UUID uuid, int bonus, String reason) {
		Validate.notNull(uuid, "UUID cannot be null");
		Validate.isTrue(bonus > 0, "Bonus must be positive");
		Validate.notEmpty(reason, "There must be a reason for awarding bonuses");
		bonuses.add(new Bonus(uuid, bonus, reason));
	}
	
	/**
	 * Get the list of bonuses that will be awarded to players
	 * 
	 * @return The list of bonuses
	 */
	public final List<Bonus> getBonuses() {
		return bonuses;
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
	 * Get the spawn location for the beginning of the minigame for a player
	 * 
	 * @param player The player being teleported
	 * 
	 * @return The spawn location, can be random
	 */
	public abstract Location getStartingLocation(Player player);
	
	/**
	 * Determine whether the specified player is playing in the minigame
	 * 
	 * @param player The player to check
	 * 
	 * @return Whether the player is playing in this minigame
	 */
	public boolean isPlaying(Player player) {
		return r.getInGame().contains(player.getUniqueId());
	}
	
	/**
	 * Determine whether the specified player is playing in the minigame
	 * 
	 * @param player The player to check
	 * 
	 * @return Whether the player is playing in this minigame
	 */
	public boolean isPlaying(UUID player) {
		return r.getInGame().contains(player);
	}
	
	/**
	 * Get a player from their name
	 * 
	 * @param name The name of the player
	 * @return The player object
	 */
	public Player getPlayer(String name) {
		for (Player player : getPlayers()) {
			if (player.getName().equals(name))
				return player;
		}
		return null;
	}
	
	/**
	 * Get a player from their UUID
	 * 
	 * @param uuid The UUID of the player
	 * 
	 * @return The player object
	 */
	public Player getPlayer(UUID uuid) {
		for (Player player : getPlayers()) {
			if (player.getUniqueId().equals(uuid))
				return player;
		}
		return null;
	}
	
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
	 * Get the number of players that are currently playing
	 * 
	 * @return The number of currently playing players
	 */
	public int getPlayerAmount() {
		return getRotation().getInGame().size();
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
	
	public boolean setAlive(Player player, boolean alive) {
		UUID u = player.getUniqueId();
		if (this.alive.contains(u) && alive)
			return false;
		if (!this.alive.contains(u) && !alive)
			return false;
		if (alive) {
			player.setGameMode(GameMode.ADVENTURE);
			this.alive.add(u);
		} else {
			player.setGameMode(GameMode.SPECTATOR);
			this.alive.remove(u);
		}
		return true;
	}
	
	/**
	 * Determine whether the specified player is alive
	 * 
	 * @param player THe player to check
	 * 
	 * @return Whether the player is marked as alive
	 */
	public boolean isAlive(Player player) {
		return isPlaying(player) && alive.contains(player.getUniqueId());
	}
	
	/**
	 * Determine whether the specified player is alive
	 * 
	 * @param player THe player to check
	 * 
	 * @return Whether the player is marked as alive
	 */
	public boolean isAlive(UUID player) {
		return isPlaying(player) && alive.contains(player);
	}
	
	/**
	 * Get the players that are currently alive
	 * 
	 * @return An array of {@link Player} instances that are alive
	 */
	public Player[] getAlive() {
		UUID[] uuids = getAliveUUIDs();
		Player[] players = new Player[uuids.length];
		int i = 0;
		for (UUID u : uuids)
			players[i++] = Bukkit.getPlayer(u);
		return players;
	}
	
	/**
	 * Get the UUIDs of players that are currently alive
	 * 
	 * @return An array of UUIDs that represent currently alive players
	 */
	public UUID[] getAliveUUIDs() {
		return alive.toArray(new UUID[alive.size()]);
	}
	
	/**
	 * Get the number of players that are currently alive
	 * 
	 * @return The number of currently alive players
	 */
	public int getAliveAmount() {
		return alive.size();
	}
	
	/**
	 * Get the names of players that are currently alive
	 * 
	 * @return A array of player names that are alive
	 */
	public String[] getAliveNames() {
		String[] players = new String[alive.size()];
		int i = 0;
		for (UUID u : alive)
			players[i++] = Bukkit.getPlayer(u).getName();
		return players;
	}
	
	/**
	 * Get the names of players that are currently alive colored with a
	 * certain chatcolor
	 * 
	 * @param color The ChatColor to color the names
	 * 
	 * @return An array of strings of the player names, prefixed with
	 *         \u00a7 and the color
	 */
	public String[] getAliveNamesWithColor(final ChatColor color) {
		String[] names = getAliveNames();
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
	 * Set armor for all players
	 * 
	 * @param helmet The helmet for each player
	 * @param chestplate The chestplate for each player
	 * @param leggings The leggings for each player
	 * @param boots The boots for each player
	 */
	public void armorAll(ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots) {
		for (Player player : getPlayers()) {
			PlayerInventory i = player.getInventory();
			i.setHelmet(helmet);
			i.setChestplate(chestplate);
			i.setLeggings(leggings);
			i.setBoots(boots);
		}
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
			Tuple<ItemStack, Integer> tuple = supplier.apply(player);
			ItemStack itemstack = tuple.getLeft();
			int slot = tuple.getRight();
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
	 * @param slot The slot number to put the item in
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
	 * @param slot The slot number to put the item in
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
	 * Apply a potion effect to all players
	 * 
	 * @param effect The potion effect to apply
	 */
	public void potionAll(final PotionEffect effect) {
		applyAll(new PlayerConsumer() {
			
			@Override
			public void apply(Player player) {
				player.addPotionEffect(effect);
			}
			
		});
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
	 * Set scoreboards that can change per player
	 * 
	 * @param scoreboards The function to supply scoreboards
	 */
	public void setScoreboard(Function<Player, Scoreboard> scoreboards) {
		for (Player player : getAlive())
			player.setScoreboard(scoreboards.apply(player));
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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((r == null) ? 0 : r.hashCode());
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
		Minigame other = (Minigame) obj;
		if (r == null) {
			if (other.r != null)
				return false;
		} else if (!r.equals(other.r))
			return false;
		return true;
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
	 * Display an action bar message to a player
	 * 
	 * @param player The player to display the message to
	 * @param message The message to send
	 * 
	 * @return Whether the function finished successfully
	 */
	public boolean sendActionBarMessage(Player player, String message) {
		try {
			Object chatSerializer = getNMSClass("IChatBaseComponent$ChatSerializer").getMethod("a", String.class).invoke(null, "{\"text\":\"" + message + "\"}");
			Object playChat = getNMSClass("PacketPlayOutChat").getConstructor(getNMSClass("IChatBaseComponent"), Byte.TYPE).newInstance(chatSerializer, (byte) 2);
			Object handle = player.getClass().getMethod("getHandle", (Class<?>[]) new Class[0]).invoke(player, new Object[0]);
			Object connection = handle.getClass().getField("playerConnection").get(handle);
			connection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(connection, playChat);
			return true;
		} catch (InvocationTargetException | IllegalAccessException | InstantiationException | NoSuchMethodException | NoSuchFieldException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Display a title with an optional subtitle to a player
	 * 
	 * @param player The player to display the message to
	 * @param title The title to send
	 * @param subtitle The subtitle to send
	 * @param fadeIn The amount of time in ticks it takes to fade in
	 * @param stay The amount of time in ticks the message stays
	 * @param fadeOut The amount of time in ticks it takes to fade out
	 * 
	 * @return Whether the function finished successfully
	 */
	public boolean sendTitleMessage(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
		try {
			Method chatSerializerA = getNMSClass("IChatBaseComponent$ChatSerializer").getMethod("a", String.class);
			Class<?> enumTitleAction = getNMSClass("PacketPlayOutTitle$EnumTitleAction");
			Object chatSerializerTitle = chatSerializerA.invoke(null, "{\"text\":\"" + title + "\"}");
			Object chatSerializerSubtitle = chatSerializerA.invoke(null, "{\"text\":\"" + subtitle + "\"}");
			Object enumTitle = enumTitleAction.getField("TITLE").get(null);
			Object enumSubtitle = enumTitleAction.getField("SUBTITLE").get(null);
			Object playTitle = getNMSClass("PacketPlayOutTitle").getConstructor(getNMSClass("PacketPlayOutTitle$EnumTitleAction"), getNMSClass("IChatBaseComponent")).newInstance(enumTitle, chatSerializerTitle);
			Object playSubtitle = getNMSClass("PacketPlayOutTitle").getConstructor(getNMSClass("PacketPlayOutTitle$EnumTitleAction"), getNMSClass("IChatBaseComponent")).newInstance(enumSubtitle, chatSerializerSubtitle);
			Object playTime = getNMSClass("PacketPlayOutTitle").getConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE).newInstance(fadeIn, stay, fadeOut);
			Object entityPlayer = player.getClass().getMethod("getHandle", (Class<?>[]) new Class[0]).invoke(player, new Object[0]);
			Object connection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
			Method sendPacket = connection.getClass().getMethod("sendPacket", getNMSClass("Packet"));
			sendPacket.invoke(connection, playTitle);
			if (subtitle != null && !"".equals(subtitle))
				sendPacket.invoke(connection, playSubtitle);
			sendPacket.invoke(connection, playTime);
			return true;
		} catch (InvocationTargetException | IllegalAccessException | InstantiationException | NoSuchMethodException | NoSuchFieldException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Display an action bar message to all players
	 * 
	 * @param message The message to send
	 */
	public void actionAll(final String message) {
		applyAll(new PlayerConsumer() {
			
			@Override
			public void apply(Player player) {
				sendActionBarMessage(player, message);
			}
		});
	}
	
	/**
	 * Display a title with an optional subtitle to all players
	 * 
	 * @param title The title to send
	 * @param subtitle The subtitle to send
	 * @param fadeIn The amount of time in ticks it takes to fade in
	 * @param stay The amount of time in ticks the message stays
	 * @param fadeOut The amount of time in ticks it takes to fade out
	 */
	public void titleAll(final String title, final String subtitle, final int fadeIn, final int stay, final int fadeOut) {
		applyAll(new PlayerConsumer() {
			
			@Override
			public void apply(Player player) {
				sendTitleMessage(player, title, subtitle, fadeIn, stay, fadeOut);
			}
		});
	}
	
	/**
	 * Create a listener for an event
	 * 
	 * @param <E> The event type to listen for
	 * @param listener What to do when the event happens
	 */
	@SuppressWarnings("unchecked")
	public <E extends Event> void listenEvent(EventListener<E> listener) {
		MinigameManager.getMinigameManager().addListener(this, (Class<E>) ((ParameterizedType) (listener.getClass().getGenericInterfaces()[0])).getActualTypeArguments()[0], listener);
	}
	
	/**
	 * Get a friendly display of a time
	 * 
	 * @param seconds The number of seconds
	 * 
	 * @return A user-friendly string
	 */
	public String getTime(int seconds) {
		if (seconds < 60) {
			if (seconds == 1)
				return "1 second";
			else
				return seconds + " seconds";
		} else {
			int minutes = seconds / 60;
			int remainder = seconds % 60;
			if (remainder == 0) {
				if (minutes == 1)
					return "1 minute";
				else
					return minutes + " minutes";
			} else {
				if (minutes != 1 && remainder != 1)
					return minutes + " minutes and " + remainder + " seconds";
				else if (minutes != 1)
					return minutes + " minutes and 1 second";
				else if (remainder != 1)
					return "1 minute and " + remainder + " seconds";
				else
					return "1 minute and 1 second";
			}
		}
	}
	
	/**
	 * Supplies a location for every player given
	 */
	public static interface LocationSupplier extends Function<Player, Location> {}
	
	/**
	 * Supplies an itemstack and a slot to place the itemstack for every player
	 * given
	 */
	public static interface ItemStackSupplier extends Function<Player, Tuple<ItemStack, Integer>> {}
	
	/**
	 * Represents a map for a minigame, containing its config name, display
	 * name, and author
	 * 
	 * @author DonkeyCore
	 */
	public static class MapInfo {
		
		/**
		 * Name of the map that can be found in the minigame's config file
		 */
		private final String configName;
		/**
		 * Name that is to be displayed to the players
		 */
		private final String name;
		/**
		 * Author(s) of the map, also displayed to the players
		 */
		private final String author;
		
		/**
		 * Create an instanceof MapInfo
		 * 
		 * @param configName Name of the map that can be found in the minigame's
		 *            config file
		 * @param name Name that is to be displayed to the players
		 * @param author Author(s) of the map, also displayed to the players
		 */
		public MapInfo(String configName, String name, String author) {
			this.configName = configName;
			this.name = name;
			this.author = author;
		}
		
		/**
		 * Get the name of the map that can be found in the minigame's config
		 * file
		 * 
		 * @return The config name
		 */
		public String getConfigName() {
			return configName;
		}
		
		/**
		 * Get the name that is to be displayed to the players
		 * 
		 * @return The display name
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * Get the author(s) of the map (this will be displayed to the players)
		 * 
		 * @return The author(s)
		 */
		public String getAuthor() {
			return author;
		}
		
	}
	
	/**
	 * Represents a bonus that is awarded to a player after the game ends for a
	 * reason other than winning
	 * 
	 * @author DonkeyCore
	 */
	public static class Bonus {
		
		/**
		 * UUID of the player
		 */
		private final UUID uuid;
		/**
		 * Amount to award
		 */
		private final int currency;
		/**
		 * Reason for awarding the currency
		 */
		private final String reason;
		
		/**
		 * Create a new bonus
		 * 
		 * @param uuid UUID of the player to award
		 * @param currency Amount to award
		 * @param reason Reason for awarding the currency
		 */
		private Bonus(UUID uuid, int currency, String reason) {
			this.uuid = uuid;
			this.currency = currency;
			this.reason = reason;
		}
		
		/**
		 * Get the UUID of the player being awarded
		 * 
		 * @return The UUID of the player
		 */
		public UUID getUUID() {
			return uuid;
		}
		
		/**
		 * Get the amount of currency to be awarded
		 * 
		 * @return The amount of currency
		 */
		public int getCurrency() {
			return currency;
		}
		
		/**
		 * Get the reason for awarding the currency to the player
		 * 
		 * @return The reason
		 */
		public String getReason() {
			return reason;
		}
		
	}
	
	/**
	 * Represents two values in one object
	 * 
	 * @author DonkeyCore
	 * 
	 * @param <L> Type on the left
	 * @param <R> Type on the right
	 */
	public static abstract class Tuple<L, R> {
		
		/**
		 * Get the object on the left
		 * 
		 * @return The object on the left
		 */
		public abstract L getLeft();
		
		/**
		 * Get the object on the right
		 * 
		 * @return The object on the right
		 */
		public abstract R getRight();
		
		/**
		 * Create a tuple
		 * 
		 * @param <L> The object to be on the left
		 * @param <R> The object to be on the right
		 * @param l The object on the left
		 * @param r The object on the right
		 * 
		 * @return A new immutable tuple
		 */
		public static <L, R> Tuple<L, R> of(L l, R r) {
			return new ImmutableTuple<L, R>(l, r);
		}
		
	}
	
	/**
	 * Tuple that cannot be changed
	 * 
	 * @author DonkeyCore
	 * 
	 * @param <L> Type on the left
	 * @param <R> Type on the right
	 */
	public static class ImmutableTuple<L, R> extends Tuple<L, R> {
		
		/**
		 * The object on the right
		 */
		private final L l;
		/**
		 * The object on the left
		 */
		private final R r;
		
		/**
		 * Create a new immutable tuple with the given values
		 * 
		 * @param l The object on the left
		 * @param r The object on the right
		 */
		public ImmutableTuple(L l, R r) {
			this.l = l;
			this.r = r;
		}
		
		@Override
		public L getLeft() {
			return l;
		}
		
		@Override
		public R getRight() {
			return r;
		}
		
	}
	
	/**
	 * Tuple that can be changed
	 * 
	 * @author DonkeyCore
	 * 
	 * @param <L> Type on the left
	 * @param <R> Type on the right
	 */
	public static class MutableTuple<L, R> extends Tuple<L, R> {
		
		/**
		 * The object on the left
		 */
		private L l;
		/**
		 * The object on the right
		 */
		private R r;
		
		/**
		 * Create a new mutable tuple with the given values
		 * 
		 * @param l The object on the left
		 * @param r The object on the right
		 */
		public MutableTuple(L l, R r) {
			this.l = l;
			this.r = r;
		}
		
		/**
		 * Create a new mutable tuple with no starting values
		 */
		public MutableTuple() {}
		
		@Override
		public L getLeft() {
			return l;
		}
		
		@Override
		public R getRight() {
			return r;
		}
		
		/**
		 * Set a new value for the object on the left
		 * 
		 * @param l The new left object
		 */
		public void setLeft(L l) {
			this.l = l;
		}
		
		/**
		 * Set a new value for the object on the right
		 * 
		 * @param r The new right object
		 */
		public void setRight(R r) {
			this.r = r;
		}
		
	}
	
	/**
	 * Performs some operation for every player given
	 * 
	 * @author DonkeyCore
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
	 * Listens to events sent by MinigameListener
	 * 
	 * @author DonkeyCore
	 */
	public static interface EventListener<E extends Event> {
		
		/**
		 * Accept an event and perform some operation
		 * 
		 * @param event The event to act based upon
		 */
		public void onEvent(E event);
		
	}
	
	/**
	 * Represents a function
	 * 
	 * @author DonkeyCore
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
	 * @author DonkeyCore
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
