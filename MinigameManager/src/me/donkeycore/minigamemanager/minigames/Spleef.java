package me.donkeycore.minigamemanager.minigames;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import me.donkeycore.minigamemanager.api.items.ItemStackBuilder;
import me.donkeycore.minigamemanager.api.minigame.Minigame;
import me.donkeycore.minigamemanager.api.minigame.MinigameAttributes;
import me.donkeycore.minigamemanager.api.minigame.MinigameType;
import me.donkeycore.minigamemanager.api.rotation.Rotation;
import me.donkeycore.minigamemanager.api.teams.Team;

@DefaultMinigame
@MinigameAttributes(name = "Spleef", type = MinigameType.LAST_MAN_STANDING, authors = "DonkeyCore", alwaysFullHealth = true, alwaysFullHunger = true)
public class Spleef extends Minigame {
	
	private final Location[] spawns;
	Team[] teams;
	
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
	}
	
	@Override
	public Location getStartingLocation() {
		return spawns[random.nextInt(spawns.length)];
	}
	
}
