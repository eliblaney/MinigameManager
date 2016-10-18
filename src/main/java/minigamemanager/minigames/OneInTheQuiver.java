package minigamemanager.minigames;

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

import minigamemanager.api.achievement.Achievement;
import minigamemanager.api.items.ItemStackBuilder;
import minigamemanager.api.minigame.Minigame;
import minigamemanager.api.minigame.MinigameAttributes;
import minigamemanager.api.minigame.MinigameType;
import minigamemanager.api.profile.PlayerProfile;
import minigamemanager.api.rotation.Rotation;
import minigamemanager.api.scoreboard.ScoreboardHelper;
import minigamemanager.api.scoreboard.ScoreboardBuilder;
import minigamemanager.api.winner.SingleWinnerList;
import minigamemanager.core.MinigameManager;

@MinigameAttributes(name = "One_In_The_Quiver", type = MinigameType.MOST_POINTS, authors = "DonkeyCore", alwaysSaturated = true, canDropItems = false, canPickUpItems = false, isDefault = true)
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
	 * Scoreboard helper to help with the timer
	 */
	private ScoreboardHelper scoreboardHelper;
	/**
	 * Time left (default 5 minutes)
	 */
	private int time = 5 * 60;
	
	/**
	 * First blood achievement
	 */
	public static final Achievement BLOOD = new Achievement(OneInTheQuiver.class, "First blood!", "Be the first one to get a kill!", Material.SNOW_BALL);
	
	/**
	 * Whether the first blood achievement is still available
	 */
	private boolean firstBlood = true;
	
	public OneInTheQuiver(Rotation r) {
		super(r, randomMap(OneInTheQuiver.class));
		kills = new HashMap<String, Integer>();
	}
	
	public static void onRegister(MinigameManager manager) {
		manager.registerAchievements(BLOOD);
	}
	
	@Override
	public void onStart() {
		armorAll(new ItemStack(Material.CHAINMAIL_HELMET), new ItemStack(Material.CHAINMAIL_CHESTPLATE), new ItemStack(Material.CHAINMAIL_LEGGINGS), new ItemStack(Material.CHAINMAIL_BOOTS));
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
		// listen to when the player takes damage
		listenEvent(new EventListener<EntityDamageByEntityEvent>() {
			
			@Override
			public void onEvent(EntityDamageByEntityEvent event) {
				// filter only those who are alive
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
							if(firstBlood) {
								PlayerProfile.getPlayerProfile(killer.getUniqueId()).giveAchievement(BLOOD);
								firstBlood = false;
							}
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
					Player killer = player.getKiller();
					if (killer != null) {
						announce(ChatColor.RED + player.getName() + ChatColor.RESET + " was killed by " + ChatColor.RED + killer.getName() + ChatColor.RESET + ".");
						killer.getInventory().addItem(new ItemStack(Material.ARROW));
						if(firstBlood) {
							PlayerProfile.getPlayerProfile(killer.getUniqueId()).giveAchievement(BLOOD);
							firstBlood = false;
						}
						kills.put(ChatColor.GREEN + killer.getName(), kills.get(ChatColor.GREEN + killer.getName()) + 1);
					} else
						announce(ChatColor.GOLD + player.getName() + " " + ChatColor.RED + event.getDeathMessage().replace(player.getName(), "").trim());
					event.getDrops().clear();
					event.setDeathMessage(null);
					event.setNewTotalExp(0);
					event.setDroppedExp(0);
					respawn(player);
				}
			}
		});
		// build a scoreboard with a display name of "Kills" in red, listing the alive player names in green with their kills
		ScoreboardBuilder sb = new ScoreboardBuilder("oitq" + getId() + "_kills", ChatColor.GOLD + "" + ChatColor.BOLD + time + ChatColor.YELLOW + ChatColor.BOLD + " seconds left").setLines(kills);
		// we don't actually need to complete the build, we just need the helper to set the update interval
		scoreboardHelper = sb.getAdvanced();
		// all the players get updates
		scoreboardHelper.setRecipients(getPlayers());
		// every 20 ticks (1 second), drop the timer down by 1 and end the game if it reaches 0
		scoreboardHelper.setUpdateInterval(20, new Runnable() {
			public void run() {
				scoreboardHelper.getObjective().setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + time + ChatColor.YELLOW + ChatColor.BOLD + " seconds left");
				scoreboardHelper.setLines(kills);
				if (time-- == 0) {
					scoreboardHelper.stopUpdating();
					UUID first = null, second = null, third = null;
					// get first place and remove it from the list
					first = getTop();
					kills.remove(ChatColor.GREEN + Bukkit.getPlayer(first).getName());
					// get second place if possible and remove it from the list
					if (kills.size() > 0) {
						second = getTop();
						kills.remove(ChatColor.GREEN + Bukkit.getPlayer(second).getName());
					}
					// get third place if possible and remove it from the list
					if (kills.size() > 0)
						third = getTop();
					// end the minigame!
					end(new SingleWinnerList(first, second, third));
				}
			}
		});
	}
	
	private UUID getTop() {
		UUID top = null;
		int maxKills = -1;
		for (Map.Entry<String, Integer> entry : kills.entrySet()) {
			if (entry.getValue() > maxKills) {
				top = getPlayer(entry.getKey().substring(2)).getUniqueId();
				maxKills = entry.getValue().intValue();
			}
		}
		return top;
	}
	
	@Override
	public void onEnd(int error) {
		if (scoreboardHelper != null)
			scoreboardHelper.stopUpdating();
	}
	
	private void respawn(final Player player) {
		// make sure they don't get the death screen
		player.setHealth(player.getMaxHealth());
		// set them to dead
		setAlive(player, false);
		// reset all potion effects (to prevent a bug where resistance could not be applied if killed too quickly)
		for (PotionEffect pe : player.getActivePotionEffects())
			player.removePotionEffect(pe.getType());
		// give resistance and blindness
		player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 120, 10));
		player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 10));
		// tell them that they will respawn
		sendTitleMessage(player, "", ChatColor.RESET + "" + ChatColor.BOLD + "Respawning in 3 seconds...", 5, 50, 5);
		Bukkit.getScheduler().scheduleSyncDelayedTask(MinigameManager.getPlugin(), new Runnable() {
			public void run() {
				if (isPlaying(player)) {
					// set them back alive
					setAlive(player, true);
					// teleport them to a random location
					player.teleport(getStartingLocation(player));
					// give them the starting items
					player.getInventory().clear();
					player.getInventory().addItem(bow, axe, new ItemStack(Material.ARROW));
				}
			}
		}, 60L);
	}
	
	@Override
	public Location getStartingLocation(Player player) {
		// random spawn
		return spawns[random.nextInt(spawns.length)];
	}
	
}
