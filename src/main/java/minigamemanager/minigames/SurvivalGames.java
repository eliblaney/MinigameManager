package minigamemanager.minigames;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;

import minigamemanager.api.chest.ChestRandomizer;
import minigamemanager.api.inventory.SpectatorMenu;
import minigamemanager.api.items.ItemStackBuilder;
import minigamemanager.api.minigame.Minigame;
import minigamemanager.api.minigame.MinigameAttributes;
import minigamemanager.api.minigame.MinigameErrors;
import minigamemanager.api.minigame.MinigameType;
import minigamemanager.api.rotation.Rotation;
import minigamemanager.api.scoreboard.ScoreboardBuilder;
import minigamemanager.api.winner.SingleWinnerList;
import minigamemanager.core.MinigameManager;

@MinigameAttributes(name = "Survival Games", type = MinigameType.LAST_MAN_STANDING, authors = "DonkeyCore", isDefault = true)
public class SurvivalGames extends Minigame {
	
	private final ItemStack compass;
	private UUID thirdPlace, secondPlace;
	
	public SurvivalGames(Rotation r) {
		super(r, randomMap(SurvivalGames.class));
		compass = ItemStackBuilder.fromMaterial(Material.COMPASS).name("&7Spectator Compass").lore("Right-click to teleport to players").shiny().build();
	}
	
	@Override
	public void onStart() {
		// listen to when players die and respawn them
		listenEvent(new EventListener<PlayerDeathEvent>() {
			
			@Override
			public void onEvent(PlayerDeathEvent event) {
				Player player = event.getEntity();
				player.setHealth(player.getMaxHealth());
				if (isAlive(player)) {
					Player killer = player.getKiller();
					if (killer != null)
						announce(ChatColor.RED + player.getName() + ChatColor.RESET + " was killed by " + ChatColor.RED + killer.getName() + ChatColor.RESET + ".");
					else
						announce(ChatColor.GOLD + player.getName() + " " + ChatColor.RED + event.getDeathMessage().replace(player.getName(), "").trim());
					event.getDrops().clear();
					event.setDeathMessage(null);
					event.setNewTotalExp(0);
					setSpectator(player);
				}
			}
		});
		// spectators don't take damage
		listenEvent(new EventListener<EntityDamageEvent>() {
			
			@Override
			public void onEvent(EntityDamageEvent event) {
				if (!(event.getEntity() instanceof Player))
					return;
				Player player = (Player) event.getEntity();
				if (!isAlive(player))
					event.setCancelled(true);
			}
		});
		final Minigame minigame = this;
		// handle clicking with spectator compass
		listenEvent(new EventListener<PlayerInteractEvent>() {
			
			private final ItemStack backItem = ItemStackBuilder.fromMaterial(Material.COMPASS).name("Previous page").build();
			private final ItemStack nextItem = ItemStackBuilder.fromMaterial(Material.COMPASS).name("Next page").build();
			
			@Override
			public void onEvent(PlayerInteractEvent event) {
				Player player = event.getPlayer();
				ItemStack item = event.getItem();
				if (compass.equals(item)) {
					// alive players do not deserve the almighty compass
					if (isAlive(player))
						player.getInventory().remove(item);
					else
						new SpectatorMenu(minigame, getAliveNames(), "Spectate", backItem, nextItem).open(player);
				}
			};
		});
		// can't drop compass
		listenEvent(new EventListener<PlayerDropItemEvent>() {
			
			@Override
			public void onEvent(PlayerDropItemEvent event) {
				if (compass.equals(event.getItemDrop()))
					event.setCancelled(true);
			}
		});
		ChestRandomizer cr = new ChestRandomizer(this);
		cr.setItems(MinigameManager.getPlugin().getConfig().getConfigurationSection("minigames").getConfigurationSection("default-minigames").getConfigurationSection("defaults").getConfigurationSection(getName().replace(' ', '_')).getConfigurationSection("chests"));
		cr.fill();
		// regeneration for 90 seconds
		potionAll(new PotionEffect(PotionEffectType.REGENERATION, 71 * 20, 1));
		// show list of everybody alive
		updateScoreboard();
		// nobody is allowed to move
		lock();
		// in case spawn points are in the air, don't kick the players for flying
		applyAll(new PlayerConsumer() {
			
			@Override
			public void apply(Player player) {
				// in case players happen to be in the air, the server should not kick them for flying
				player.setAllowFlight(true);
			}
		});
		// start the countdown
		new Countdown(11, 0, new Consumer<Long>() {
			
			@Override
			public void apply(Long t) {
				if (t == 11)
					titleAll(ChatColor.GOLD + "Starting in", null, 0, 20, 5);
				else
					titleAll(ChatColor.GOLD + "" + ChatColor.BOLD + t, null, 0, 20, 5);
			}
		}, new Runnable() {
			
			@Override
			public void run() {
				applyAll(new PlayerConsumer() {
					
					@Override
					public void apply(Player player) {
						// return things to normal
						player.setAllowFlight(false);
					}
				});
				unlock();
			}
		}).start();
	}
	
	private void setSpectator(Player player) {
		setAlive(player, false);
		hide(player);
		thirdPlace = secondPlace;
		secondPlace = player.getUniqueId();
		// update the scoreboard without the player in the list
		updateScoreboard();
		// We have a winner!
		if (getAliveAmount() == 1)
			end(new SingleWinnerList(getAliveUUIDs()[0], secondPlace, thirdPlace));
		// We're just testing with a single person, everything seemed to work fine
		else if (getAliveAmount() == 0 && !MinigameManager.isRelease()) {
			announce(ChatColor.GREEN + "Finished!");
			end(null);
			// Just in case, end the game if there aren't any people left (though this case should never happen)
		} else if (getAliveAmount() < 1)
			end(MinigameErrors.NOT_ENOUGH_PLAYERS, null);
		else {
			// flying in adventure mode so that they can be constrained to only spectate certain players
			player.setGameMode(GameMode.ADVENTURE);
			player.setAllowFlight(true);
			for (PotionEffect pe : player.getActivePotionEffects())
				player.removePotionEffect(pe.getType());
			// empty inventory with a compass in middle of hotbar
			player.getInventory().clear();
			player.getInventory().setItem(5, compass);
		}
	}
	
	private void updateScoreboard() {
		// build a scoreboard with a display name of "Alive" in bold gold, listing the alive player names in green
		Scoreboard s = new ScoreboardBuilder("sg" + getId() + "_alive", "&6&lAlive").setLines(getAliveNamesWithColor(ChatColor.GREEN)).build();
		// update the scoreboard for everybody
		setScoreboard(s);
	}
	
	private int currentSpawn = 0;
	
	@Override
	public Location getStartingLocation(Player player) {
		// requires a unique spawn for each player
		return spawns[currentSpawn++];
	}
	
}
