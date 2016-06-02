package minigamemanager.minigames;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;

import minigamemanager.api.items.ItemStackBuilder;
import minigamemanager.api.minigame.Minigame;
import minigamemanager.api.minigame.MinigameAttributes;
import minigamemanager.api.minigame.MinigameErrors;
import minigamemanager.api.minigame.MinigameType;
import minigamemanager.api.rotation.Rotation;
import minigamemanager.api.scoreboard.ScoreboardBuilder;
import minigamemanager.api.winner.SingleWinnerList;
import minigamemanager.core.MinigameManager;

/**
 * A default minigame where players attempt to destroy snow blocks under other
 * players to make them fall into lava (last person alive wins)
 * 
 * @author DonkeyCore
 */
@MinigameAttributes(name = "Spleef", type = MinigameType.LAST_MAN_STANDING, authors = "DonkeyCore", alwaysFullHealth = true, alwaysSaturated = true, canDropItems = false, canPickUpItems = false, isDefault = true)
public class Spleef extends Minigame {
	
	private final ItemStack shovel;
	private UUID secondPlace, thirdPlace;
	
	public Spleef(Rotation r) {
		super(r, randomMap(Spleef.class));
		ItemStackBuilder builder = ItemStackBuilder.fromMaterial(Material.DIAMOND_SPADE).unsafeEnchantment(Enchantment.DIG_SPEED, 10).unbreakable(true).lore("��� Diggy Diggy Hole").flags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS);
		try {
			builder.canDestroy("minecraft:snow");
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException e) {
			// only thrown if canDestroy(...) failed to add NBT data
			e.printStackTrace();
		}
		shovel = builder.build();
	}
	
	@Override
	public void onStart() {
		// listen to when the player moves
		listenEvent(new EventListener<PlayerMoveEvent>() {
			
			@Override
			public void onEvent(PlayerMoveEvent event) {
				// filter only those who are alive
				if (!isAlive(event.getPlayer()))
					return;
				Location l = event.getTo();
				Location from = event.getFrom();
				// if the player moves a whole block, check for lava, and if so, remove them from the game
				if (l.getBlockX() != from.getBlockX() || l.getBlockY() != from.getBlockY() || l.getBlockZ() != from.getBlockZ()) {
					if (l.getBlock().getType() == Material.LAVA || l.getBlock().getType() == Material.STATIONARY_LAVA)
						kill(event.getPlayer());
				}
			}
		});
		// give everybody a very efficient diamond shovel
		giveAll(new ItemStackSupplier() {
			
			@Override
			public Tuple<ItemStack, Integer> apply(Player player) {
				// return itemstack to be put in the first slot of the hotbar
				return Tuple.of(shovel, 0);
			}
		});
		// create a scoreboard that lists everybody's names
		updateScoreboard();
	}
	
	private void kill(Player player) {
		// announce their death and mark them as dead
		announce(ChatColor.RED + player.getName() + ChatColor.RESET + " died from " + ChatColor.RED + "burning in lava" + ChatColor.RESET + ".");
		setAlive(player, false);
		thirdPlace = secondPlace;
		secondPlace = player.getUniqueId();
		// update the scoreboard without the player in the list
		updateScoreboard();
		// We have a winner!
		if (getAliveAmount() == 1)
			end(new SingleWinnerList(getAliveUUIDs()[0], secondPlace, thirdPlace));
		// We're just testing with a single person, everything seemed to work fine
		else if (getAliveAmount() == 0 && !MinigameManager.isRelease()) {
			announce(ChatColor.GREEN + "Finished!");
			end(null);
			// Just in case, end the game if there aren't any people left (though this case should never happen)
		} else if (getAliveAmount() < 1)
			end(MinigameErrors.NOT_ENOUGH_PLAYERS, null);
	}
	
	private void updateScoreboard() {
		// build a scoreboard with a display name of "Alive" in aqua/bold, listing the alive player names in green
		Scoreboard s = new ScoreboardBuilder("splf" + getId() + "_alive", "&b&lAlive").setLines(getAliveNamesWithColor(ChatColor.GREEN)).build();
		// update the scoreboard for everybody
		setScoreboard(s);
	}
	
	@Override
	public Location getStartingLocation(Player player) {
		// random spawn
		return spawns[random.nextInt(spawns.length)];
	}
	
}
