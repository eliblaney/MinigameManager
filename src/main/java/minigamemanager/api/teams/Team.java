package minigamemanager.api.teams;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import minigamemanager.api.minigame.Minigame.LocationSupplier;

/**
 * Represents a group of players that are working together in a minigame
 */
public class Team {
	
	/**
	 * The scoreboard version of the team
	 */
	private org.bukkit.scoreboard.Team team = null;
	/**
	 * The list of players on the team
	 */
	private List<UUID> players = new LinkedList<>();
	/**
	 * The name of the team
	 */
	private String name;
	/**
	 * The prefix color of the team
	 */
	private ChatColor color = ChatColor.WHITE;
	
	/**
	 * Create a new team with the specified players
	 * 
	 * @param players The players to have by default. Can be empty.
	 */
	public Team(Player... players) {
		for (Player player : players)
			this.players.add(player.getUniqueId());
	}
	
	/**
	 * Empty constructor that is used for the builder
	 */
	private Team() {}
	
	/**
	 * Attach the Bukkit team class to this team
	 * 
	 * @param team The Bukkit team class
	 */
	public void setScoreboardTeam(org.bukkit.scoreboard.Team team) {
		this.team = team;
	}
	
	/**
	 * Create a new Bukkit team and populate it with this team's entires
	 * 
	 * @param scoreboard The scoreboard from which to create the team
	 * @param name The name of the team
	 * @return The newly created team
	 */
	public org.bukkit.scoreboard.Team createScoreboardTeam(Scoreboard scoreboard, String name) {
		setScoreboardTeam(scoreboard.registerNewTeam(name));
		for (Player player : getPlayers())
			team.addEntry(player.getName());
		team.setDisplayName(getDisplayName());
		team.setPrefix(color.toString());
		return team;
	}
	
	/**
	 * Get the Bukkit team associated with this team
	 * 
	 * @return An instance of Bukkit's Team class
	 */
	public org.bukkit.scoreboard.Team getScoreboardTeam() {
		return team;
	}
	
	/**
	 * Set the new team name
	 * 
	 * @param name The team's name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Get the team name
	 * 
	 * @return The team's name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Get the team's name, with the addition of color and the replacement of
	 * underscores with spaces
	 * 
	 * @return The team's name with color and spaces added to it
	 */
	public String getDisplayName() {
		return color + name.replace('_', ' ');
	}
	
	/**
	 * Set the new team's color
	 * 
	 * @param color The team's color
	 */
	public void setColor(ChatColor color) {
		this.color = color;
	}
	
	/**
	 * Get the team's color
	 * 
	 * @return The team's color
	 */
	public ChatColor getColor() {
		return color;
	}
	
	/**
	 * Teleport all team players to a location
	 * 
	 * @param location The {@link Location} to teleport to
	 */
	public void teleport(Location location) {
		for (Player player : getPlayers())
			player.teleport(location);
	}
	
	/**
	 * Teleport all team players to locations as specified by the supplier
	 * 
	 * @param supplier A {@link LocationSupplier} that, when given a player,
	 *            returns a location
	 */
	public void teleport(LocationSupplier supplier) {
		for (Player player : getPlayers())
			player.teleport(supplier.apply(player));
	}
	
	/**
	 * Message all team players
	 * 
	 * @param message The message to send
	 */
	public void message(String message) {
		for (Player player : getPlayers())
			player.sendMessage(message);
	}
	
	/**
	 * Add a player to the team. Equivalent to {@link #addPlayer(UUID)}
	 * 
	 * @param player The player to add
	 * @return Whether the player was successfully added
	 * @see #addPlayer(UUID)
	 */
	public boolean addPlayer(Player player) {
		return addPlayer(player.getUniqueId());
	}
	
	/**
	 * Add a player to the team
	 * 
	 * @param player The UUID of the player to add
	 * @return Whether the player was successfully added
	 */
	public boolean addPlayer(UUID player) {
		return players.add(player);
	}
	
	/**
	 * Remove a player from the team. Equivalent to {@link #removePlayer(UUID)}
	 * 
	 * @param player The player to remove
	 * @return Whether the player was successfully removed
	 * @see #removePlayer(UUID)
	 */
	public boolean removePlayer(Player player) {
		return removePlayer(player.getUniqueId());
	}
	
