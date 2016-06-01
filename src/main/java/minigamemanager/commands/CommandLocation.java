package minigamemanager.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import minigamemanager.api.config.CustomConfig;
import minigamemanager.api.minigame.Minigame;
import minigamemanager.api.minigame.MinigameAttributes;
import minigamemanager.config.MessageType;
import minigamemanager.config.MinigameLocations;
import minigamemanager.config.MinigameMessages;
import minigamemanager.core.MinigameManager;

/**
 * /location command - Modifies locations
 * 
 * @author DonkeyCore
 */
public class CommandLocation implements CommandExecutor {
	
	private final MinigameManager manager;
	
	public CommandLocation(MinigameManager manager) {
		this.manager = manager;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		MinigameMessages messages = manager.getMessages();
		if (cmd.getName().equalsIgnoreCase("location")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(messages.getMessage(MessageType.ONLY_PLAYERS));
				return true;
			}
			if (args.length < 2) {
				sender.sendMessage(messages.getMessage(MessageType.TOO_LITTLE_ARGUMENTS));
				return false;
			}
			final MinigameLocations locs = manager.getDefaultMinigameLocations();
			final FileConfiguration config = locs.getConfig();
			final Player player = (Player) sender;
			Location loc = player.getLocation();
			String locStr = "(" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ", " + ((int) loc.getYaw()) + ", " + ((int) loc.getPitch()) + ")";
			if (args.length == 2) {
				if (args[0].equalsIgnoreCase("set")) {
					if (args[1].equalsIgnoreCase("lobby")) {
						ConfigurationSection lobby = config.getConfigurationSection("rotations").getConfigurationSection("lobby");
						setLocation(lobby, loc);
						player.sendMessage(messages.getMessage(MessageType.LOCATION_SET).replace("%for%", "lobby").replace("%loc%", locStr));
					} else if (args[1].equalsIgnoreCase("spawn")) {
						ConfigurationSection spawn = config.getConfigurationSection("rotations").getConfigurationSection("spawn");
						setLocation(spawn, loc);
						player.sendMessage(messages.getMessage(MessageType.LOCATION_SET).replace("%for%", "spawn").replace("%loc%", locStr));
					} else
						return false;
				} else if (args[0].equalsIgnoreCase("view")) {
					if (args[1].equalsIgnoreCase("lobby")) {
						ConfigurationSection lobby = config.getConfigurationSection("rotations").getConfigurationSection("lobby");
						loc = getLocation(lobby);
						player.sendMessage(replaceLocationVars(messages.getMessage(MessageType.LOCATION_VIEW).replace("%name%", "lobby"), loc));
					} else if (args[1].equalsIgnoreCase("spawn")) {
						ConfigurationSection spawn = config.getConfigurationSection("rotations").getConfigurationSection("spawn");
						loc = getLocation(spawn);
						player.sendMessage(replaceLocationVars(messages.getMessage(MessageType.LOCATION_VIEW).replace("%name%", "spawn"), loc));
					} else
						return false;
				} else
					return false;
			} else if (args.length >= 4) {
				Class<? extends Minigame> m = manager.getMinigame(args[1]);
				ConfigurationSection map = null;
				Object o = null;
				if (m == null) {
					player.sendMessage(messages.getMessage(MessageType.COULDNT_FIND_MINIGAME).replace("%name%", args[1]));
					return false;
				} else {
					o = getMap(m, config);
					if (o instanceof ConfigurationSection)
						map = (ConfigurationSection) o;
					else if (o instanceof CustomConfig)
						map = ((CustomConfig) o).getConfig();
				}
				if (args[0].equalsIgnoreCase("set")) {
					if (!map.contains(args[2])) {
						map.createSection(args[2]);
						ConfigurationSection inside = map.getConfigurationSection(args[2]);
						inside.createSection("mapinfo");
						inside.getConfigurationSection("mapinfo").set("name", "Arena");
						inside.getConfigurationSection("mapinfo").set("author", "the Server Admins");
						inside.createSection("spawns");
						inside.set("chests", new ArrayList<String>());
					}
					if(args.length > 4 && args[3].equalsIgnoreCase("chest")) {
						int tier = 0;
						try {
							tier = Integer.parseInt(args[4]);
							// tier must be positive
							if(tier < 0)
								throw new Exception();
						} catch(Exception e) {
							player.sendMessage(messages.getMessage(MessageType.NOT_VALID_NUMBER));
							return false;
						}
						List<String> chests = map.getStringList("chests");
						chests.add(tier + "," + loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
						map.set("chests", chests);
						if (o != null && o instanceof CustomConfig)
							((CustomConfig) o).saveConfig();
						else
							locs.saveConfig();
						return true;
					}
					if (args[3].equalsIgnoreCase("none") || args[3].equalsIgnoreCase("null") || args[3].equalsIgnoreCase("delete")) {
						map.set(args[2], null);
						if (o != null && o instanceof CustomConfig)
							((CustomConfig) o).saveConfig();
						else
							locs.saveConfig();
						player.sendMessage(messages.getMessage(MessageType.LOCATION_DELETE).replace("%for%", args[2]));
						return true;
					}
					map = map.getConfigurationSection(args[2]).getConfigurationSection("spawns");
					if (!map.contains(args[3]))
						map.createSection(args[3]);
					ConfigurationSection name = map.getConfigurationSection(args[3]);
					if (args.length > 4 && (args[4].equalsIgnoreCase("none") || args[4].equalsIgnoreCase("null") || args[4].equalsIgnoreCase("delete"))) {
						setLocation(name, null);
						if (o != null && o instanceof CustomConfig)
							((CustomConfig) o).saveConfig();
						else
							locs.saveConfig();
						player.sendMessage(messages.getMessage(MessageType.LOCATION_DELETE).replace("%for%", args[3]));
						return true;
					}
					setLocation(name, loc);
					if (o != null && o instanceof CustomConfig)
						((CustomConfig) o).saveConfig();
					else
						locs.saveConfig();
					player.sendMessage(messages.getMessage(MessageType.LOCATION_SET).replace("%for%", args[1]).replace("%loc%", locStr));
				} else if (args[0].equalsIgnoreCase("view")) {
					if (map == null)
						return false;
					map = map.getConfigurationSection(args[2]);
					if (map == null)
						return false;
					map = map.getConfigurationSection("spawns");
					if (map == null || !map.contains(args[3]))
						return false;
					loc = getLocation(map.getConfigurationSection(args[3]));
					player.sendMessage(replaceLocationVars(messages.getMessage(MessageType.LOCATION_VIEW).replace("%name%", args[1]), loc));
				} else
					return false;
			} else if (args.length > 4) {
				if (args[0].equalsIgnoreCase("mapinfo")) {
					Class<? extends Minigame> m = manager.getMinigame(args[1]);
					ConfigurationSection map = null;
					Object o;
					if (m == null) {
						player.sendMessage(messages.getMessage(MessageType.COULDNT_FIND_MINIGAME).replace("%name%", args[1]));
						return false;
					} else {
						o = getMap(m, config);
						if (o instanceof ConfigurationSection)
							map = (ConfigurationSection) o;
						else if (o instanceof CustomConfig)
							map = ((CustomConfig) o).getConfig();
					}
					if (!map.contains(args[2]) || !map.getConfigurationSection(args[2]).contains("mapinfo"))
						return false;
					map = map.getConfigurationSection(args[2]).getConfigurationSection("mapinfo");
					StringBuilder sb = new StringBuilder();
					for (int i = 4; i < args.length; i++)
						sb.append(args[i] + " ");
					String value = sb.toString().trim();
					if (args[3].equalsIgnoreCase("name"))
						map.set("name", value);
					else if (args[3].equalsIgnoreCase("author"))
						map.set("author", value);
					else
						return false;
					if (o != null && o instanceof CustomConfig)
						((CustomConfig) o).saveConfig();
					else
						locs.saveConfig();
					player.sendMessage(messages.getMessage(MessageType.MAPINFO_SET).replace("%key%", args[3].toLowerCase()).replace("%value%", value));
				} else
					return false;
			} else
				return false;
		} else
			return false;
		return true;
		
	}
	
