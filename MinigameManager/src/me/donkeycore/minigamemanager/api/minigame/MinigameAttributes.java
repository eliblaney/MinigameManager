package me.donkeycore.minigamemanager.api.minigame;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MinigameAttributes {
	
	String name();
	
	MinigameType type();
	
	String[] authors() default {};
	
}
