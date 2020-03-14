package fr.breakerland.warp.cmd;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.breakerland.warp.BreakerWarp;

public class DelWarp implements CommandExecutor {

	BreakerWarp main;
	public DelWarp(BreakerWarp breakerWarp) {
		this.main = breakerWarp;
	}

	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player)) {
			return false;
		}
		Player p = (Player) sender;
		Integer i = 0;
		String title = "";
		while (i<args.length) {
			if(i== 0) {
				title = args[i];
				i++;
			}
			else {
			 title = title+" "+args[i];
			 i++;
			}
		}
		if(!checkTitle(p.getUniqueId().toString(), title)) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("prefix")+" "+main.getConfig().getString("msg.err_nowarp")));
			return false;
		}
		else {
			delWarp(p.getUniqueId().toString(), title);
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("prefix")+" "+main.getConfig().getString("msg.deleted").replace("%warp%", title)));
		}
		return false;
	}

	public boolean checkTitle(String uuid, String title) {
		try {
			PreparedStatement statement = main.getConnection().prepareStatement("SELECT * FROM `warpdata` WHERE `uuid`='"+uuid+"' AND `title`='" + title + "'");

			ResultSet results = statement.executeQuery();
			if (results.next())
				return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	public void delWarp(String uuid, String title) {
			try (Statement statement = main.getConnection().createStatement()) {
				statement.executeUpdate("DELETE FROM `warpdata` WHERE `uuid`='"+uuid+"' AND `title`='" + title + "'");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		
	}
}