	private Object getMap(Class<? extends Minigame> m, FileConfiguration defaultConfig) {
		MinigameAttributes attr = m.getAnnotation(MinigameAttributes.class);
		Object o;
		if (attr.isDefault())
			o = defaultConfig.getConfigurationSection("default-minigames").getConfigurationSection(attr.name());
		else {
			o = manager.getData(m).getConfig();
			if (o == null)
				throw new RuntimeException(attr.name() + " did not specify a FileConfiguration for its locations!");
		}
		return o;
	}
	
	private void setLocation(ConfigurationSection section, Location loc) {
		if (loc != null) {
			section.set("world", loc.getWorld().getName());
			section.set("x", loc.getX());
			section.set("y", loc.getY());
			section.set("z", loc.getZ());
			section.set("yaw", loc.getYaw());
			section.set("pitch", loc.getPitch());
		} else
			section.getParent().set(section.getName(), null);
	}
	
	private Location getLocation(ConfigurationSection section) {
		World world = Bukkit.getWorld(section.getString("world"));
		double x = section.getDouble("x");
		double y = section.getDouble("y");
		double z = section.getDouble("z");
		float yaw = Float.parseFloat(section.getString("yaw"));
		float pitch = Float.parseFloat(section.getString("pitch"));
		return new Location(world, x, y, z, yaw, pitch);
	}
	
	private String replaceLocationVars(String s, Location loc) {
		return s.replace("%x%", "" + loc.getBlockX()).replace("%y%", "" + loc.getBlockY()).replace("%z%", "" + loc.getBlockZ()).replace("%yaw%", "" + ((int) loc.getYaw())).replace("%pitch%", "" + ((int) loc.getPitch()));
	}
	
}
