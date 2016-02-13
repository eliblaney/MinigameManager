package me.donkeycore.minigamemanager.minigames;

import java.util.Arrays;

import org.apache.commons.lang3.tuple.Pair;
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
		setGamemode(GameMode.ADVENTURE);
		healAll();
		clearAll();
		giveAll(new ItemStackSupplier() {
			
			@Override
			public Pair<ItemStack, Integer> apply(Player player) {
				ItemStack i = new ItemStack(Material.DIAMOND_SPADE); // Backup item
				try {
					i = ItemStackBuilder.fromMaterial(Material.DIAMOND_SPADE).unsafeEnchantment(Enchantment.DIG_SPEED, 10).unbreakable(true).lore("♪ Diggy Diggy Hole").flags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS).canDestroy("minecraft:snow").build();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return Pair.of(i, 0);
			}
		});
		alive.addAll(Arrays.asList(getPlayerUUIDs()));
		// create a scoreboard that lists everybody's names
		updateScoreboard();
		listenEvent(PlayerMoveEvent.class, new EventListener() {
			
			@Override
			public void onEvent(Event event) {
				PlayerMoveEvent e = (PlayerMoveEvent) event;
				Location l = e.getTo();
				Location from = e.getFrom();
				if (l.getBlockX() != from.getBlockX() || l.getBlockY() != from.getBlockY() || l.getBlockZ() != from.getBlockZ()) {
					if (l.getBlock().getType() == Material.LAVA || l.getBlock().getType() == Material.STATIONARY_LAVA)
						kill(e.getPlayer());
				}
			}
		});
	}
	
	private void kill(Player player) {
		setAlive(player, false);
		updateScoreboard();
		if (getAliveAmount() == 1 && MinigameManager.isRelease()) {
			announce("\u00a7a\u00a7l" + getAliveNames()[0] + " \u00a7e\u00a7lwins!");
			end();
		} else if (getAliveAmount() < 2 && !MinigameManager.isRelease()) {
			announce("\u00a7aFinished!");
			end();
		} else if (getAliveAmount() < 1)
			end(MinigameErrors.NOT_ENOUGH_PLAYERS);
	}
	
	private void updateScoreboard() {
		Scoreboard s = new ScoreboardBuilder("blah", "\u00a7b\u00a7lAlive").setLines(getAliveNamesWithColor(ChatColor.GREEN)).build();
		setScoreboard(s);
	}
	
	@Override
	public Location getStartingLocation() {
		return spawns[random.nextInt(spawns.length)];
	}
	
}
