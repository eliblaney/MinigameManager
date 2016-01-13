package me.donkeycore.minigamemanager.api.rotation;

import org.apache.commons.lang.Validate;

import me.donkeycore.minigamemanager.rotations.DefaultRotationManager;

public final class SubstitutionHandler {
	
	private static SubstitutionHandler instance;
	private Class<? extends RotationManager> rotationManager = DefaultRotationManager.class;
	
	private SubstitutionHandler() {}
	
	public Class<? extends RotationManager> getRotationManager() {
		return rotationManager;
	}
	
	public void setRotationManager(Class<? extends RotationManager> rotationManager) {
		Validate.notNull(rotationManager);
		this.rotationManager = rotationManager;
	}
	
	public static SubstitutionHandler getInstance() {
		if (instance == null)
			instance = new SubstitutionHandler();
		return instance;
	}
	
}
