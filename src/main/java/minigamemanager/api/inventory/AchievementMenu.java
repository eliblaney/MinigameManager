package minigamemanager.api.inventory;

import java.io.ObjectInputStream.GetField;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import minigamemanager.api.achievement.Achievement;
import minigamemanager.api.items.ItemStackBuilder;
import minigamemanager.api.minigame.Minigame.EventListener;
import minigamemanager.api.minigame.MinigameShell.EmptyMinigameShell;
import minigamemanager.api.profile.PlayerProfile;
import minigamemanager.core.MinigameManager;

/**
 * A menu that shows all the achievements and the ones that the player has achieved
 * 
 * @author DonkeyCore
 */
public class AchievementMenu extends PagedMenu {
	
	private static final String title = "Achievements";
	private static final ItemStack backItem = ItemStackBuilder.fromMaterial(Material.PAPER).name("Previous page").build();
	private static final ItemStack nextItem = ItemStackBuilder.fromMaterial(Material.PAPER).name("Next page").build();
	
	public AchievementMenu(final PlayerProfile player) {
		super(new EmptyMinigameShell(), title, backItem, nextItem, null, getAchievementItems(player));
		MinigameManager.getMinigameManager().addListener(getShell(), InventoryCloseEvent.class, new EventListener<InventoryCloseEvent>() {

			@Override
			public void onEvent(InventoryCloseEvent event) {
				if(event.getPlayer().getUniqueId().equals(player.getUUID()) && event.getInventory().getTitle().equals(title))
					discard(); // free resources after player closes inventory
			}
			
		});
	}
	
	private static ItemStack[] getAchievementItems(PlayerProfile player) {
		// TODO: Sort based on parent minigame into a nice format
		Achievement[] as = MinigameManager.getMinigameManager().getAchievements();
		ItemStack[] items = new ItemStack[as.length];
		int i = 0;
		for(Achievement a : as) {
			ItemStack item = a.getItem().clone();
			if(player.hasAchievement(a)) {
				ItemMeta im = item.getItemMeta();
				im.getLore().add(0, ChatColor.GREEN + "Completed!");
				item.setItemMeta(im);
			}
			items[i++] = item;
		}
		return items;
	}
	
}
