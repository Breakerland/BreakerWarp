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

public class EditDesc implements Listener {
	
	BreakerWarp main;
	public EditDesc(BreakerWarp breakerwarp) {
		this.main = breakerwarp;
	}
	
	

	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void awaitMessage(AsyncPlayerChatEvent e) {
		Player p = e.getPlayer();
		if(!main.editDesc.containsKey(p.getUniqueId().toString())) {
			return;
		}
		else {
			if(e.getMessage().length() > 128) {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("prefix")+" "+main.getConfig().getString("msg.err_lenght").replace("%type%", "description").replace("%l%", "128")));
				main.editDesc.remove(p.getUniqueId().toString());
				e.setCancelled(true);
				return;
			}
			else {
				
				editDesc(p.getUniqueId().toString(), main.editDesc.get(p.getUniqueId().toString()), e.getMessage().replace("'", "`"));
				main.editDesc.remove(p.getUniqueId().toString());
				e.setCancelled(true);
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("prefix")+" "+main.getConfig().getString("msg.desc_changed")));
			}
		}
	}
	
	public void editDesc(String uuid, Integer i, String desc) {
		try {
			PreparedStatement statement = main.getConnection().prepareStatement("UPDATE `warpdata` SET `description`='"+desc+"' WHERE `uuid`='"+uuid+"' AND `warpid`='"+i+"'");
			statement.executeUpdate();
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}


}
