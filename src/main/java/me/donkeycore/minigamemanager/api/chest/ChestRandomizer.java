package me.donkeycore.minigamemanager.api.chest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.inventory.ItemStack;

public class ChestRandomizer {
	
	/**
	 * All of the randomized chests that will be handled
	 */
	private final RandomizedChest[] chests;
	/**
	 * The tiers of chests with corresponding items
	 */
	private final Map<Integer, ItemStack[]> items = new HashMap<>();
	
	/**
	 * Create an instance of ChestRandomizer
	 * 
	 * @param chests An array of randomized chests belonging to this randomizer
	 */
	public ChestRandomizer(RandomizedChest... chests) {
		this.chests = chests;
	}
	
	/**
	 * Get the chests
	 * 
	 * @return An array of randomized chests
	 */
	public RandomizedChest[] getChests() {
		return chests;
	}
	
	/**
	 * Set the items for a particular tier of chests
	 * 
	 * @param tier The tier of chests (power of two)
	 * @param items An array of items that are available to those chests
	 */
	public void setItems(int tier, ItemStack... items) {
		this.items.put(tier, items);
	}
	
	/**
	 * Get the items for a particular tier of chests
	 * 
	 * @param tier The tier of chests
	 * 			
	 * @return All the items that are available to that tier
	 */
	public ItemStack[] getItems(int tier) {
		List<ItemStack> items = new ArrayList<ItemStack>();
		for (Entry<Integer, ItemStack[]> e : this.items.entrySet()) {
			if ((tier & e.getKey()) == 1)
				items.addAll(Arrays.asList(e.getValue()));
		}
		return items.toArray(new ItemStack[items.size()]);
	}
	
	/**
	 * Fill all the chests with items of their tier
	 */
	public void fill() {
		for (RandomizedChest c : chests)
			c.fill(getItems(c.getTier()));
	}
	
}
