package fr.breakerland.warp.listener;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import fr.breakerland.warp.BreakerWarp;

public class EditItem implements Listener {

	BreakerWarp main;
	public EditItem(BreakerWarp breakerWarp) {
		this.main = breakerWarp;
	}
	
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void interractEditItemGUI(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		if(!main.editItem.containsKey(p.getUniqueId().toString())) {
			return;
		}
		if(e.getRawSlot()<27) {
			e.setCancelled(true);
			return;
		}		
		else if(e.getCurrentItem() != null){
			e.setCancelled(true);
			if(!e.getCurrentItem().getType().isAir()) {
			changeItem(p.getUniqueId().toString(), main.editItem.get(p.getUniqueId().toString()), e.getCurrentItem().getType().toString());		
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("prefix")+" "+main.getConfig().getString("msg.item_changed")));
			}
			
		}
		else {
			return;
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void closeGUI(InventoryCloseEvent e) {
		Player p = (Player) e.getPlayer();
		if(!main.editItem.containsKey(p.getUniqueId().toString())) {
			return;
		}
		else {
			main.editItem.remove(p.getUniqueId().toString());
		}
	}
	
	public void changeItem(String uuid, Integer warpid, String item) {
		try {
			PreparedStatement statement = main.getConnection().prepareStatement("UPDATE `warpdata` SET `item`='"+item+"' WHERE `uuid`='"+uuid+"' AND `warpid`='"+warpid+"'");
			statement.executeUpdate();
			
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
