package minigamemanager.core;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import org.bukkit.Server;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.projectiles.ProjectileSource;

import minigamemanager.core.MinigameManager.ListenerEntry;

/**
 * Custom implementation of PluginManager that redirects everything to a
 * SimplePluginManager with the exception of events, so that those can be
 * handled properly
 * 
 * @author DonkeyCore
 */
public final class MinigamePluginManagerWrapper implements PluginManager {
	
	final PluginManager pm;
	
	public MinigamePluginManagerWrapper(Server instance, SimpleCommandMap commandMap) {
		this.pm = new SimplePluginManager(instance, commandMap);
	}
	
	public MinigamePluginManagerWrapper(PluginManager pm) {
		this.pm = pm;
	}
	
	@Override
	public void addPermission(Permission perm) {
		pm.addPermission(perm);
	}
	
	@Override
	public void callEvent(Event event) throws IllegalStateException {
		ListenerEntry[] entries = MinigameManager.getMinigameManager().listeners.toArray(new ListenerEntry[MinigameManager.getMinigameManager().listeners.size()]);
		for (ListenerEntry e : entries) {
			try {
				if (e.event.isInstance(event)) {
					// make sure players are actually in the minigame
					// manual checks to optimize
					if (event instanceof PlayerEvent && !e.minigame.isPlaying(((PlayerEvent) event).getPlayer()))
						continue;
					// PlayerDeathEvent is not a PlayerEvent
					if (event instanceof PlayerDeathEvent && !e.minigame.isPlaying(((PlayerDeathEvent) event).getEntity()))
						continue;
					// check shooters of projectiles
					if(event instanceof ProjectileHitEvent) {
						ProjectileSource entity = ((ProjectileHitEvent) event).getEntity().getShooter();
						if(entity instanceof Player && !e.minigame.isPlaying((Player) entity))
							continue;
					}
					if(event instanceof ProjectileLaunchEvent) {
						ProjectileSource entity = ((ProjectileLaunchEvent) event).getEntity().getShooter();
						if(entity instanceof Player && !e.minigame.isPlaying((Player) entity))
							continue;
					}
					// cover any other events that have players involved
					try {
						// does it have a getPlayer() method?
						Method getPlayer = event.getClass().getMethod("getPlayer");
						getPlayer.setAccessible(true);
						// if the player is not in the minigame, skip it
						if (!e.minigame.isPlaying((Player) getPlayer.invoke(event)))
							continue;
					} catch (Exception ex) {}
					try {
						// does it have a getEntity() method?
						Method getPlayer = event.getClass().getMethod("getEntity");
						getPlayer.setAccessible(true);
						// is the entity a player?
						Entity entity = (Entity) getPlayer.invoke(event);
						// if the player is not in the minigame, skip it
						if (entity instanceof Player && !e.minigame.isPlaying((Player) entity))
							continue;
					} catch (Exception ex) {}
					// event is compatible with minigame; send to listener
					Method onEvent = e.listener.getClass().getDeclaredMethod("onEvent", e.event);
					onEvent.setAccessible(true);
					onEvent.invoke(e.listener, e.event.cast(event));
				}
			} catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException | ClassCastException ex) {
				ex.printStackTrace();
			}
		}
		pm.callEvent(event);
	}
	
	@Override
	public void clearPlugins() {
		pm.clearPlugins();
	}
	
	@Override
	public void disablePlugin(Plugin plugin) {
		pm.disablePlugin(plugin);
	}
	
	@Override
	public void disablePlugins() {
		pm.disablePlugins();
	}
	
	@Override
	public void enablePlugin(Plugin plugin) {
		pm.enablePlugin(plugin);
	}
	
	@Override
	public Set<Permissible> getDefaultPermSubscriptions(boolean op) {
		return pm.getDefaultPermSubscriptions(op);
	}
	
	@Override
	public Set<Permission> getDefaultPermissions(boolean op) {
		return pm.getDefaultPermissions(op);
	}
	
	@Override
	public Permission getPermission(String name) {
		return pm.getPermission(name);
	}
	
	@Override
	public Set<Permissible> getPermissionSubscriptions(String permission) {
		return pm.getPermissionSubscriptions(permission);
	}
	
	@Override
	public Set<Permission> getPermissions() {
		return pm.getPermissions();
	}
	
	@Override
	public Plugin getPlugin(String name) {
		return pm.getPlugin(name);
	}
	
	@Override
	public Plugin[] getPlugins() {
		return pm.getPlugins();
	}
	
	@Override
	public boolean isPluginEnabled(String name) {
		return pm.isPluginEnabled(name);
	}
	
	@Override
	public boolean isPluginEnabled(Plugin plugin) {
		return pm.isPluginEnabled(plugin);
	}
	
	@Override
	public Plugin loadPlugin(File file) throws InvalidPluginException, InvalidDescriptionException, UnknownDependencyException {
		return pm.loadPlugin(file);
	}
	
	@Override
	public Plugin[] loadPlugins(File directory) {
		return pm.loadPlugins(directory);
	}
	
	@Override
	public void recalculatePermissionDefaults(Permission perm) {
		pm.recalculatePermissionDefaults(perm);
	}
	
	@Override
	public void registerEvent(Class<? extends Event> event, Listener listener, EventPriority priority, EventExecutor executor, Plugin plugin) {
		pm.registerEvent(event, listener, priority, executor, plugin);
	}
	
	@Override
	public void registerEvent(Class<? extends Event> event, Listener listener, EventPriority priority, EventExecutor executor, Plugin plugin, boolean ignoreCancelled) {
		pm.registerEvent(event, listener, priority, executor, plugin, ignoreCancelled);
	}
	
	@Override
	public void registerEvents(Listener listener, Plugin plugin) {
		pm.registerEvents(listener, plugin);
	}
	
	@Override
	public void registerInterface(Class<? extends PluginLoader> loader) throws IllegalArgumentException {
		pm.registerInterface(loader);
	}
	
	@Override
	public void removePermission(Permission perm) {
		pm.removePermission(perm);
	}
	
	@Override
	public void removePermission(String name) {
		pm.removePermission(name);
	}
	
	@Override
	public void subscribeToDefaultPerms(boolean op, Permissible permissible) {
		pm.subscribeToDefaultPerms(op, permissible);
	}
	
	@Override
	public void subscribeToPermission(String permission, Permissible permissible) {
		pm.subscribeToPermission(permission, permissible);
	}
	
	@Override
	public void unsubscribeFromDefaultPerms(boolean op, Permissible permissible) {
		pm.unsubscribeFromDefaultPerms(op, permissible);
	}
	
	@Override
	public void unsubscribeFromPermission(String permission, Permissible permissible) {
		pm.unsubscribeFromPermission(permission, permissible);
	}
	
	@Override
	public boolean useTimings() {
		return pm.useTimings();
	}
	
}
