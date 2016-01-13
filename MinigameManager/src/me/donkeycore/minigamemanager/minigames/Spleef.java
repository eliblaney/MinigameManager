package me.donkeycore.minigamemanager.minigames;

import java.util.Random;

import org.bukkit.Location;

import me.donkeycore.minigamemanager.api.minigame.Minigame;
import me.donkeycore.minigamemanager.api.minigame.MinigameAttributes;
import me.donkeycore.minigamemanager.api.minigame.MinigameType;
import me.donkeycore.minigamemanager.api.rotation.Rotation;

@DefaultMinigame
@MinigameAttributes(name = "Spleef", type = MinigameType.LAST_MAN_STANDING)
public class Spleef extends Minigame {
	
	private final Location[] spawns;
	
	public Spleef(Rotation r) {
		super(r);
		spawns = getMinigameManager().getMinigameLocations().getMinigameSpawns(getName());
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Location getStartingLocation() {
		return spawns[new Random().nextInt(spawns.length)];
	}
	
}
