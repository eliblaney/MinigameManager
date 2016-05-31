package minigamemanager.api.items;

import static minigamemanager.api.nms.ReflectionAPI.getCraftClass;
import static minigamemanager.api.nms.ReflectionAPI.getNMSClass;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Dye;
import org.bukkit.material.MaterialData;

/**
 * Class to build a custom ItemStack
 * 
 * @author DonkeyCore
 */
public class ItemStackBuilder {
	
	/**
	 * The itemstack to build
	 */
	private ItemStack i;
	
	/**
	 * Create a new instance of the builder
	 * 
	 * @param itemstack The base itemstack
	 */
	public ItemStackBuilder(ItemStack itemstack) {
		Validate.notNull(itemstack);
		this.i = itemstack;
	}
	
	/**
	 * Create a new instance of the builder
	 * 
	 * @param dye The dye to create from
	 * 
	 * @return The builder instance
	 */
	public static ItemStackBuilder fromDye(Dye dye) {
		return new ItemStackBuilder(dye.toItemStack());
	}
	
	/**
	 * Create a new instance of the builder
	 * 
	 * @param itemstack The base itemstack
	 * 
	 * @return The builder instance
	 */
	public static ItemStackBuilder fromItemStack(ItemStack itemstack) {
		return new ItemStackBuilder(itemstack);
	}
	
	/**
	 * Create a new instance of the builder
	 * 
	 * @param material The material to use
	 * 
	 * @return The builder instance
	 */
	public static ItemStackBuilder fromMaterial(Material material) {
		return new ItemStackBuilder(new ItemStack(material));
	}
	
	/**
	 * Create a new instance of the builder
	 * 
	 * @param data The MaterialData to use
	 * 
	 * @return The builder instance
	 */
	public static ItemStackBuilder fromMaterialData(MaterialData data) {
		return new ItemStackBuilder(data.toItemStack());
	}
	
	/**
	 * Set the material data
	 * 
	 * @param data The new material data
	 * 
	 * @return The builder instance
	 */
	public ItemStackBuilder materialData(MaterialData data) {
		i.setData(data);
		return this;
	}
	
	/**
	 * Set the data
	 * 
	 * @param data The data to set
	 * @return The builder instance
	 * 
	 * @deprecated {@link MaterialData#setData(byte)} is deprecated
	 */
	public ItemStackBuilder data(byte data) {
		MaterialData md = i.getData();
		md.setData(data);
		i.setData(md);
		return this;
	}
	
	/**
	 * Add an enchantment
	 * 
	 * @param ench The enchantment to add
	 * @param level The level of the enchantment
	 * 
	 * @return The builder instance
	 */
	public ItemStackBuilder enchantment(Enchantment ench, int level) {
		i.addEnchantment(ench, level);
		return this;
	}
	
	/**
	 * Add an unsafe enchantment
	 * 
	 * @param ench The enchantment to add
	 * @param level The level of the enchantment which can be any positive level
	 * 
	 * @return The builder instance
	 */
	public ItemStackBuilder unsafeEnchantment(Enchantment ench, int level) {
		i.addUnsafeEnchantment(ench, level);
		return this;
	}
	
	/**
	 * Add the 'Shiny I' enchantment<br>
	 * NOTE: This method automatically hides enchantment on the item. If you
	 * make a call to {@link #lore(String...)} and want Shiny I, call this
	 * method AFTER you call {@link #lore(String...)}
	 * 
	 * @return The builder instance
	 */
	public ItemStackBuilder shiny() {
		unsafeEnchantment(Enchantment.LUCK, 0);
		List<String> lore = i.getItemMeta().getLore();
		lore.add(0, "\u00a77Shiny I");
		lore(lore.toArray(new String[lore.size()]));
		flags(ItemFlag.HIDE_ENCHANTS);
		return this;
	}
	
	/**
	 * Set the size of the itemstack
	 * 
	 * @param amount The new size
	 * @return The builder instance
	 */
	public ItemStackBuilder amount(int amount) {
		i.setAmount(amount);
		return this;
	}
	
	/**
	 * Set the durability
	 * 
	 * @param durability The new durability
	 * 
	 * @return The builder instance
	 */
	public ItemStackBuilder durability(short durability) {
		i.setDurability(durability);
		return this;
	}
	
	/**
	 * Set the display name
	 * 
	 * @param name The new name; colors will be translated from the ampersand
	 * 
	 * @return The builder instance
	 */
	public ItemStackBuilder name(String name) {
		ItemMeta im = i.getItemMeta();
		im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		i.setItemMeta(im);
		return this;
	}
	
	/**
	 * Set the lore
	 * 
	 * @param lore A string array for each line of lore
	 * 
	 * @return The builder instance
	 */
	public ItemStackBuilder lore(String... lore) {
		ItemMeta im = i.getItemMeta();
		int index = 0;
		for (String s : lore)
			lore[index++] = ChatColor.translateAlternateColorCodes('&', s);
		im.setLore(Arrays.asList(lore));
		i.setItemMeta(im);
		return this;
	}
	
	/**
	 * Set the flags
	 * 
	 * @param flags The array of flags to be set
	 * 
	 * @return The builder instance
	 */
	public ItemStackBuilder flags(ItemFlag... flags) {
		ItemMeta im = i.getItemMeta();
		im.addItemFlags(flags);
		i.setItemMeta(im);
		return this;
	}
	
