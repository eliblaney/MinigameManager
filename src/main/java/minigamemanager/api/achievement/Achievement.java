package minigamemanager.api.achievement;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import minigamemanager.api.items.ItemStackBuilder;
import minigamemanager.api.minigame.Minigame;
import minigamemanager.api.minigame.MinigameAttributes;
import minigamemanager.core.MinigameManager;

/**
 * An achievement that a player can obtain by playing minigames
 * 
 * @author DonkeyCore
 */
public class Achievement {
	
	/**
	 * The class of the minigame that owns this achievement
	 */
	private final Class<? extends Minigame> parent;
	/**
	 * The display name of the achievement
	 */
	private final String name;
	/**
	 * The description of the achievement
	 */
	private final String description;
	/**
	 * The ID of the achievement. 0 = generated ID
	 */
	private final int id;
	/**
	 * Item that represents the achievement (e.g. in a menu)
	 */
	private ItemStack item;
	
	/**
	 * Create a new achievement. Its ID is generated from its parent minigame,
	 * name, and description.
	 * 
	 * @param parent The class of the minigame that owns this achievement
	 * @param name The display name of the achievement
	 * @param description The description of the achievement
	 */
	public Achievement(Class<? extends Minigame> parent, String name, String description) {
		this(parent, name, description, 0);
	}
	
	/**
	 * Create a new achievement with a specific ID number
	 * 
	 * @param parent The class of the minigame that owns this achievement
	 * @param name The display name of the achievement
	 * @param material The material to use for the default item that represents
	 *            the achievement
	 * @param description The description of the achievement
	 */
	public Achievement(Class<? extends Minigame> parent, String name, String description, Material material) {
		this(parent, name, description, 0);
		setItemWithDefaults(material);
	}
	
	/**
	 * Create a new achievement with a specific ID number
	 * 
	 * @param parent The class of the minigame that owns this achievement
	 * @param name The display name of the achievement
	 * @param item The item that represents the achievement
	 * @param description The description of the achievement
	 */
	public Achievement(Class<? extends Minigame> parent, String name, String description, ItemStack item) {
		this(parent, name, description, 0);
		this.item = item;
	}
	
	/**
	 * Create a new achievement with a specific ID number
	 * 
	 * @param parent The class of the minigame that owns this achievement
	 * @param name The display name of the achievement
	 * @param description The description of the achievement
	 * @param id The ID number to identify this achievement
	 */
	public Achievement(Class<? extends Minigame> parent, String name, String description, int id) {
		Validate.notEmpty(name, "The name of the achievement cannot be empty");
		Validate.notEmpty(description, "The description of the achievement cannot be null");
		this.parent = parent;
		this.name = name;
		this.description = description;
		this.id = id;
		setItemWithDefaults(Material.NETHER_STAR);
	}
	
	/**
	 * Get the parent of this achievement
	 * 
	 * @return The class of the minigame that owns this achievement
	 */
	public Class<? extends Minigame> getParent() {
		return parent;
	}
	
	/**
	 * Get the display name of the achievement
	 * 
	 * @return The display name of the achievement
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Get the description of the achievement
	 * 
	 * @return The description of the achievement
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Get the item that is used to represent this achievement (in a menu for
	 * example)
	 * 
	 * @return An ItemStack that represents this achievement
	 */
	public ItemStack getItem() {
		return item;
	}
	
	/**
	 * Set the new item that will be used to represent this achievement
	 * 
	 * @param item The new ItemStack
	 * 
	 * @return The old ItemStack
	 */
	public ItemStack setItem(ItemStack item) {
		ItemStack old = this.item;
		this.item = item;
		return old;
	}
	
	/**
	 * Set the new item that will be used to represent this achievement using a
	 * default name and description based on the name/description of the
	 * achievement. Will show name in dark purple, prefixed by the minigame, description in light gray,
	 * and be shiny.
	 * 
	 * @param material The material of the new item
	 * 
	 * @return The generated ItemStack
	 */
	public ItemStack setItemWithDefaults(Material material) {
		MinigameAttributes attr = Minigame.getAttributes(parent);
		return this.item = ItemStackBuilder.fromMaterial(material).name("&5 [" + (attr.shortName().equals("") ? attr.name() : attr.shortName()) + "] " + name).shiny().lore("&7" + description).build();
	}
	
	/**
	 * The achievement's hash code will be used to identify it in the save files
	 * <br>
	 * <br>
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		if (id != 0) // custom ID
			return id;
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		if (parent != null)
			result = prime * result + ((parent == null) ? 0 : parent.getName().hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Achievement other = (Achievement) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		return hashCode() == other.hashCode();
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
}
