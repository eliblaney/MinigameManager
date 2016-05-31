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

public class PagedMenu implements InventoryMenu {
	
	private final Inventory[] invs;
	private final ItemStack backItem, nextItem;
	
	public PagedMenu(final Minigame minigame, int pageRows, int pages, String title, final ItemStack backItem, final ItemStack nextItem, final EventListener<InventoryClickEvent> onClick) {
		Validate.notNull(minigame, "Minigame cannot be null");
		Validate.isTrue(pageRows > 0 && pageRows <= 9, "There must be between 0-9 rows, inclusive, per page");
		Validate.isTrue(pages > 0, "There must be at least 1 page");
		if (title == null)
			title = "";
		Validate.notNull(backItem, "The \"back\" item cannot be null");
		Validate.notNull(nextItem, "The \"next\" item cannot be null");
		this.invs = new Inventory[pages];
		this.backItem = backItem;
		this.nextItem = nextItem;
		for (int i = 0; i < pages; i++)
			this.invs[i] = Bukkit.createInventory(null, pageRows * 9, String.format(title, i + 1));
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
					for (int i = 0; i < invs.length; i++) {
						if (invs[i].equals(e.getClickedInventory())) {
							ItemStack item = e.getCurrentItem();
							Player player = (Player) human;
							if (backItem.equals(item)) {
								player.closeInventory();
								player.openInventory(invs[i - 1]);
							} else if (nextItem.equals(item)) {
								player.closeInventory();
								player.openInventory(invs[i + 1]);
							} else
								onClick.onEvent(e);
						}
					}
				}
			});
		}
	}
	
	public PagedMenu(Minigame minigame, String title, ItemStack backItem, ItemStack nextItem, EventListener<InventoryClickEvent> onClick, ItemStack... items) {
		this(minigame, Math.min(getSegmentsForSize(9, items.length), 54), getSegmentsForSize(45, items.length), title, backItem, nextItem, onClick);
		setItems(items);
	}
	
	@Override
	public void clear() {
		for (Inventory i : invs)
			i.clear();
		setNavigation();
	}
	
	private void setNavigation() {
		for (int i = 0; i < invs.length; i++) {
			Inventory inv = invs[i];
			int lastRowIndex = inv.getSize() - 9;
			/*
			 * Set back & next items in middle slots of last row
			 * 
			 * [0][1][2][3][4][5][6][7][8]
			 * __________^_____^__________
			 */
			if (i != 0)
				inv.setItem(lastRowIndex + 3, backItem);
			if (i != invs.length - 1)
				inv.setItem(lastRowIndex + 5, nextItem);
		}
	}
	
	@Override
	public void setItems(ItemStack... items) {
		clear();
		if (items == null || items.length == 0)
			return;
		int inv = 0, invIndex = 0;
		for (int i = 0; i < items.length; i++) {
			invs[inv].setItem(invIndex++, items[i]);
			// skip last row
			if (invIndex >= (invs[inv].getSize() - 9)) {
				++inv;
				invIndex = 0;
			}
		}
	}
	
	@Override
	public InventoryView open(Player player) {
		return player.openInventory(invs[0]);
	}
	
	public static int getSegmentsForSize(int segmentSize, int size) {
		int div = size / segmentSize;
		if (div == ((double) size / segmentSize))
			return div;
		return div + 1;
	}
	
}
