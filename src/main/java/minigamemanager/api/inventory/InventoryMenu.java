package minigamemanager.api.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public interface InventoryMenu {
	
	public void clear();
	
	public void setItems(ItemStack... items);
	
	public InventoryView open(Player player);
	
}
