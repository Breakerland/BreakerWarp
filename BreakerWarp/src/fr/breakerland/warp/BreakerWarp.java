package fr.breakerland.warp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import fr.breakerland.warp.cmd.DelWarp;
import fr.breakerland.warp.cmd.EditGUI;
import fr.breakerland.warp.cmd.OpenGUI;
import fr.breakerland.warp.cmd.SetWarp;
import fr.breakerland.warp.listener.EditDesc;
import fr.breakerland.warp.listener.EditItem;
import fr.breakerland.warp.listener.EditPrice;
import fr.breakerland.warp.listener.EditTitle;
import net.milkbowl.vault.economy.Economy;

public class BreakerWarp extends JavaPlugin {
	
	public List<String> playerOpen = new ArrayList<String>();
	public List<String> playerEdit = new ArrayList<String>();
	public HashMap<String,Integer> playerSpec = new HashMap<String,Integer>();
	public HashMap<String,Integer>editTitle = new HashMap<String,Integer>();
	public HashMap<String,Integer>editDesc = new HashMap<String,Integer>();
	public HashMap<String,Integer>editItem = new HashMap<String,Integer>();
	public HashMap<String,Integer>editDel = new HashMap<String,Integer>();
	public HashMap<String,Integer>editPrice = new HashMap<String,Integer>();
	public HashMap<String,Integer>grades = new HashMap<String,Integer>();;
	public Long timeout;
	
	public Economy economy = null;
	
	public String host,
	database,
	username,
	password;
	public int port;
	private Connection connection;

	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		
		setupGrades();
		mysqlSetup();
		initTable();	
		OpenGUI instance = new OpenGUI(this);
		getServer().getPluginManager().registerEvents(instance, this);
		getCommand("bwarp").setExecutor(instance);
		getCommand("setbwarp").setExecutor(new SetWarp(this));
		getCommand("delbwarp").setExecutor(new DelWarp(this));
		EditGUI editinstance = new EditGUI(this);
		getServer().getPluginManager().registerEvents(editinstance, this);
		getCommand("editbwarp").setExecutor(editinstance);
		getServer().getPluginManager().registerEvents(new EditTitle(this), this);
		getServer().getPluginManager().registerEvents(new EditDesc(this), this);
		getServer().getPluginManager().registerEvents(new EditItem(this), this);
		getServer().getPluginManager().registerEvents(new EditPrice(this), this);
	}
	
	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	
	public void mysqlSetup() {
		host = getConfig().getString("db.host");
		port = getConfig().getInt("db.port");
		database = getConfig().getString("db.database");
		username = getConfig().getString("db.username");
		password = getConfig().getString("db.password");
		timeout = System.currentTimeMillis();
		
		try {
			synchronized (this) {
				if (getConnection() != null && !getConnection().isClosed())
					return;
				Class.forName("com.mysql.jdbc.Driver");
				setConnection(DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password));
				getServer().getConsoleSender().sendMessage("§aSuccessfully connected to warp database");
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void initTable() {
		try (Statement statement = getConnection().createStatement()) {
			
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS `warpdata` "
					+ "(`uuid` VARCHAR(36) NOT NULL, "
					+ "`warpid` INT(11) NOT NULL AUTO_INCREMENT, "
					+ "`world` VARCHAR(36) NOT NULL, "
					+ "`x` DOUBLE NOT NULL, "
					+ "`y` DOUBLE NOT NULL, "
					+ "`z` DOUBLE NOT NULL, "
					+ "`yaw` FLOAT NOT NULL, "
					+ "`pitch` FLOAT NOT NULL, "
					+ "`title` VARCHAR(36) NOT NULL, "
					+ "`item` VARCHAR(36) NOT NULL, "
					+ "`description` VARCHAR(128), "
					+ "`visit` INT(11) NOT NULL, "
					+ "`price` DOUBLE NOT NULL, "
					+ "`activate` BOOLEAN NOT NULL DEFAULT TRUE, "
					+ "PRIMARY KEY (`warpid`))");
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	@SuppressWarnings("unchecked")
	public void setupGrades() {
		List<String> list = (List<String>) this.getConfig().get("perms");
		Integer taille = list.size();
		String listre = list.toString().replace("[", "").replace("]", "").replace(" ", "").replace("{", "").replace("}","");
		String[] liste2 = listre.split(",");
		Integer i = 0;
		
		while(i<taille) {
			String[] dif = liste2[i].split("=");
			Integer max = Integer.parseInt(dif[1]);
			this.grades.put(dif[0], max);
			i++;
		}
	}
	
	public boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return economy != null;
	}
	
	public Boolean testBalance(Player player, double montant) {
		Boolean b = false;
		if(setupEconomy()) {
			double balance = economy.getBalance(player);
			if(balance >= montant) {
				b = true;
			} else {
				b = false;
			}
		}
		return b;
	}

	
}
