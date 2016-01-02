package me.donkeycore.minigamemanager.minigames;

import org.bukkit.Location;

import me.donkeycore.minigamemanager.api.Minigame;
import me.donkeycore.minigamemanager.api.MinigameAttributes;
import me.donkeycore.minigamemanager.api.MinigameType;
import me.donkeycore.minigamemanager.api.Rotation;

@MinigameAttributes(type = MinigameType.LAST_MAN_STANDING)
public class Spleef extends Minigame {

	public Spleef(Rotation r) {
		super(r);
		// TODO: Get spawn locations from locations.yml
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
		// TODO Auto-generated method stub
		return null;
	}
	
}
