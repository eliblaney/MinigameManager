package me.donkeycore.minigamemanager.api.nms;

import org.bukkit.Bukkit;

public class ReflectionAPI {
	
	public final String version;
	
	public ReflectionAPI() {
		this.version = Bukkit.getServer().getClass().getPackage().getName().replace(".",  ",").split(",")[3];
	}
	
	public Class<?> getCraftClass(String name) {
		try {
			return Class.forName("org.bukkit.craftbukkit." + version + "." + name);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
	
	public Class<?> getNMSClass(String name) {
		try {
			return Class.forName("net.minecraft.server." + version + "." + name);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
	
}
