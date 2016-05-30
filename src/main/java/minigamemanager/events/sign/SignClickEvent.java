package minigamemanager.events.sign;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;

public class SignClickEvent extends Event {
	
	protected static final HandlerList handlers = new HandlerList();
	
	private final Player player;
	private final Action action;
	private final Sign sign;
	
	public SignClickEvent(Player player, Action action, Sign sign) {
		this.player = player;
		this.action = action;
		this.sign = sign;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public Action getAction() {
		return action;
	}
	
	public Location getLocation() {
		return sign.getLocation();
	}
	
	public Block getBlock() {
		return sign.getBlock();
	}
	
	public Sign getSign() {
		return sign;
	}
	
	public String[] getLines() {
		return sign.getLines();
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
}
