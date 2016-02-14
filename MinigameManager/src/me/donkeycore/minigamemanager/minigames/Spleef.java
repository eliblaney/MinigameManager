package me.donkeycore.minigamemanager.minigames;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;

import me.donkeycore.minigamemanager.api.items.ItemStackBuilder;
import me.donkeycore.minigamemanager.api.minigame.Minigame;
import me.donkeycore.minigamemanager.api.minigame.MinigameAttributes;
import me.donkeycore.minigamemanager.api.minigame.MinigameErrors;
import me.donkeycore.minigamemanager.api.minigame.MinigameType;
import me.donkeycore.minigamemanager.api.rotation.Rotation;
import me.donkeycore.minigamemanager.api.scoreboard.ScoreboardBuilder;
import me.donkeycore.minigamemanager.core.MinigameManager;

/**
 * A default minigame where players attempt to destroy snow blocks under other
 * players to make them fall into lava (last person alive wins)
 * 
 * @author DonkeyCore
 */
@DefaultMinigame
@MinigameAttributes(name = "Spleef", type = MinigameType.LAST_MAN_STANDING, authors = "DonkeyCore", alwaysFullHealth = true, alwaysSaturated = true, canDropItems = false, canPickUpItems = false)
public class Spleef extends Minigame {
	
	private ItemStack shovel;
	
	{
		ItemStackBuilder builder = ItemStackBuilder.fromMaterial(Material.DIAMOND_SPADE).unsafeEnchantment(Enchantment.DIG_SPEED, 10).unbreakable(true).lore("♪ Diggy Diggy Hole").flags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS);
		try {
			builder.canDestroy("minecraft:snow");
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException e) {
			// only thrown if canDestroy(...) failed to add NBT data
			e.printStackTrace();
		}
		shovel = builder.build();
	}
	
	public Spleef(Rotation r) {
		super(r, "map1", getMapinfo("map1")[0], getMapinfo("map1")[1]);
	}
	
	private static String[] getMapinfo(String map) {
		return getMinigameManager().getMinigameLocations().getMapInfo(((MinigameAttributes) Spleef.class.getAnnotation(MinigameAttributes.class)).name(), map);
	}
	
	@Override
	public void onStart() {
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
		// listen to when the player moves
		listenEvent(new EventListener<PlayerMoveEvent>() {
			
			@Override
			public void onEvent(PlayerMoveEvent event) {
				// filter only those who are playing and are alive
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
	}
	
	private void kill(Player player) {
		// announce their death and mark them as dead
		announce("\u00a7c" + player.getName() + " \u00a7rdied from \u00a76burning in lava\u00a7r.");
		setAlive(player, false);
		// update the scoreboard without the player in the list
		updateScoreboard();
		// We have a winner!
		if (getAliveAmount() == 1) {
			titleAll("\u00a7a" + getAliveNames()[0] + " \u00a7rwins!", null, 5, 20, 5);
			end();
			// We're just testing with a single person, everything seemed to work fine
		} else if (getAliveAmount() == 0 && !MinigameManager.isRelease()) {
			announce("\u00a7aFinished!");
			end();
			// Just in case, end the game if there aren't any people left (though this case should never happen)
		} else if (getAliveAmount() < 1)
			end(MinigameErrors.NOT_ENOUGH_PLAYERS);
	}
	
	private void updateScoreboard() {
		// build a scoreboard with a display name of "Alive" in aqua/bold, listing the alive player names in green
		Scoreboard s = new ScoreboardBuilder("spleef" + getId() + "_alive", "\u00a7b\u00a7lAlive").setLines(getAliveNamesWithColor(ChatColor.GREEN)).build();
		// update the scoreboard for everybody
		setScoreboard(s);
	}
	
	@Override
	public Location getStartingLocation(Player player) {
		// random spawn
		return spawns[random.nextInt(spawns.length)];
	}
	
}
