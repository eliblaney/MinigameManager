package me.donkeycore.minigamemanager.api.minigame;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Set the attributes of a minigame. Required for all minigames.
 * 
 * @author DonkeyCore
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MinigameAttributes {
	
	/**
	 * The name of the minigame
	 * 
	 * @return The name of the minigame
	 */
	String name();
	
	/**
	 * The type of the minigame
	 * 
	 * @return An instance of MinigameType
	 */
	MinigameType type();
	
	/**
	 * The authors of the minigame
	 * 
	 * @return An array of authors' names
	 */
	String[] authors() default {};
	
	/**
	 * Whether players should always have full health during the minigame
	 * 
	 * @return Whether players should always have full health (default false)
	 */
	boolean alwaysFullHealth() default false;
	
	/**
	 * Whether players should always be saturated during the minigame
	 * 
	 * @return Whether players should always be saturated (default false)
	 */
	boolean alwaysSaturated() default false;
	
	/**
	 * Whether players should be able to drop items on the ground
	 * 
	 * @return Whether players can drop items (default true)
	 */
	boolean canDropItems() default true;
	
	/**
	 * Whether players should be able to pick up items from the ground
	 * 
	 * @return Whether players can pick up items (default true)
	 */
	boolean canPickUpItems() default true;
	
	/**
	 * Whether the minigame is a default minigame that came with MinigameManager. Most likely false.
	 * 
	 * @return Whether the minigame is a default minigame
	 */
	boolean isDefault() default false;
	
}
