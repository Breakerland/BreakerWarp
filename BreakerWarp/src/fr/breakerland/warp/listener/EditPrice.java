package fr.breakerland.warp.listener;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import fr.breakerland.warp.BreakerWarp;

public class EditPrice implements Listener {
	
	BreakerWarp main;
	public EditPrice(BreakerWarp breakerwarp) {
		this.main = breakerwarp;
	}
	
	

	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void awaitMessage(AsyncPlayerChatEvent e) {
		Player p = e.getPlayer();
		if(!main.editPrice.containsKey(p.getUniqueId().toString())) {
			return;
		}
		else {
			try {
				Double msg = Double.valueOf(e.getMessage());
				if(msg>Double.valueOf(main.getConfig().getInt("max_price"))){
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("prefix")+" "+main.getConfig().getString("msg.err_max_price").replace("%price%", main.getConfig().getInt("max_price")+main.getConfig().getString("type"))));
					e.setCancelled(true);
					main.editPrice.remove(p.getUniqueId().toString());
					return;
				}
				else {
					//Double msg = Double.parseDouble(e.getMessage());
					editPrice(p.getUniqueId().toString(), main.editPrice.get(p.getUniqueId().toString()), msg);
					main.editPrice.remove(p.getUniqueId().toString());
					e.setCancelled(true);
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("prefix")+" "+main.getConfig().getString("msg.price_changed")));
				}
			}catch (NumberFormatException nfe) {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("prefix")+" "+main.getConfig().getString("msg.err_type").replace("%type%", "prix")));
				main.editPrice.remove(p.getUniqueId().toString());
				e.setCancelled(true);
				return;
			}
			
			
		
		}
	}
	
	public void editPrice(String uuid, Integer i, Double price) {
		try {
			PreparedStatement statement = main.getConnection().prepareStatement("UPDATE `warpdata` SET `price`='"+price+"' WHERE `uuid`='"+uuid+"' AND `warpid`='"+i+"'");
			statement.executeUpdate();
			main.getServer().getConsoleSender().sendMessage("oui");
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}


}