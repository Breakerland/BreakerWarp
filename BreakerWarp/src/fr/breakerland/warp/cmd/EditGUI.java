package fr.breakerland.warp.cmd;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

public class EditGUI implements CommandExecutor, Listener  {

	BreakerWarp main;
	public EditGUI(BreakerWarp breakerWarp) {
		this.main = breakerWarp;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player)) {
			return false;
		}
		else {
	
			Player p = (Player) sender;
			Inventory editGUI = Bukkit.createInventory(null, 9, ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("gui.personnal")));
			main.playerEdit.add(p.getUniqueId().toString());
			ItemStack item =new ItemStack(Material.WHITE_STAINED_GLASS_PANE,1);
			ItemMeta itmeta = item.getItemMeta();
			itmeta.setDisplayName(" ");
			item.setItemMeta(itmeta);
			Integer i = 0;
			while(i<9) {
				editGUI.setItem(i, item);
				i++;
			}
			getWarp(editGUI, p.getUniqueId().toString());
			p.openInventory(editGUI);
			
		}
		
		return false;
	}

	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void interractGUI(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		if(!main.playerEdit.contains(p.getUniqueId().toString())) {
			return;
		}
		e.setCancelled(true);
		if(e.getRawSlot()==2||e.getRawSlot()==4||e.getRawSlot()==6) {
			
			if(e.getCurrentItem().getType().equals(Material.WHITE_STAINED_GLASS_PANE)) {
				return;
			}
			else {
				String title = e.getCurrentItem().getItemMeta().getDisplayName();
				Inventory specGUI = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("gui.edit")));
				getSpecWarp(specGUI, p.getUniqueId().toString(), title);
				main.playerSpec.put(p.getUniqueId().toString(), getWarpId(p.getUniqueId().toString(), title));
				p.openInventory(specGUI);
			}
		}
		
		
	}
	
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void interractEditGUI(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		if(!main.playerSpec.containsKey(p.getUniqueId().toString())) {
			return;
		}
		if(e.getRawSlot()>53) {
			e.setCancelled(true);
			return;
		}
		else if(e.getRawSlot()==20) {
			e.setCancelled(true);			
			main.editTitle.put(p.getUniqueId().toString(), main.playerSpec.get(p.getUniqueId().toString()));
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("prefix")+" "+main.getConfig().getString("msg.howto").replace("%type%", "le titre")));
			p.closeInventory();
		}
		else if(e.getRawSlot()==22) {
			e.setCancelled(true);			
			main.editPrice.put(p.getUniqueId().toString(), main.playerSpec.get(p.getUniqueId().toString()));
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("prefix")+" "+main.getConfig().getString("msg.howto").replace("%type%", "le prix")));
			p.closeInventory();
		}
		else if(e.getRawSlot()==24) {
			e.setCancelled(true);			
			main.editDesc.put(p.getUniqueId().toString(), main.playerSpec.get(p.getUniqueId().toString()));
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("prefix")+" "+main.getConfig().getString("msg.howto").replace("%type%", "la description")));
			p.closeInventory();
		}
		else if(e.getRawSlot()==38) {
			e.setCancelled(true);
			Integer warpid = main.playerSpec.get(p.getUniqueId().toString());
			Inventory itemGUI = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("gui.edit")));
			ItemStack help = new ItemStack(Material.BOOK,1);
			ItemMeta helpmeta = help.getItemMeta();
			helpmeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("tuto_edit.title")));
			List<String> lore = new ArrayList<String>();
			lore.add(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("tuto_edit.line1")));
			lore.add(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("tuto_edit.line2")));
			helpmeta.setLore(lore);
			help.setItemMeta(helpmeta);
			itemGUI.setItem(13, help);
			p.openInventory(itemGUI);
			main.editItem.put(p.getUniqueId().toString(), warpid);
			}
		else if(e.getRawSlot()==42) {
			e.setCancelled(true);
			if(e.getCurrentItem().getType().equals(Material.RED_WOOL)) {
				changeState(p.getUniqueId().toString(), main.playerSpec.get(p.getUniqueId().toString()), 0);
				ItemStack greenwool = new ItemStack(Material.GREEN_WOOL,1);
				ItemMeta metagreen = greenwool.getItemMeta();
				metagreen.setDisplayName(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("items.wool_on")));
				greenwool.setItemMeta(metagreen);
				e.getInventory().setItem(42, greenwool);
			}
			else if(e.getCurrentItem().getType().equals(Material.GREEN_WOOL)) {
				changeState(p.getUniqueId().toString(), main.playerSpec.get(p.getUniqueId().toString()), 1);
				ItemStack redwool = new ItemStack(Material.RED_WOOL,1);
				ItemMeta metared = redwool.getItemMeta();
				metared.setDisplayName(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("items.wool_off")));
				redwool.setItemMeta(metared);
				e.getInventory().setItem(42, redwool);
			}
		}
		else if(e.getRawSlot()==40) {
			ItemStack icon = e.getInventory().getItem(4);
			ItemStack glass = e.getInventory().getItem(9);
			Inventory delInv = e.getInventory();
			delInv.clear();
			delInv.setItem(4, icon);
			Integer i = 9;
			while(i<18) {
				delInv.setItem(i, glass);
				i++;
			}
			ItemStack validate = new ItemStack(Material.GREEN_STAINED_GLASS_PANE, 1);
			ItemMeta valmeta = validate.getItemMeta();
			valmeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("items.validate")));
			validate.setItemMeta(valmeta);
			ItemStack refuse = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
			ItemMeta refmeta = refuse.getItemMeta();
			refmeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("items.refuse")));
			refuse.setItemMeta(refmeta);
			delInv.setItem(29, validate);
			delInv.setItem(33, refuse);
			
		}
		else if(e.getRawSlot() == 33) {
			String title = e.getInventory().getItem(4).getItemMeta().getDisplayName();
			Inventory specGUI = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("edit")));
			getSpecWarp(specGUI, p.getUniqueId().toString(), title);
			p.openInventory(specGUI);
			main.playerSpec.put(p.getUniqueId().toString(), getWarpId(p.getUniqueId().toString(), title));
		}
		else if(e.getRawSlot()==29) {
			p.closeInventory();
			p.performCommand("delbwarp "+e.getInventory().getItem(4).getItemMeta().getDisplayName());
		}
		else {
			e.setCancelled(true);
			return;
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void closeGUI(InventoryCloseEvent e) {
		Player p = (Player) e.getPlayer();
		if(main.playerEdit.contains(p.getUniqueId().toString())) {
			main.playerEdit.remove(p.getUniqueId().toString());
		}
		else if(main.playerSpec.containsKey(p.getUniqueId().toString())) {
			main.playerSpec.remove(p.getUniqueId().toString());
		}
		else {
			return;
		}
		
	}
	public void getWarp(Inventory i, String uuid) {
		try {
			PreparedStatement statement = main.getConnection().prepareStatement("SELECT * FROM `warpdata` WHERE `uuid`='"+uuid+"'");

			ResultSet results = statement.executeQuery();
			Integer place = 4;
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
				i.setItem(place, item);
				if(place == 4) {
					place = place -2;
				}
				else if(place == 2) {
					place = place +4;
				}
			}
			return;

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return;

	}
	public int getWarpId(String uuid, String title) {
		Integer id = -1;
		try {
			PreparedStatement statement = main.getConnection().prepareStatement("SELECT `warpid` FROM `warpdata` WHERE `uuid`='"+uuid+"' AND `title`='"+title+"'");
			ResultSet rs = statement.executeQuery();
			if(rs.next()) {
				id = rs.getInt("warpid");
				return id;
			}
			else {
				return id;
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return id;
	}
	
	public void getSpecWarp(Inventory i, String uuid, String title) {
		try {
			PreparedStatement statement = main.getConnection().prepareStatement("SELECT * FROM `warpdata` WHERE `uuid`='"+uuid+"' AND `title`='"+title+"'");
			ResultSet results = statement.executeQuery();
			if(results.next()) {
				///Decoration
				ItemStack glass =new ItemStack(Material.BLACK_STAINED_GLASS_PANE,1);
				ItemMeta glassmeta = glass.getItemMeta();
				glassmeta.setDisplayName(" ");
				glass.setItemMeta(glassmeta);
				Integer it = 9;
				while(it<18) {
					i.setItem(it, glass);
					it++;
				}
				
				///"Title item"
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
				i.setItem(4, item);
				
				///Edit name
				ItemStack tag = new ItemStack(Material.NAME_TAG);
				ItemMeta tagmeta = tag.getItemMeta();
				tagmeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("items.tag")));
				tag.setItemMeta(tagmeta);
				i.setItem(20, tag);
				
				///Edit price
				ItemStack nug = new ItemStack(Material.GOLD_NUGGET);
				ItemMeta nugmeta = nug.getItemMeta();
				nugmeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("items.nugget")));
				nug.setItemMeta(nugmeta);
				i.setItem(22, nug);
				
				///Edit description
				ItemStack sign = new ItemStack(Material.OAK_SIGN);
				ItemMeta signmeta = sign.getItemMeta();
				signmeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("items.sign")));
				sign.setItemMeta(signmeta);
				i.setItem(24, sign);
				
				///Edit item
				ItemStack frame = new ItemStack(Material.ITEM_FRAME);
				ItemMeta frameta = frame.getItemMeta();
				frameta.setDisplayName(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("items.frame")));
				frame.setItemMeta(frameta);
				i.setItem(38, frame);
				
				///Delete item
				ItemStack bin = new ItemStack(Material.COMPOSTER);
				ItemMeta binmeta = frame.getItemMeta();
				binmeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("items.composter")));
				bin.setItemMeta(binmeta);
				i.setItem(40, bin);
				
				///Edit on / off
				PreparedStatement onoffquery = main.getConnection().prepareStatement("SELECT `activate` FROM `warpdata` WHERE `uuid`='"+uuid+"' AND `title`='"+title+"'");
				ResultSet rs = onoffquery.executeQuery();
				if(rs.next()) {
					if(rs.getBoolean("activate")) {
						ItemStack redwool = new ItemStack(Material.RED_WOOL,1);
						ItemMeta metared = redwool.getItemMeta();
						metared.setDisplayName(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("items.wool_off")));
						redwool.setItemMeta(metared);
						i.setItem(42, redwool);
					}
					else {
						ItemStack greenwool = new ItemStack(Material.GREEN_WOOL,1);
						ItemMeta metagreen = greenwool.getItemMeta();
						metagreen.setDisplayName(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("items.wool_on")));
						greenwool.setItemMeta(metagreen);
						i.setItem(42, greenwool);
					}
				}
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void changeState(String uuid, Integer warpid, Integer choice) {
		try {
			PreparedStatement statement = main.getConnection().prepareStatement("UPDATE `warpdata` SET `activate`='"+choice+"' WHERE `uuid`='"+uuid+"' AND `warpid`='"+warpid+"'");
			statement.executeUpdate();
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
}