	/**
	 * Set whether the item is unbreakable - Requires spigot!
	 * 
	 * @param unbreakable Whether the item should be unbreakable
	 * 
	 * @return The builder instance
	 */
	public ItemStackBuilder unbreakable(boolean unbreakable) {
		ItemMeta im = i.getItemMeta();
		im.spigot().setUnbreakable(unbreakable);
		i.setItemMeta(im);
		return this;
	}
	
	/**
	 * Set what blocks the tool can destroy
	 * 
	 * @param destroyable The names of the blocks that can be destroyed
	 * 
	 * @return The builder instance
	 * 
	 * @throws IllegalAccessException Thrown by a problem in reflection
	 * @throws IllegalArgumentException Thrown by a problem in reflection
	 * @throws InvocationTargetException Thrown by a problem in reflection
	 * @throws NoSuchMethodException Thrown by a problem in reflection
	 * @throws SecurityException Thrown by a problem in reflection
	 * @throws InstantiationException Thrown by a problem in reflection
	 */
	public ItemStackBuilder canDestroy(String... destroyable) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, InstantiationException {
		Class<?> nbttagcompound = getNMSClass("NBTTagCompound");
		Class<?> nbttagstring = getNMSClass("NBTTagString");
		Class<?> nbttaglist = getNMSClass("NBTTagList");
		Class<?> nbtbase = getNMSClass("NBTBase");
		
		Class<?> craftItemStack = getCraftClass("inventory.CraftItemStack");
		// CraftItemStack.asNMSCopy(i);
		Object nmsItemStack = craftItemStack.getMethod("asNMSCopy", ItemStack.class).invoke(null, i);
		Class<?> clazz = nmsItemStack.getClass();
		
		// ItemStack.hasTag();
		Boolean hasTag = (Boolean) clazz.getMethod("hasTag").invoke(nmsItemStack);
		if (!hasTag)
			// ItemStack.setTag(new NBTTagCompound());
			clazz.getMethod("setTag", nbttagcompound).invoke(nmsItemStack, nbttagcompound.newInstance());
		// ItemStack.getTag();
		Object tag = clazz.getMethod("getTag").invoke(nmsItemStack);
		// NBTTagList list = new NBTTagList();
		Object list = nbttaglist.newInstance();
		for (String d : destroyable) {
			// NBTTagList.add(new NBTTagString(d));
			nbttaglist.getMethod("add", nbtbase).invoke(list, nbttagstring.getConstructor(String.class).newInstance(d));
		}
		// NBTTagCompound.set("CanDestroy", list);
		tag.getClass().getMethod("set", String.class, nbtbase).invoke(tag, "CanDestroy", list);
		// ItemStack.setTag(tag);
		clazz.getMethod("setTag", nbttagcompound).invoke(nmsItemStack, tag);
		// i = CraftItemStack.asBukkitCopy(nmsItemStack);
		i = (ItemStack) craftItemStack.getMethod("asBukkitCopy", clazz).invoke(null, nmsItemStack);
		return this;
	}
	
	/**
	 * Set what blocks the block can be placed on
	 * 
	 * @param placeable The names of the blocks that can be placed on
	 * 
	 * @return The builder instance
	 * 
	 * @throws IllegalAccessException Thrown by a problem in reflection
	 * @throws IllegalArgumentException Thrown by a problem in reflection
	 * @throws InvocationTargetException Thrown by a problem in reflection
	 * @throws NoSuchMethodException Thrown by a problem in reflection
	 * @throws SecurityException Thrown by a problem in reflection
	 * @throws InstantiationException Thrown by a problem in reflection
	 */
	public ItemStackBuilder canPlaceOn(String... placeable) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, InstantiationException {
		Class<?> nbttagcompound = getNMSClass("NBTTagCompound");
		Class<?> nbttagstring = getNMSClass("NBTTagString");
		Class<?> nbttaglist = getNMSClass("NBTTagList");
		Class<?> nbtbase = getNMSClass("NBTBase");
		
		Class<?> craftItemStack = getCraftClass("inventory.CraftItemStack");
		// CraftItemStack.asNMSCopy(i);
		Object nmsItemStack = craftItemStack.getMethod("asNMSCopy", ItemStack.class).invoke(null, i);
		Class<?> clazz = nmsItemStack.getClass();
		
		// ItemStack.hasTag();
		Boolean hasTag = (Boolean) clazz.getMethod("hasTag").invoke(nmsItemStack);
		if (!hasTag)
			// ItemStack.setTag(new NBTTagCompound());
			clazz.getMethod("setTag", nbttagcompound).invoke(nmsItemStack, nbttagcompound.newInstance());
		// ItemStack.getTag();
		Object tag = clazz.getMethod("getTag").invoke(nmsItemStack);
		// NBTTagList list = new NBTTagList();
		Object list = nbttaglist.newInstance();
		for (String d : placeable) {
			// NBTTagList.add(new NBTTagString(d));
			nbttaglist.getMethod("add", nbtbase).invoke(list, nbttagstring.getConstructor(String.class).newInstance(d));
		}
		// NBTTagCompound.set("CanDestroy", list);
		tag.getClass().getMethod("set", String.class, nbtbase).invoke(tag, "CanPlaceOn", list);
		// ItemStack.setTag(tag);
		clazz.getMethod("setTag", nbttagcompound).invoke(nmsItemStack, tag);
		// i = CraftItemStack.asBukkitCopy(nmsItemStack);
		i = (ItemStack) craftItemStack.getMethod("asBukkitCopy", clazz).invoke(null, nmsItemStack);
		return this;
	}
	
	/**
	 * Get the instance of the itemstack
	 * 
	 * @return The builder instance
	 */
	public ItemStack build() {
		return i;
	}
	
}
