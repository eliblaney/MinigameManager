package minigamemanager.api.chest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import minigamemanager.api.config.MinigameConfig;
import minigamemanager.api.items.ItemParser;
import minigamemanager.api.minigame.Minigame;
import minigamemanager.config.MinigameLocations;
import minigamemanager.core.MinigameManager;

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
	 * Create an instance of ChestRandomizer, automatically finding chests from
	 * config files
	 * 
	 * @param minigame The minigame that supplies the randomized chests
	 */
	public ChestRandomizer(Minigame minigame) {
		final List<String> chestStrings;
		if (minigame.isDefault()) {
			MinigameLocations ml = MinigameManager.getMinigameManager().getDefaultMinigameLocations();
			chestStrings = ml.getConfig().getConfigurationSection("default-minigames").getConfigurationSection(minigame.getName().replace(' ', '_')).getConfigurationSection(minigame.getMap()).getStringList("chests");
		} else {
			MinigameConfig c = MinigameManager.getMinigameManager().getMinigameConfig(minigame.getClass());
			chestStrings = c.getConfig().getConfigurationSection(minigame.getMap()).getStringList("chests");
		}
		this.chests = new RandomizedChest[chestStrings.size()];
		for (int i = 0; i < chests.length; i++) {
			String str = chestStrings.get(i);
			String[] arr = str.split(",");
			Location l = new Location(Bukkit.getWorld(arr[1]), Integer.parseInt(arr[2]), Integer.parseInt(arr[3]), Integer.parseInt(arr[4]));
			chests[i] = new RandomizedChest(l, Integer.parseInt(arr[0]));
		}
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
	 * Automatically create {@link ItemStack ItemStacks} and set items for each
	 * tier based on a config
	 * 
	 * @param chests The {@link ConfigurationSection} where the item information
	 *            can be found. Must contain String lists that are each named
	 *            the tier that they belong to.
	 */
	public void setItems(ConfigurationSection chests) {
		for(String key : chests.getKeys(false)) {
			int tier = Integer.parseInt(key);
			List<String> itemStrings = chests.getStringList(key);
			ItemStack[] items = ItemParser.parseItems(itemStrings.toArray(new String[itemStrings.size()]));
			setItems(tier, items);
		}
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
			if ((tier & e.getKey()) == e.getKey())
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
