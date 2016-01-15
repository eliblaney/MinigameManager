package me.donkeycore.minigamemanager.api.minigame;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.google.common.base.Function;

import me.donkeycore.minigamemanager.api.rotation.Rotation;
import me.donkeycore.minigamemanager.core.MinigameManager;

/**
 * The main minigame API class; all minigames should extend this class
 * 
 * @author DonkeyCore
 */
public abstract class Minigame {
	
	private final Rotation r;
	
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
	 */
	public void onEnd() {}
	
	/**
	 * Call this to end the minigame and continue to the next rotation<br>
	 * <b>Note:</b> If this method is being overriden, make sure there is a call
	 * to {@link Rotation#finish()} or a call to
	 * 
	 * <pre>
	 * super.end()
	 * </pre>
	 */
	public void end() {
		r.finish();
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
		List<UUID> uuids = getRotation().getInGame();
		Player[] players = new Player[uuids.size()];
		int i = 0;
		for (UUID u : uuids)
			players[i++] = Bukkit.getPlayer(u);
		return players;
	}
	
	/**
	 * Teleport all players to a location
	 * 
	 * @param supplier A {@link LocationSupplier} that, when given a player,
	 *            returns the
	 *            location to teleport that player.
	 */
	public void teleportAll(LocationSupplier supplier) {
		for (Player player : getPlayers())
			getRotation().teleportAll(supplier.apply(player));
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
	 * Supplies a location for every player given
	 */
	public static interface LocationSupplier extends Function<Player, Location> {}
	
	/**
	 * Supplise an itemstack and a slot to place the itemstack for every player
	 * given
	 */
	public static interface ItemStackSupplier extends Function<Player, Pair<ItemStack, Integer>> {}
	
	/**
	 * Performs some operation for every player given
	 */
	public static interface PlayerConsumer {
		
		public void apply(Player player);
		
	}
	
}
