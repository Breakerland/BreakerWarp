package fr.breakerland.warp.cmd;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.breakerland.warp.BreakerWarp;

public class SetWarp implements CommandExecutor {
	
	
	
	BreakerWarp main;
	public SetWarp(BreakerWarp breakerWarp) {
		this.main = breakerWarp;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player)) {
			return false;
		}
		
		Player p = (Player) sender;
		if(p.getItemInHand().getType().equals(Material.AIR)) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("prefix")+" "+main.getConfig().getString("msg.noitem")));
			return false;
		}
		if(args.length == 0) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("prefix")+" "+main.getConfig().getString("msg.nodesc")));
			return false;
		}
		Integer perm = 0;
		for(String grades : main.grades.keySet()) {
			
			if(p.hasPermission("group."+grades)) {
				perm = main.grades.get(grades);
				
				
			}
		}
		
		if(!countWarp(p.getUniqueId().toString(), perm)) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("prefix")+" "+main.getConfig().getString("msg.max_warp")));
			return false;
		}

		Double x = p.getLocation().getX();
		Double y = p.getLocation().getY();
		Double z = p.getLocation().getZ();
		Float yaw = p.getLocation().getYaw();
		Float pitch = p.getLocation().getPitch();
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
		title = title.replace("'", "`");
		if(title.length() > 36) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("prefix")+" "+main.getConfig().getString("msg.err_lenght").replace("%type%", "titre").replace("%l%", "36")));
			return false;
		}
		if(checkTitle(title)) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("prefix")+" "+main.getConfig().getString("msg.err_title")));
			return false;
		}
		
		Double price = main.getConfig().getDouble("create_price");
		if(!main.testBalance(p, price)) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("prefix")+" "+main.getConfig().getString("msg.err_eco").replace("%price%", price.toString()+main.getConfig().getString("type"))));
			return false;
		}
		main.economy.withdrawPlayer(p, price);
		String item = p.getItemInHand().getType().toString();		
		createWarp(p.getUniqueId().toString(),p.getWorld().getName(),x,y,z,yaw,pitch,title,item);
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("prefix")+" "+main.getConfig().getString("msg.create").replace("%warp%", title).replace("%price%", price.toString()+main.getConfig().getString("type"))));
		return false;
	}
	
	public void createWarp(String uuid,String world, Double x, Double y, Double z, Float yaw, Float pitch, String title, String item) {
		
			try (Statement statement = main.getConnection().createStatement()) {
				statement.executeUpdate("INSERT INTO `warpdata` (uuid,world,x,y,z,yaw,pitch,title,item,price,visit,categorie) "
						+ "VALUES ('"+uuid+"','"+world+"','"+x+"','"+y+"','"+z+"','"+yaw+"','"+pitch+"','"+title+"','"+item+"','0','0','0')");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		
	}
	
	public boolean checkTitle(String title) {
		try {
			PreparedStatement statement = main.getConnection().prepareStatement("SELECT * FROM `warpdata` WHERE `title`='" + title + "'");

			ResultSet results = statement.executeQuery();
			if (results.next()) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean countWarp(String uuid, Integer perm) {
		try (Statement statement = main.getConnection().createStatement()){
			ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM `warpdata` WHERE `uuid`='"+uuid+"'");
			if(result.next()) {
				Integer numb = result.getInt("COUNT(*)");
				if(numb>=perm || numb==3) {
					return false;
				}
				else {
					return true;
				}
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	

}
