package minigamemanager.api.achivement;

import org.apache.commons.lang.Validate;

import minigamemanager.api.minigame.Minigame;

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
	 * Create a new achievement
	 * 
	 * @param parent The class of the minigame that owns this achievement
	 * @param name The display name of the achievement
	 * @param description The description of the achievement
	 */
	public Achievement(Class<? extends Minigame> parent, String name, String description) {
		Validate.notEmpty(name, "The name of the achievement cannot be empty");
		Validate.notEmpty(description, "The description of the achievement cannot be null");
		this.parent = parent;
		this.name = name;
		this.description = description;
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
	 * The achievement's hash code will be used to identify it in the save files
	 * <br>
	 * <br>
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
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
