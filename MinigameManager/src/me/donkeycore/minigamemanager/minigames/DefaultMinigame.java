package me.donkeycore.minigamemanager.minigames;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to indicate if a minigame is a default minigame. Do not use.
 * 
 * @author DonkeyCore
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DefaultMinigame {}
