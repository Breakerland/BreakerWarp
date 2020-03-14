package fr.breakerland.warp.listener;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import fr.breakerland.warp.BreakerWarp;

public class EditTitle implements Listener {
	
	BreakerWarp main;
	public EditTitle(BreakerWarp breakerwarp) {
		this.main = breakerwarp;
	}
	
	

	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void awaitMessage(AsyncPlayerChatEvent e) {
		Player p = e.getPlayer();
		if(!main.editTitle.containsKey(p.getUniqueId().toString())) {
			return;
		}
		else {
			if(e.getMessage().length() > 36) {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("prefix")+" "+main.getConfig().getString("msg.err_lenght").replace("%type%", "titre").replace("%l%", "36")));
				main.editTitle.remove(p.getUniqueId().toString());
				return;
			}
			else {
				if(checkTitle(e.getMessage())) {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("prefix")+" "+main.getConfig().getString("msg.err_title")));
					main.editTitle.remove(p.getUniqueId().toString());
					return;
				}
				editTitle(p.getUniqueId().toString(), main.editTitle.get(p.getUniqueId().toString()), e.getMessage().replace("'", "`"));
				main.editTitle.remove(p.getUniqueId().toString());
				e.setCancelled(true);
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("prefix")+" "+main.getConfig().getString("msg.title_changed").replace("%warp%",e.getMessage())));
			}
		}
		return;
	}
	
	public void editTitle(String uuid, Integer i, String title) {
		try {
			PreparedStatement statement = main.getConnection().prepareStatement("UPDATE `warpdata` SET `title`='"+title+"' WHERE `uuid`='"+uuid+"' AND `warpid`='"+i+"'");
			statement.executeUpdate();
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public boolean checkTitle(String title) {
		try {
			PreparedStatement statement = main.getConnection().prepareStatement("SELECT * FROM `warpdata` WHERE `title`='" + title + "'");

			ResultSet results = statement.executeQuery();
			if (results.next())
				return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

}
