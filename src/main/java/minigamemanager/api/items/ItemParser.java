package minigamemanager.api.items;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

/**
 * Helper class to parse Strings into {@link ItemStack ItemStacks}
 * 
 * @author DonkeyCore
 */
public class ItemParser {
	
	public static ItemStack parseItem(String item) {
		// Format:
		// material[,amount][,durability][;enchantment[:level]...]
		try {
			String material = item.replaceAll(",.*", "").replaceAll(";.*", "");
			ItemStackBuilder b = ItemStackBuilder.fromMaterial(Material.matchMaterial(material));
			String[] properties = item.split(",");
			/*
			 * properties[0] = material <-- ignore
			 * properties[1] = amount (+ maybe junk)
			 * properties[2] = durability (+ maybe junk)
			 * replaceAll(";.*", "") -> get rid of possible junk
			 */
			if (properties.length > 1)
				b.amount(Integer.parseInt(properties[1].replaceAll(";.*", "")));
			if (properties.length > 2)
				b.durability(Short.parseShort(properties[2].replaceAll(";.*", "")));
			String[] enchants = item.split(";");
			/**
			 * enchants[0] = material (+ maybe amount, durability)
			 * enchants[1...n] = enchants
			 */
			// skip everything below if there aren't any enchants
			if (enchants.length <= 1)
				return b.build();
			for (int i = 1 /* skip first entry */; i < enchants.length; i++) {
				String e = enchants[i];
				// reuse older variable because convenient name
				properties = e.split(":");
				String enchantName = enchantAliases(properties[0]).toUpperCase().replace(' ', '_');
				if (properties.length == 1)
					b.unsafeEnchantment(Enchantment.getByName(enchantName), 1);
				else
					b.unsafeEnchantment(Enchantment.getByName(enchantName), Integer.parseInt(properties[1]));
			}
			return b.build();
		} catch (Throwable t) {
			throw new ItemFormatException(t);
		}
	}
	
	public static ItemStack[] parseItems(String... items) {
		ItemStack[] is = new ItemStack[items.length];
		for (int i = 0; i < items.length; i++)
			is[i] = parseItem(items[i]);
		return is;
	}
	
	/**
	 * Replaces common enchantment names with their Bukkit versions
	 *  
	 * @param e An enchantment string containing common names of enchantments
	 * 
	 * @return The string with the common names replaced with their Bukkit versions
	 */
	private static String enchantAliases(String e) {
		return e.toLowerCase().replaceAll("protection$", "protection environmental").replaceAll("protection(\\S)", "protection environmental").replace("feather falling", "protection fall").replace("blast protection", "protection explosions").replace("sharpness", "damage all").replace("smite", "damage undead").replace("bane of arthropods", "damage arthropods").replace("respiration", "oxygen").replace("aqua affinity", "water worker").replace("looting", "loot bonus mobs").replace("efficiency", "dig speed").replace("unbreaking", "durability").replace("power", "arrow damage").replace("punch", "arrow knockback").replace("infinity", "arrow infinite").replace("flame", "arrow fire");
	}
	
	public static class ItemFormatException extends IllegalArgumentException {
		
		private static final long serialVersionUID = -2936619347545826060L;
		
		public ItemFormatException() {
			super();
		}
		
		public ItemFormatException(String message) {
			super(message);
		}
		
		public ItemFormatException(String message, Throwable cause) {
			super(message, cause);
		}
		
		public ItemFormatException(Throwable cause) {
			super(cause);
		}
		
	}
	
}
