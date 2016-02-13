package me.donkeycore.minigamemanager.minigames;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
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

@DefaultMinigame
@MinigameAttributes(name = "Spleef", type = MinigameType.LAST_MAN_STANDING, authors = "DonkeyCore", alwaysFullHealth = true, alwaysFullHunger = true)
public class Spleef extends Minigame {
	
	private final Location[] spawns;
	
	public Spleef(Rotation r) {
		super(r);
		spawns = getMinigameManager().getMinigameLocations().getMinigameSpawns(getName());
	}
	
	@Override
	public void onStart() {
		// set everybody's gamemode to adventure
		setGamemode(GameMode.ADVENTURE);
		// heal everybody to full health and hunger
		healAll();
		// clear everybody's inventories
		clearAll();
		// give everybody a very efficient diamond shovel
		giveAll(new ItemStackSupplier() {
			
			@Override
			public Tuple<ItemStack, Integer> apply(Player player) {
				ItemStack i = new ItemStack(Material.DIAMOND_SPADE); // Backup item
				try {
					i = ItemStackBuilder.fromMaterial(Material.DIAMOND_SPADE).unsafeEnchantment(Enchantment.DIG_SPEED, 10).unbreakable(true).lore("♪ Diggy Diggy Hole").flags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS).canDestroy("minecraft:snow").build();
				} catch (Exception e) {
					// only thrown if canDestroy(...) failed to add NBT data
					e.printStackTrace();
				}
				// return itemstack to be put in the first slot of the hotbar
				return Tuple.of(i, 0);
			}
		});
		// create a scoreboard that lists everybody's names
		updateScoreboard();
		// listen to when the player moves
		listenEvent(PlayerMoveEvent.class, new EventListener() {
			
			@Override
			public void onEvent(Event event) {
				PlayerMoveEvent e = (PlayerMoveEvent) event;
				Location l = e.getTo();
				Location from = e.getFrom();
				// if the player moves a whole block, check for lava, and if so, remove them from the game
				if (l.getBlockX() != from.getBlockX() || l.getBlockY() != from.getBlockY() || l.getBlockZ() != from.getBlockZ()) {
					if (l.getBlock().getType() == Material.LAVA || l.getBlock().getType() == Material.STATIONARY_LAVA)
						kill(e.getPlayer());
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
			announce("\u00a7a" + getAliveNames()[0] + " \u00a7rwins!");
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
	public Location getStartingLocation() {
		// random spawn
		return spawns[random.nextInt(spawns.length)];
	}
	
}
