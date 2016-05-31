package minigamemanager.api.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import minigamemanager.api.items.ItemStackBuilder;
import minigamemanager.api.minigame.Minigame;
import minigamemanager.api.minigame.Minigame.EventListener;

public class SpectatorMenu extends PagedMenu {
	
	public SpectatorMenu(final Minigame minigame, String[] alive, String title, ItemStack backItem, ItemStack nextItem) {
		super(minigame, title, backItem, nextItem, new EventListener<InventoryClickEvent>() {
			public void onEvent(InventoryClickEvent event) {
				ItemStack item = event.getCurrentItem();
				if (item != null && item.getType() == Material.SKULL_ITEM) {
					HumanEntity human = event.getWhoClicked();
					// only works for spectators
					if (!(human instanceof Player) || minigame.isAlive(((Player) human)))
						return;
					SkullMeta meta = (SkullMeta) item.getItemMeta();
					Player target = Bukkit.getPlayer(meta.getOwner());
					human.teleport(target);
				}
			};
		}, getHeadsForPlayers(alive));
	}
	
	public static ItemStack[] getHeadsForPlayers(String... players) {
		ItemStack[] items = new ItemStack[players.length];
		for (int i = 0; i < items.length; i++) {
			ItemStack item = ItemStackBuilder.fromMaterial(Material.SKULL_ITEM).name("&6" + players[i]).build();
			SkullMeta im = (SkullMeta) item.getItemMeta();
			im.setOwner(players[i]);
			item.setItemMeta(im);
			items[i] = item;
		}
		return items;
	}
	
}
