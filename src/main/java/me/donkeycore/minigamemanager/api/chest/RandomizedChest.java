package me.donkeycore.minigamemanager.api.chest;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class RandomizedChest {
	
	/**
	 * Random object for finding random slots
	 */
	private final Random r;
	/**
	 * The location of the chest
	 */
	private final Location loc;
	/**
	 * The tier of the chest
	 */
	private int tier;
	/**
	 * Whether the chest is active or not
	 */
	private boolean active = true;
	
	/**
	 * Create a new randomized chest
	 * 
	 * @param loc The location of the chest
	 * @param tier The tier of the chest
	 */
	public RandomizedChest(Location loc, int tier) {
		this(loc, tier, 0);
	}
	
	/**
	 * Create a new randomized chest with a seed
	 * 
	 * @param loc The location of the chest
	 * @param tier The tier of the chest
	 * @param seed The seed for {@link Random}
	 */
	public RandomizedChest(Location loc, int tier, int seed) {
		this.loc = loc;
		this.tier = tier;
		if (seed == 0)
			this.r = new Random();
		else
			this.r = new Random(seed);
	}
	
	/**
	 * Set the chest's tier
	 * 
	 * @param tier The chest's new tier
	 */
	public void setTier(int tier) {
		this.tier = tier;
	}
	
	/**
	 * Get the chest tier
	 * 
	 * @return The chest's tier
	 */
	public int getTier() {
		return tier;
	}
	
	/**
	 * Set whether the chest is active or not
	 * 
	 * @param active Whether the chest should be active or not
	 */
	public void setActive(boolean active) {
		this.active = active;
	}
	
	/**
	 * Determine whether the chest should be filled with items
	 * 
	 * @return Whether the chest is active
	 */
	public boolean isActive() {
		return active;
	}
	
	/**
	 * Fill the chest randomly with items
	 * 
	 * @param items The items to fill the chest with
	 * 
	 * @return Whether the chest successfully filled (or successfully blocked from being inactive)
	 */
	public boolean fill(ItemStack... items) {
		if(!active)
			return true;
		Block block = loc.getBlock();
		if (block != null && block.getState() instanceof Chest) {
			Chest chest = (Chest) block.getState();
			Inventory inv = chest.getBlockInventory();
			inv.clear();
			for (ItemStack i : items) {
				int slot = 0;
				do {
					if (inv.firstEmpty() == -1) // inventory full
						return false;
					slot = r.nextInt(inv.getSize());
				} while (inv.getItem(slot) != null && inv.getItem(slot).getType().equals(i.getType()));
				ItemStack it = inv.getItem(slot);
				if (it != null && it.getType().equals(i.getType())) {
					int amount = it.getAmount() + i.getAmount();
					if(amount > it.getMaxStackSize())
						amount = it.getMaxStackSize();
					it.setAmount(amount);
				} else
					inv.setItem(slot, i);
			}
			return true;
		} else
			return false;
	}
	
}