	/**
	 * Remove a player from the team.
	 * 
	 * @param player The UUID of the player to remove
	 * @return Whether the player was successfully removed
	 */
	public boolean removePlayer(UUID player) {
		return players.remove(player);
	}
	
	/**
	 * Check to see if the team contains a player. Equivalent to
	 * {@link #containsPlayer(UUID)}
	 * 
	 * @param player The UUID of the player to check
	 * @return Whether the player is on the team
	 * @see #containsPlayer(UUID)
	 */
	public boolean containsPlayer(Player player) {
		return containsPlayer(player.getUniqueId());
	}
	
	/**
	 * Check to see if the team contains a player
	 * 
	 * @param player The player to check
	 * @return Whether the player is on the team
	 */
	public boolean containsPlayer(UUID player) {
		return players.contains(player);
	}
	
	/**
	 * Get the array of players that are on the team
	 * 
	 * @return An array of {@link Player} objects that are on the team
	 */
	public Player[] getPlayers() {
		Player[] players = new Player[this.players.size()];
		int i = 0;
		for (UUID u : this.players) {
			Player player = Bukkit.getPlayer(u);
			if (player == null)
				this.players.remove(u);
			else
				players[i++] = player;
		}
		return players;
	}
	
	/**
	 * Clear the team from all values
	 */
	public void clear() {
		players.clear();
		name = null;
		color = null;
		if (team != null)
			team.unregister();
		team = null;
	}
	
	/**
	 * Builder class to generate teams
	 */
	public static class Builder {
		
		/**
		 * The scoreboard version of the team
		 */
		private org.bukkit.scoreboard.Team team;
		/**
		 * Create a new team automatically if the scoreboard is nonnull
		 */
		private Scoreboard scoreboard;
		/**
		 * The list of players on the team
		 */
		private List<UUID> players = new LinkedList<>();
		/**
		 * The name of the team
		 */
		private String name;
		/**
		 * The prefix color of the team
		 */
		private ChatColor color;
		
		/**
		 * Set the scoreboard version of the team. Mutually exclusive with the
		 * other team method.
		 * 
		 * @param team The scoreboard version of the team
		 * 
		 * @return The builder instance
		 */
		public Builder team(org.bukkit.scoreboard.Team team) {
			if (this.scoreboard != null)
				throw new IllegalStateException("Cannot set a team if a team is queued to exist");
			this.team = team;
			return this;
		}
		
		/**
		 * Whether to create a scoreboard version of the team. Mutually
		 * exclusive with the other team method.
		 * 
		 * @param scoreboard The scoreboard to use. Set to null to disable
		 *            creating a team.
		 * 
		 * @return The builder instance
		 */
		public Builder team(Scoreboard scoreboard) {
			if (this.team != null && scoreboard != null)
				throw new IllegalStateException("Cannot create a team if there is an existing one");
			this.scoreboard = scoreboard;
			return this;
		}
		
		/**
		 * Add players on the team. Can be called multiple times.
		 * 
		 * @param players The players to add to the team
		 * 
		 * @return The builder instance
		 */
		public Builder players(Player... players) {
			for (Player player : players)
				this.players.add(player.getUniqueId());
			return this;
		}
		
		/**
		 * Add players on the team. Can be called multiple times.
		 * 
		 * @param uuids The players to add to the team
		 * 
		 * @return The builder instance
		 */
		public Builder players(UUID... uuids) {
			for (UUID uuid : uuids)
				this.players.add(uuid);
			return this;
		}
		
		/**
		 * Set the name of the team
		 * 
		 * @param name The new name of the team
		 * 
		 * @return The builder instance
		 */
		public Builder name(String name) {
			this.name = name;
			return this;
		}
		
		/**
		 * Set the prefix color of the team
		 * 
		 * @param color The new color
		 * 
		 * @return The builder instance
		 */
		public Builder color(ChatColor color) {
			this.color = color;
			return this;
		}
		
		/**
		 * Create the team
		 * 
		 * @return The newly created Team instance
		 */
		public Team build() {
			Team team = new Team();
			if (this.team != null)
				team.team = this.team;
			if (this.players != null)
				team.players = this.players;
			if (this.name != null)
				team.name = this.name;
			if (this.color != null)
				team.color = this.color;
			if (this.scoreboard != null)
				team.createScoreboardTeam(this.scoreboard, this.name);
			return team;
		}
		
	}
	
}
