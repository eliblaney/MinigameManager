package me.donkeycore.minigamemanager.minigames;

import java.util.Random;

import org.bukkit.Location;

import me.donkeycore.minigamemanager.api.Minigame;
import me.donkeycore.minigamemanager.api.MinigameAttributes;
import me.donkeycore.minigamemanager.api.MinigameType;
import me.donkeycore.minigamemanager.api.Rotation;

@MinigameAttributes(type = MinigameType.LAST_MAN_STANDING)
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
	public String getName() {
		return "Spleef";
	}
	
	@Override
	public Location getStartingLocation() {
		return spawns[new Random().nextInt(spawns.length)];
	}
	
}
