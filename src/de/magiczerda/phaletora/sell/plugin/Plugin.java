package de.magiczerda.phaletora.sell.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import de.magiczerda.phaletora.sell.inventories.QuicksellInventory;
import de.magiczerda.phaletora.sell.utils.ConfirmBook;
import net.Indyuce.mmoitems.MMOItems;
import net.md_5.bungee.api.ChatColor;

public class Plugin extends JavaPlugin {
	
	/*
	 * The value of each MMOItem (which has to be accessed
	 * via its display name) is saved in this HashMap.
	 */
	
	public Map<String, ItemStack> values;
	
	/*
	 * We have to save the quicksell inventory
	 * while the player is looking at it.
	 */
	
	public Map<Player, Inventory> inventories;
	
	/*
	 * This list contains the names (not IDs) of all
	 * MMOItems that can be sold via the quicksell menu.
	 */
	
	public List<String> marketableItems;
	
	private QuicksellInventory quicksellInv;
	
	@Override
	public void onEnable() {
		initConfig();
		
		//create the confirm book
		new ConfirmBook();
		
		values = new HashMap<String, ItemStack>();
		inventories = new HashMap<Player, Inventory>();
		marketableItems = new ArrayList<String>();
		
		quicksellInv = new QuicksellInventory(this);
		
		readConfig();
		
		//register the inventory-click listener
		this.getServer().getPluginManager().registerEvents(quicksellInv, this);
	}
	
	private void initConfig() {
		//list of standard items
		String[] items = {"CUTLASS SWORD"};
		
		//tutorial text at the beginning of the config
		this.getConfig().options().header("List all of the items you plan on configuring in \"items\" as follows: [ITEM TYPE], " + System.getProperty("line.separator") +
				"configure them under \"prices\" as follows: [ITEM]: [AMOUNT] [PAYMENT] [PAYMENT TYPE]" + System.getProperty("line.separator") +
				"Example: A cutlass is worth 3 copper coins -> CUTLASS: 3 COPPER_COIN MISCELLANEOUS" + System.getProperty("line.separator"));
		
		//add default values to the config
		this.getConfig().addDefault("items", items);
		this.getConfig().addDefault("prices.CUTLASS", "3 COPPER_COIN MISCELLANEOUS");
		
		this.getConfig().addDefault("sell_message", "");
		
		
		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
	}
	
	/*
	 * Initializes the value and marketableItems maps
	 */
	
	private void readConfig() {
		List<String> items = this.getConfig().getStringList("items");
		
		//iterate over every item in the list
		for(String item : items) {
			if(item == "")
				continue;
			
			String articleType = item.split(" ")[1].toUpperCase();
			String articleID = item.split(" ")[0].toUpperCase();
			
			//entry[0]: amount, [1]: payment-id, [2]: payment type
			String[] entry = this.getConfig().getString("prices." + articleID).split(" ");
			
			String paymentID = entry[1].toUpperCase();
			int paymentAmount = Integer.parseInt(entry[0]);
			
			//reconstruct the article and payment ItemStacks from the config
			
			ItemStack articleItemStack = MMOItems.plugin.getItems().getItem(MMOItems.plugin.getTypes().get(articleType), articleID);
			ItemStack paymentItemStack = MMOItems.plugin.getItems().getItem(MMOItems.plugin.getTypes().get(entry[2].toUpperCase()), paymentID);
			
			paymentItemStack.setAmount(paymentAmount);
			
			String articleName = articleItemStack.getItemMeta().getDisplayName();
			
			//add the value to the values map (Article name -> payment ItemStack)
			this.values.put(articleName, paymentItemStack);
			marketableItems.add(articleName);
			
			System.out.println("[Phaletora-Sell] " + articleName + ChatColor.RESET + " will be sold for " +
					paymentAmount + " " + paymentItemStack.getItemMeta().getDisplayName() + ChatColor.RESET + "(s).");
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		//player commands
		if(sender instanceof Player) {
			Player p = (Player) sender;
			if(label.equalsIgnoreCase("quicksell")) {
				
				//create the quicksell inventory
				Inventory inv = Bukkit.createInventory(p, 9, "Quicksell inventory");
				
				//set the confirm book at the last slot of the inventory
				inv.setItem(inv.getSize()-1, ConfirmBook.confirmBook);
				
				//save the quicksell inventory
				inventories.put(p, inv);
				
				//open the quicksell inventory
				p.openInventory(inv);
				
				return true;
			}
			
		//console commands
		} else if(label.equalsIgnoreCase("quicksell"))
				System.out.println("[Phaletora-Sell] This is a player command! You can't use it from the console.");
		
		return false;
	}
	
	@Override
	public void onDisable() {
		quicksellInv.closeInv();
	}
	
}
