package fr.breakerland.warp.cmd;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.breakerland.warp.BreakerWarp;

public class OpenGUI implements CommandExecutor, Listener {
	
	BreakerWarp main;
	public OpenGUI(BreakerWarp breakerWarp) {
		this.main = breakerWarp;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player)) {
			return false;
		}
		else {
			try {
				if(System.currentTimeMillis()+main.getConfig().getLong("timeout_hour")*3600000 > main.timeout || main.getConnection().isClosed() || main.getConnection() != null) {
					main.mysqlSetup();
				}
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
			Player p = (Player) sender;
			Inventory warpGUI = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("gui.list")));
			main.playerOpen.add(p.getUniqueId().toString());
			ItemStack item =new ItemStack(Material.BLACK_STAINED_GLASS_PANE,1);
			ItemMeta itmeta = item.getItemMeta();
			itmeta.setDisplayName(" ");
			item.setItemMeta(itmeta);
			Integer i = 0;
			while(i<9) {
				warpGUI.setItem(i, item);
				warpGUI.setItem(i+45, item);
				i++;
			}
			i=9;
			while(i<54) {
				warpGUI.setItem(i, item);
				warpGUI.setItem(i+8, item);
				i+=9;
			}
			getWarp(warpGUI);
			p.openInventory(warpGUI);
			
		}
		
		return false;
	}
	
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void interractGUI(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		if(!main.playerOpen.contains(p.getUniqueId().toString())) {
			return;
		}
		if(e.getRawSlot() > 53) {
			e.setCancelled(true);
		}
		else if(e.getCurrentItem() != null){
			e.setCancelled(true);
			if(e.getCurrentItem().getItemMeta().hasLore()) {
				if(isActive(e.getCurrentItem().getItemMeta().getDisplayName())) {
					if(!main.testBalance(p, getPrice(e.getCurrentItem().getItemMeta().getDisplayName()))) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("prefix")+" "+main.getConfig().getString("msg.err_eco").replace("%price%", getPrice(e.getCurrentItem().getItemMeta().getDisplayName())+main.getConfig().getString("type"))));
						e.setCancelled(true);
						return;
					}
					else {
						Double price = getPrice(e.getCurrentItem().getItemMeta().getDisplayName());
						if(price>0.00) {
							main.economy.withdrawPlayer(p, price);
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("prefix")+" "+main.getConfig().getString("msg.paid").replace("%price%", price+main.getConfig().getString("type"))));
							main.economy.depositPlayer(Bukkit.getOfflinePlayer(UUID.fromString(getOwner(e.getCurrentItem().getItemMeta().getDisplayName()))), price);
							if(Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(UUID.fromString(getOwner(e.getCurrentItem().getItemMeta().getDisplayName()))))) {
								Bukkit.getPlayer(UUID.fromString(getOwner(e.getCurrentItem().getItemMeta().getDisplayName()))).sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("prefix")+" "+main.getConfig().getString("msg.received").replace("%price%", price+main.getConfig().getString("type"))));
							}
						}
						teleportPlayer(p, e.getCurrentItem().getItemMeta().getDisplayName());
					}
					
				}
				else if(!isActive(e.getCurrentItem().getItemMeta().getDisplayName()) &&p.hasPermission("group.modérateur")) {
					Double price = getPrice(e.getCurrentItem().getItemMeta().getDisplayName());
					if(price>0.00) {
						main.economy.withdrawPlayer(p, price);
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("prefix")+" "+main.getConfig().getString("msg.paid").replace("%price%", price+main.getConfig().getString("type"))));
						main.economy.depositPlayer(Bukkit.getOfflinePlayer(UUID.fromString(getOwner(e.getCurrentItem().getItemMeta().getDisplayName()))), price);
						if(Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(UUID.fromString(getOwner(e.getCurrentItem().getItemMeta().getDisplayName()))))) {
							Bukkit.getPlayer(UUID.fromString(getOwner(e.getCurrentItem().getItemMeta().getDisplayName()))).sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("prefix")+" "+main.getConfig().getString("msg.received").replace("%price%", price+main.getConfig().getString("type"))));
						}
					}
					teleportPlayer(p, e.getCurrentItem().getItemMeta().getDisplayName());
				}
				
				else {
					e.setCancelled(true);
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("prefix")+" "+main.getConfig().getString("msg.err_desac")));
					return;
				}
			}
			
		}
		return;
	}
	
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void closeGUI(InventoryCloseEvent e) {
		Player p = (Player) e.getPlayer();
		if(!main.playerOpen.contains(p.getUniqueId().toString())) {
			return;
		}
		main.playerOpen.remove(p.getUniqueId().toString());
	}
	
	public void getWarp(Inventory i) {
		try {
			PreparedStatement statement = main.getConnection().prepareStatement("SELECT * FROM `warpdata` ORDER BY `visit` DESC");

			ResultSet results = statement.executeQuery();
			while (results.next()) {
				ItemStack item = new ItemStack(Material.getMaterial(results.getString("item")),1);
				ItemMeta itmeta = item.getItemMeta();
				itmeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', results.getString("title")));
				List<String> lore = new ArrayList<String>();
				lore.add("§6§l-------------");
				if(results.getString("description") != null) {
					lore.add(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("lore.description"))+" "+ChatColor.translateAlternateColorCodes('&',results.getString("description")));
				}
				else {
					lore.add(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("lore.description"))+" ");
				}
				lore.add(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("lore.owner"))+" "+Bukkit.getOfflinePlayer(UUID.fromString(results.getString("uuid"))).getName());
				lore.add(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("lore.price"))+" "+results.getDouble("price")+main.getConfig().getString("type"));
				lore.add(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("lore.visit"))+" "+results.getInt("visit"));
				if(results.getBoolean("activate")) {
					lore.add(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("lore.ison")));
				}
				else {
					lore.add(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("lore.isoff")));
				}
				itmeta.setLore(lore);
				item.setItemMeta(itmeta);
				i.addItem(item);
			}
			return;

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return;

	}
	
	public void teleportPlayer(Player p, String warp) {
		try {
			PreparedStatement statement = main.getConnection().prepareStatement("SELECT * FROM `warpdata` WHERE `title`='"+warp+"'");
			ResultSet results = statement.executeQuery();
			
			if(results.next()) {
				
				///TELEPORT THE PLAYER
				Location warploc = new Location(Bukkit.getWorld(results.getString("world")), results.getDouble("x"), results.getDouble("y"), results.getDouble("z"), results.getFloat("yaw"), results.getFloat("pitch"));
				p.teleport(warploc);
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("prefix")+" "+main.getConfig().getString("msg.teleport").replace("%player%", Bukkit.getOfflinePlayer(UUID.fromString(results.getString("uuid"))).getName())));
				
				///UPDATE NUMBER OF VISIT
				if(!p.getUniqueId().toString().equalsIgnoreCase(getOwner(warp))) {
					Integer i = results.getInt("visit")+1;
					PreparedStatement statementt = main.getConnection().prepareStatement("UPDATE `warpdata` SET `visit`='"+i+"' WHERE `title`='" +warp+ "'");
					statementt.executeUpdate();
				}
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return;
	}
	
	public boolean isActive(String warp) {
		try {
			PreparedStatement statement = main.getConnection().prepareStatement("SELECT `activate` FROM `warpdata` WHERE `title`='"+warp+"'");
			ResultSet results = statement.executeQuery();
			if(results.next()) {
				if(results.getBoolean("activate")) {
					return true;
				}
				else {
					return false;
				}
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	public double getPrice(String warp) {
		Double price = 0.0;
		try {
			PreparedStatement statement = main.getConnection().prepareStatement("SELECT `price` FROM `warpdata` WHERE `title`='"+warp+"'");
			ResultSet results = statement.executeQuery();
			if(results.next()) {
				price = results.getDouble("price");
				return price;
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return price;
	
	}
	
	public String getOwner(String warp) {
		try {
			PreparedStatement statement = main.getConnection().prepareStatement("SELECT `uuid` FROM `warpdata` WHERE `title`='"+warp+"'");
			ResultSet results = statement.executeQuery();
			if(results.next()) {
				String owner = results.getString("uuid");
				return owner;
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
