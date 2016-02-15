package me.donkeycore.minigamemanager.minigames;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;

import me.donkeycore.minigamemanager.api.items.ItemStackBuilder;
import me.donkeycore.minigamemanager.api.minigame.Minigame;
import me.donkeycore.minigamemanager.api.minigame.MinigameAttributes;
import me.donkeycore.minigamemanager.api.minigame.MinigameType;
import me.donkeycore.minigamemanager.api.rotation.Rotation;
import me.donkeycore.minigamemanager.api.scoreboard.ScoreboardBuilder;
import me.donkeycore.minigamemanager.core.MinigameManager;

@DefaultMinigame
@MinigameAttributes(name = "One_In_The_Quiver", type = MinigameType.MOST_POINTS, authors = "DonkeyCore", alwaysSaturated = true, canDropItems = false, canPickUpItems = false)
public class OneInTheQuiver extends Minigame {
	
	/**
	 * Everybody's display names with their respective kills
	 */
	private final Map<String, Integer> kills;
	/**
	 * Unbreakable bow
	 */
	private final ItemStack bow = ItemStackBuilder.fromMaterial(Material.BOW).unbreakable(true).lore("Don't miss!").shiny().flags(ItemFlag.HIDE_UNBREAKABLE).build();
	/**
	 * Unbreakable iron axe
	 */
	private final ItemStack axe = ItemStackBuilder.fromMaterial(Material.IRON_AXE).unbreakable(true).lore("Well, at least I have this...").shiny().flags(ItemFlag.HIDE_UNBREAKABLE).build();
	/**
	 * Main timer set for 5 minutes
	 */
	private BukkitTask timer;
	/**
	 * Time left (default 5 minutes)
	 */
	private int time = 5 * 60;
	
	public OneInTheQuiver(Rotation r) {
		super(r, "map1", getMapinfo("map1")[0], getMapinfo("map1")[1]);
		kills = new HashMap<String, Integer>();
	}
	
	private static String[] getMapinfo(String map) {
		return getMinigameManager().getMinigameLocations().getMapInfo(((MinigameAttributes) OneInTheQuiver.class.getAnnotation(MinigameAttributes.class)).name(), map);
	}
	
	@Override
	public void onStart() {
		giveAll(new ItemStackSupplier() {
			
			@Override
			public Tuple<ItemStack, Integer> apply(Player player) {
				// return itemstack to be put in the first slot of the hotbar
				return Tuple.of(bow, 0);
			}
		});
		giveAll(new ItemStackSupplier() {
			
			@Override
			public Tuple<ItemStack, Integer> apply(Player player) {
				// return itemstack to be put in the first slot of the hotbar
				return Tuple.of(axe, 1);
			}
		});
		giveAll(new ItemStack(Material.ARROW), 2);
		potionAll(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 60, 10));
		for (String name : getPlayerNamesWithColor(ChatColor.GREEN))
			kills.put(name, 0);
		updateScoreboard();
		// listen to when the player takes damage
		listenEvent(new EventListener<EntityDamageByEntityEvent>() {
			
			@Override
			public void onEvent(EntityDamageByEntityEvent event) {
				// filter only those who are playing and are alive
				Entity entity = event.getEntity();
				Entity damager = event.getDamager();
				if (!(entity instanceof Player))
					return;
				if (damager instanceof Projectile) {
					if (!isAlive((Player) event.getEntity()))
						return;
					Projectile proj = (Projectile) damager;
					ProjectileSource projShooter = proj.getShooter();
					if (proj instanceof Arrow && projShooter instanceof Player) {
						Player killer = (Player) projShooter;
						if (entity.getUniqueId().equals(killer.getUniqueId()))
							announce(ChatColor.GOLD + entity.getName() + ChatColor.RED + " committed suicide.");
						else {
							announce(ChatColor.RED + entity.getName() + ChatColor.RESET + " was killed by " + ChatColor.RED + killer.getName() + ChatColor.RESET + ".");
							killer.getInventory().addItem(new ItemStack(Material.ARROW));
							kills.put(ChatColor.GREEN + killer.getName(), kills.get(ChatColor.GREEN + killer.getName()) + 1);
						}
						respawn((Player) entity);
					}
				}
			}
		});
		// listen for when arrows hit the ground and remove them
		listenEvent(new EventListener<ProjectileHitEvent>() {
			
			@Override
			public void onEvent(ProjectileHitEvent event) {
				Projectile proj = event.getEntity();
				ProjectileSource projShooter = proj.getShooter();
				if (proj instanceof Arrow && projShooter instanceof Player && isAlive((Player) projShooter))
					proj.remove();
			}
		});
		// listen to when players die and respawn them
		listenEvent(new EventListener<PlayerDeathEvent>() {
			
			@Override
			public void onEvent(PlayerDeathEvent event) {
				Player player = event.getEntity();
				if (isAlive(player)) {
					announce("\u00a76" + player.getName() + " " + ChatColor.RED + event.getDeathMessage().replace(player.getName(), "").trim());
					event.getDrops().clear();
					event.setDeathMessage(null);
					event.setNewTotalExp(0);
					event.setDroppedExp(0);
					respawn(player);
				}
			}
		});
		// count down the timer until time runs out then end the game
		timer = new BukkitRunnable() {
			public void run() {
				updateScoreboard();
				if (time-- == 0) {
					timer.cancel();
					timer = null;
					UUID winner = null;
					int maxKills = -1;
					for (Map.Entry<String, Integer> entry : kills.entrySet()) {
						if (entry.getValue() > maxKills) {
							winner = getPlayer(entry.getKey().substring(2)).getUniqueId();
							maxKills = entry.getValue().intValue();
						}
					}
					titleAll(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + getPlayer(winner).getName() + " won!", null, 5, 20, 5);
					end();
				}
			}
		}.runTaskTimer(MinigameManager.getPlugin(), 0L, 20L);
	}
	
	@Override
	public void onEnd(int error) {
		if (timer != null)
			timer.cancel();
	}
	
	private void respawn(final Player player) {
		// make sure they don't get the death screen
		player.setHealth(player.getMaxHealth());
		// set them to dead
		setAlive(player, false);
		// give resistance and blindness
		player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 120, 10));
		player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 10));
		// tell them that they will respawn
		sendTitleMessage(player, "", ChatColor.RESET + "" + ChatColor.BOLD + "Respawning in 3 seconds...", 5, 50, 5);
		Bukkit.getScheduler().scheduleSyncDelayedTask(MinigameManager.getPlugin(), new Runnable() {
			public void run() {
				// set them back alive
				setAlive(player, true);
				// teleport them to a random location
				player.teleport(getStartingLocation(player));
				// give them the starting items
				player.getInventory().clear();
				player.getInventory().addItem(bow, axe, new ItemStack(Material.ARROW));
			}
		}, 60L);
	}
	
	private void updateScoreboard() {
		// build a scoreboard with a display name of "Kills" in red, listing the alive player names in green with their kills
		Scoreboard s = new ScoreboardBuilder("oitq" + getId() + "_kills", ChatColor.GOLD + "" + ChatColor.BOLD + time + ChatColor.YELLOW + ChatColor.BOLD + " seconds left").setLines(kills).build();
		// update the scoreboard for everybody
		setScoreboard(s);
	}
	
	@Override
	public Location getStartingLocation(Player player) {
		// random spawn
		return spawns[random.nextInt(spawns.length)];
	}
	
}
