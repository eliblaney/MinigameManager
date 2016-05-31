package minigamemanager.api.inventory;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import minigamemanager.api.minigame.Minigame;
import minigamemanager.api.minigame.Minigame.EventListener;
import minigamemanager.core.MinigameManager;

public class BasicMenu implements InventoryMenu {
	
	private final int size;
	private final Inventory inv;
	
	public BasicMenu(Minigame minigame, int rows) {
		this(minigame, rows, null, null);
	}
	
	public BasicMenu(Minigame minigame, int rows, String title) {
		this(minigame, rows, title, null);
	}
	
	public BasicMenu(Minigame minigame, int rows, EventListener<InventoryClickEvent> onClick) {
		this(minigame, rows, null, onClick);
	}
	
	public BasicMenu(final Minigame minigame, int rows, String title, final EventListener<InventoryClickEvent> onClick) {
		Validate.notNull(minigame, "Minigame cannot be null");
		Validate.isTrue(rows >= 1 && rows <= 9, "Rows must be an integer between 1-9, inclusive");
		this.size = rows * 9;
		if (title == null)
			title = "";
		this.inv = Bukkit.createInventory(null, size, title);
		if (onClick != null) {
			minigame.listenEvent(new EventListener<InventoryInteractEvent>() {
				@Override
				public void onEvent(InventoryInteractEvent event) {
					HumanEntity human = event.getWhoClicked();
					if (!(human instanceof Player) || !minigame.isPlaying(((Player) human)))
						return;
					if (!(event instanceof InventoryClickEvent)) {
						event.setCancelled(true);
						return;
					}
					InventoryClickEvent e = (InventoryClickEvent) event;
					if (inv.equals(e.getClickedInventory()))
						onClick.onEvent(e);
				}
			});
		}
	}
	
	public Inventory getInventory() {
		return inv;
	}
	
	@Override
	public void clear() {
		inv.clear();
	}
	
	public void setItem(int index, ItemStack item) {
		Validate.isTrue(index >= 0 && index < size, "Index must be between 0-" + (size - 1) + " inclusive");
		inv.setItem(index, item);
	}
	
	@Override
	public void setItems(ItemStack... items) {
		clear();
		if(items == null || items.length == 0)
			return;
		if (items.length >= size)
			MinigameManager.getPlugin().getLogger().warning("Items array too large for inventory; truncating to " + size + " items");
		for (int i = 0; i < Math.min(items.length, size - 1); i++)
			inv.setItem(i, items[i]);
	}
	
	@Override
	public InventoryView open(Player player) {
		return player.openInventory(inv);
	}
	
	public static int getRowsForSize(int size) {
		Validate.isTrue(size >= 1 && size <= 54, "Size must be between 1-54, inclusive");
		return PagedMenu.getSegmentsForSize(9, size);
	}
	
}
