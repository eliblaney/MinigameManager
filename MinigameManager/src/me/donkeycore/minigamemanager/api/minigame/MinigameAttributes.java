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
	 * @return Whether players should always have full health
	 */
	boolean alwaysFullHealth() default false;
	
	/**
	 * Whether players should always be saturated during the minigame
	 * 
	 * @return Whether players should always be saturated
	 */
	boolean alwaysSaturated() default false;
	
}
