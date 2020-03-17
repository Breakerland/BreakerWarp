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
	OpenGUI plugin;
	public EditGUI(BreakerWarp breakerWarp, OpenGUI openGUI) {
		this.main = breakerWarp;
		this.plugin = openGUI;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player)) {
			return false;
		}
		else {
	
			Player p = (Player) sender;
			if(!haveWarp(p.getUniqueId().toString())) {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("prefix")+" "+main.getConfig().getString("msg.err_noeditablewarp")));
				return false;
			}
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
		if(e.getCurrentItem() == null) {
			e.setCancelled(true);
			return;
		}
		if(e.getRawSlot()>53) {
			e.setCancelled(true);
			return;
		}
		else if(e.getRawSlot()==main.getConfig().getInt("items.name.place")) {
			
				if(e.getCurrentItem().getType().equals(Material.getMaterial(main.getConfig().getString("items.name.material")))) {
					e.setCancelled(true);			
					main.editTitle.put(p.getUniqueId().toString(), main.playerSpec.get(p.getUniqueId().toString()));
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("prefix")+" "+main.getConfig().getString("msg.howto").replace("%type%", "le titre")));
					p.closeInventory();
				}
			
		}
		else if(e.getRawSlot()==main.getConfig().getInt("items.price.place")) {
			
				if(e.getCurrentItem().getType().equals(Material.getMaterial(main.getConfig().getString("items.price.material")))) {
					e.setCancelled(true);			
					main.editPrice.put(p.getUniqueId().toString(), main.playerSpec.get(p.getUniqueId().toString()));
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("prefix")+" "+main.getConfig().getString("msg.howto").replace("%type%", "le prix")));
					p.closeInventory();
				}
			
		}
		else if(e.getRawSlot()==main.getConfig().getInt("items.description.place")) {
			
				if(e.getCurrentItem().getType().equals(Material.getMaterial(main.getConfig().getString("items.description.material")))) {
				e.setCancelled(true);			
				main.editDesc.put(p.getUniqueId().toString(), main.playerSpec.get(p.getUniqueId().toString()));
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("prefix")+" "+main.getConfig().getString("msg.howto").replace("%type%", "la description")));
				p.closeInventory();
				}
			
		}
		else if(e.getRawSlot()==main.getConfig().getInt("items.password.place")) {
			
				if(e.getCurrentItem().getType().equals(Material.getMaterial(main.getConfig().getString("items.password.material")))) {
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
					ItemStack validate = new ItemStack(Material.getMaterial(main.getConfig().getString("items.password.options.option1.material")));
					ItemMeta valmeta = validate.getItemMeta();
					valmeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("items.password.options.option1.title")));
					validate.setItemMeta(valmeta);
					ItemStack delete = new ItemStack(Material.getMaterial(main.getConfig().getString("items.password.options.option2.material")));
					ItemMeta delmeta = delete.getItemMeta();
					delmeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("items.password.options.option2.title")));
					delete.setItemMeta(delmeta);
					delInv.setItem(main.getConfig().getInt("items.option1_place"), validate);
					delInv.setItem(main.getConfig().getInt("items.option2_place"), delete);
				}
			
		}
		else if(e.getRawSlot()==main.getConfig().getInt("items.icon.place")) {
			
				if(e.getCurrentItem().getType().equals(Material.getMaterial(main.getConfig().getString("items.icon.material")))) {
					e.setCancelled(true);
					Integer warpid = main.playerSpec.get(p.getUniqueId().toString());
					Inventory itemGUI = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("gui.edit")));
					ItemStack help = new ItemStack(Material.getMaterial(main.getConfig().getString("tuto_edit.material")));
					ItemMeta helpmeta = help.getItemMeta();
					helpmeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("tuto_edit.title")));
					List<String> lore = new ArrayList<String>();
					lore.add(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("tuto_edit.line1")));
					lore.add(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("tuto_edit.line2")));
					helpmeta.setLore(lore);
					help.setItemMeta(helpmeta);
					itemGUI.setItem(main.getConfig().getInt("tuto_edit.place"), help);
					p.openInventory(itemGUI);
					main.editItem.put(p.getUniqueId().toString(), warpid);
				}
			
		}
		else if(e.getRawSlot()==main.getConfig().getInt("items.onoff_place")) {
			
			e.setCancelled(true);
			if(e.getCurrentItem().getType().equals(Material.getMaterial(main.getConfig().getString("items.i_off.material")))) {
				changeState(p.getUniqueId().toString(), main.playerSpec.get(p.getUniqueId().toString()), 0);
				ItemStack greenwool = new ItemStack(Material.getMaterial(main.getConfig().getString("items.i_on.material")));
				ItemMeta metagreen = greenwool.getItemMeta();
				metagreen.setDisplayName(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("items.i_on.title")));
				greenwool.setItemMeta(metagreen);
				ItemStack icon = e.getInventory().getItem(4);
				ItemMeta icometa = icon.getItemMeta();
				List<String> lore = new ArrayList<String>();
				lore.addAll(icometa.getLore());
				lore.remove(5);
				lore.add(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("lore.isoff")));
				icometa.setLore(lore);
				icon.setItemMeta(icometa);
				e.getInventory().setItem(main.getConfig().getInt("items.onoff_place"), greenwool);
				e.getInventory().setItem(4, icon);
			}
			else if(e.getCurrentItem().getType().equals(Material.getMaterial(main.getConfig().getString("items.i_on.material")))) {
				changeState(p.getUniqueId().toString(), main.playerSpec.get(p.getUniqueId().toString()), 1);
				ItemStack redwool = new ItemStack(Material.getMaterial(main.getConfig().getString("items.i_off.material")));
				ItemMeta metared = redwool.getItemMeta();
				metared.setDisplayName(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("items.i_off.title")));
				redwool.setItemMeta(metared);
				ItemStack icon = e.getInventory().getItem(4);
				ItemMeta icometa = icon.getItemMeta();
				List<String> lore = new ArrayList<String>();
				lore.addAll(icometa.getLore());
				lore.remove(5);
				if(plugin.isProtected(e.getInventory().getItem(4).getItemMeta().getDisplayName())) {
					lore.add(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("lore.isprotected")));
				}
				else {
					lore.add(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("lore.ison")));
				}
				icometa.setLore(lore);
				icon.setItemMeta(icometa);
				e.getInventory().setItem(main.getConfig().getInt("items.onoff_place"), redwool);
				e.getInventory().setItem(4, icon);
				}
			
		}
		else if(e.getRawSlot()==main.getConfig().getInt("items.delete.place")) {
			
				if(e.getCurrentItem().getType().equals(Material.getMaterial(main.getConfig().getString("items.delete.material")))) {
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
					ItemStack validate = new ItemStack(Material.getMaterial(main.getConfig().getString("items.delete.options.option1.material")));
					ItemMeta valmeta = validate.getItemMeta();
					valmeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("items.delete.options.option1.title")));
					validate.setItemMeta(valmeta);
					ItemStack refuse = new ItemStack(Material.getMaterial(main.getConfig().getString("items.delete.options.option2.material")), 1);
					ItemMeta refmeta = refuse.getItemMeta();
					refmeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("items.delete.options.option2.title")));
					refuse.setItemMeta(refmeta);
					delInv.setItem(main.getConfig().getInt("items.option1_place"), validate);
					delInv.setItem(main.getConfig().getInt("items.option2_place"), refuse);
				}
			
		}
		else if(e.getRawSlot() == main.getConfig().getInt("items.option2_place")) {
			
				if(e.getCurrentItem().getType().equals(Material.getMaterial(main.getConfig().getString("items.delete.options.option2.material")))) {
					String title = e.getInventory().getItem(4).getItemMeta().getDisplayName();
					Inventory specGUI = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("gui.edit")));
					getSpecWarp(specGUI, p.getUniqueId().toString(), title);
					p.openInventory(specGUI);
					main.playerSpec.put(p.getUniqueId().toString(), getWarpId(p.getUniqueId().toString(), title));
				}
				else if(e.getCurrentItem().getType().equals(Material.getMaterial(main.getConfig().getString("items.password.options.option2.material")))) {
					deletePassword(p.getUniqueId().toString(),main.playerSpec.get(p.getUniqueId().toString()));
					p.closeInventory();
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("prefix")+" "+main.getConfig().getString("msg.deleted_pass")));
				}
			
			
		}
		else if(e.getRawSlot()==main.getConfig().getInt("items.option1_place")) {
			
				if(e.getCurrentItem().getType().equals(Material.getMaterial(main.getConfig().getString("items.delete.options.option1.material")))) {
					p.closeInventory();
					p.performCommand("delbwarp "+e.getInventory().getItem(4).getItemMeta().getDisplayName());
				}
				else if(e.getCurrentItem().getType().equals(Material.getMaterial(main.getConfig().getString("items.password.options.option1.material")))) {
					e.setCancelled(true);			
					main.editPass.put(p.getUniqueId().toString(), main.playerSpec.get(p.getUniqueId().toString()));
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("prefix")+" "+main.getConfig().getString("msg.howto").replace("%type%", "le mot de passe")));
					p.closeInventory();
				}
			
			
		}
		else if(e.getRawSlot()==main.getConfig().getInt("items.categorie.place")) {
			
				if(e.getCurrentItem().getType().equals(Material.getMaterial(main.getConfig().getString("items.categorie.material")))) {
					e.setCancelled(true);
					Integer warpid = main.playerSpec.get(p.getUniqueId().toString());
					Inventory cateGUI = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("gui.edit")));
					Integer iterator = 1;
					while(main.getConfig().getString("categories.c_"+iterator+".name") != null) {
						ItemStack categorie = new ItemStack(Material.getMaterial(main.getConfig().getString("categories.c_"+iterator+".material")));
						ItemMeta catmeta = categorie.getItemMeta();
						catmeta.setDisplayName(ChatColor.WHITE+ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("categories.c_"+iterator+".name")));
						categorie.setItemMeta(catmeta);
						Integer place = main.getConfig().getInt("categories.c_"+iterator+".position")+9;
						cateGUI.setItem(place, categorie);
						
						iterator++;
					}
					p.openInventory(cateGUI);
					main.editCate.put(p.getUniqueId().toString(), warpid);
				}
			
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
					if(results.getString("password")==null) {
						lore.add(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("lore.ison")));
					}
					else {
						lore.add(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("lore.isprotected")));
					}
					
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
					if(results.getString("password")==null) {
						lore.add(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("lore.ison")));
					}
					else {
						lore.add(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("lore.isprotected")));
					}
					
				}
				else {
					lore.add(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("lore.isoff")));
				}
				itmeta.setLore(lore);
				item.setItemMeta(itmeta);
				i.setItem(4, item);
				
				///Edit name
				ItemStack tag = new ItemStack(Material.getMaterial(main.getConfig().getString("items.name.material")));
				ItemMeta tagmeta = tag.getItemMeta();
				tagmeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("items.name.title")));
				List<String> loretag = new ArrayList<String>();
				loretag.add(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("lore.max_name")));
				tagmeta.setLore(loretag);
				tag.setItemMeta(tagmeta);
				i.setItem(main.getConfig().getInt("items.name.place"), tag);
				
				///Edit price
				ItemStack nug = new ItemStack(Material.getMaterial(main.getConfig().getString("items.price.material")));
				ItemMeta nugmeta = nug.getItemMeta();
				nugmeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("items.price.title")));
				List<String> lorenug = new ArrayList<String>();
				lorenug.add(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("lore.max_price").replace("%price%", ""+main.getConfig().getInt("max_price")+main.getConfig().getString("type"))));
				nugmeta.setLore(lorenug);
				nug.setItemMeta(nugmeta);
				i.setItem(main.getConfig().getInt("items.price.place"), nug);
				
				///Edit description
				ItemStack sign = new ItemStack(Material.getMaterial(main.getConfig().getString("items.description.material")));
				ItemMeta signmeta = sign.getItemMeta();
				signmeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("items.description.title")));
				List<String> loresign = new ArrayList<String>();
				loresign.add(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("lore.max_desc")));
				signmeta.setLore(loresign);
				sign.setItemMeta(signmeta);
				i.setItem(main.getConfig().getInt("items.description.place"), sign);
				
				///Edit item
				ItemStack frame = new ItemStack(Material.getMaterial(main.getConfig().getString("items.icon.material")));
				ItemMeta frameta = frame.getItemMeta();
				frameta.setDisplayName(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("items.icon.title")));
				frame.setItemMeta(frameta);
				i.setItem(main.getConfig().getInt("items.icon.place"), frame);
				
				///Edit Categorie
				ItemStack lectern = new ItemStack(Material.getMaterial(main.getConfig().getString("items.categorie.material")));
				ItemMeta lecmeta = frame.getItemMeta();
				lecmeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("items.categorie.title")));
				lectern.setItemMeta(lecmeta);
				i.setItem(main.getConfig().getInt("items.categorie.place"), lectern);
				
				///Delete item
				ItemStack bin = new ItemStack(Material.getMaterial(main.getConfig().getString("items.delete.material")));
				ItemMeta binmeta = frame.getItemMeta();
				binmeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("items.delete.title")));
				bin.setItemMeta(binmeta);
				i.setItem(main.getConfig().getInt("items.delete.place"), bin);
				
				///Edit password
				ItemStack key = new ItemStack(Material.getMaterial(main.getConfig().getString("items.password.material")));
				ItemMeta keymeta = key.getItemMeta();
				keymeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("items.password.title")));
				key.setItemMeta(keymeta);
				i.setItem(main.getConfig().getInt("items.password.place"), key);
				///Edit on / off
				PreparedStatement onoffquery = main.getConnection().prepareStatement("SELECT `activate` FROM `warpdata` WHERE `uuid`='"+uuid+"' AND `title`='"+title+"'");
				ResultSet rs = onoffquery.executeQuery();
				if(rs.next()) {
					if(rs.getBoolean("activate")) {
						ItemStack redwool = new ItemStack(Material.getMaterial(main.getConfig().getString("items.i_off.material")));
						ItemMeta metared = redwool.getItemMeta();
						metared.setDisplayName(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("items.i_off.title")));
						redwool.setItemMeta(metared);
						i.setItem(main.getConfig().getInt("items.onoff_place"), redwool);
					}
					else {
						ItemStack greenwool = new ItemStack(Material.getMaterial(main.getConfig().getString("items.i_on.material")));
						ItemMeta metagreen = greenwool.getItemMeta();
						metagreen.setDisplayName(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("items.i_on.title")));
						greenwool.setItemMeta(metagreen);
						i.setItem(main.getConfig().getInt("items.onoff_place"), greenwool);
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
	public void deletePassword(String uuid, Integer warpid) {
		try {
			PreparedStatement statement = main.getConnection().prepareStatement("UPDATE `warpdata` SET `password`=null WHERE `uuid`='"+uuid+"' AND `warpid`='"+warpid+"'");
			statement.executeUpdate();
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public boolean haveWarp(String uuid) {
		try {
			PreparedStatement statement = main.getConnection().prepareStatement("SELECT COUNT(*) FROM `warpdata` WHERE `uuid`='"+uuid+"'");
			ResultSet result = statement.executeQuery();
			if(result.next()) {
				if(result.getInt("COUNT(*)")==0) {
					return false;
				}
				else {
					return true;
				}
			}
		}catch (SQLException e){
			e.printStackTrace();
		}
		return false;
	}
}