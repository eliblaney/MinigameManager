package me.donkeycore.minigamemanager.api.items;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Dye;
import org.bukkit.material.MaterialData;

import me.donkeycore.minigamemanager.api.nms.ReflectionAPI;

public class ItemStackBuilder {
	
	private ItemStack i;
	
	public ItemStackBuilder(ItemStack itemstack) {
		Validate.notNull(itemstack);
		this.i = itemstack;
	}
	
	public static ItemStackBuilder fromDye(Dye dye) {
		return new ItemStackBuilder(dye.toItemStack());
	}
	
	public static ItemStackBuilder fromItemStack(ItemStack itemstack) {
		return new ItemStackBuilder(itemstack);
	}
	
	public static ItemStackBuilder fromMaterial(Material material) {
		return new ItemStackBuilder(new ItemStack(material));
	}
	
	public static ItemStackBuilder fromMaterialData(MaterialData data) {
		return new ItemStackBuilder(data.toItemStack());
	}
	
	public ItemStackBuilder materialData(MaterialData data) {
		i.setData(data);
		return this;
	}
	
	@SuppressWarnings("deprecation")
	public ItemStackBuilder data(byte data) {
		MaterialData md = i.getData();
		md.setData(data);
		i.setData(md);
		return this;
	}
	
	public ItemStackBuilder enchantment(Enchantment ench, int level) {
		i.addEnchantment(ench, level);
		return this;
	}
	
	public ItemStackBuilder unsafeEnchantment(Enchantment ench, int level) {
		i.addUnsafeEnchantment(ench, level);
		return this;
	}
	
	public ItemStackBuilder amount(int amount) {
		i.setAmount(amount);
		return this;
	}
	
	public ItemStackBuilder durability(short durability) {
		i.setDurability(durability);
		return this;
	}
	
	public ItemStackBuilder name(String name) {
		ItemMeta im = i.getItemMeta();
		im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		i.setItemMeta(im);
		return this;
	}
	
	public ItemStackBuilder lore(String... lore) {
		ItemMeta im = i.getItemMeta();
		int index = 0;
		for (String s : lore)
			lore[index++] = ChatColor.translateAlternateColorCodes('&', s);
		im.setLore(Arrays.asList(lore));
		i.setItemMeta(im);
		return this;
	}
	
	public ItemStackBuilder flags(ItemFlag... flags) {
		ItemMeta im = i.getItemMeta();
		im.addItemFlags(flags);
		i.setItemMeta(im);
		return this;
	}
	
	// Requires spigot!
	public ItemStackBuilder unbreakable(boolean unbreakable) {
		ItemMeta im = i.getItemMeta();
		im.spigot().setUnbreakable(unbreakable);
		i.setItemMeta(im);
		return this;
	}
	
	public ItemStackBuilder canDestroy(String... destroyable) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, InstantiationException {
		ReflectionAPI nms = new ReflectionAPI();
		Class<?> nbttagcompound = nms.getNMSClass("NBTTagCompound");
		Class<?> nbttagstring = nms.getNMSClass("NBTTagString");
		Class<?> nbttaglist = nms.getNMSClass("NBTTagList");
		Class<?> nbtbase = nms.getNMSClass("NBTBase");
		
		Class<?> craftItemStack = nms.getCraftClass("inventory.CraftItemStack");
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
		for(String d : destroyable) {
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
	
	public ItemStackBuilder canPlaceOn(String... placeable) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, InstantiationException {
		ReflectionAPI nms = new ReflectionAPI();
		Class<?> nbttagcompound = nms.getNMSClass("NBTTagCompound");
		Class<?> nbttagstring = nms.getNMSClass("NBTTagString");
		Class<?> nbttaglist = nms.getNMSClass("NBTTagList");
		Class<?> nbtbase = nms.getNMSClass("NBTBase");
		
		Class<?> craftItemStack = nms.getCraftClass("inventory.CraftItemStack");
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
		for(String d : placeable) {
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
	
	public ItemStack build() {
		return i;
	}
	
}
