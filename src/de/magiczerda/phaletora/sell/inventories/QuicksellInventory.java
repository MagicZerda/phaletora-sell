package de.magiczerda.phaletora.sell.inventories;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.magiczerda.phaletora.sell.plugin.Plugin;
import de.magiczerda.phaletora.sell.utils.ConfirmBook;
import net.Indyuce.mmoitems.api.item.NBTItem;

public class QuicksellInventory implements Listener {
	
	private Plugin plugin;
	
	public QuicksellInventory(Plugin plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onInventoryInteract(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		
		//we exclude most inventories by checking their names (chests can be renamed though, so we have to check if the confirm book is present, too
		if(!e.getView().getTitle().equals("Quicksell inventory"))
			return;
		
		//we find out whether the inventory is a quicksell inventory by checking if the confirm book is contained
		if(!e.getInventory().contains(ConfirmBook.confirmBook))
			return;
		
		if(e.getCurrentItem() == null)
			return;
		
		//execute the following code if the player clicked the confirm book
		if(e.getCurrentItem().equals(ConfirmBook.confirmBook)) {
			
			//cancel the event if the player shift clicks the book, so they can't have the book in their inventory
			if(e.isShiftClick()) {
				e.setCancelled(true);
				return;
			}
			
			//get all of the contents (except for the book)
			List<ItemStack> contents = getContents(plugin.inventories.get(p), p, false);
			
			//sell the items
			boolean sendMessage = sell(contents, p);
			
			//delete the current quicksell inventory (it won't be used again)
			plugin.inventories.remove(p);
			
			//close the quicksell inventory
			p.closeInventory();
			
			//send the player a custom, static message if an item was sold (colors can be used via color codes)
			if(sendMessage)
				p.sendMessage(plugin.getConfig().getString("sell_message"));
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		Player p = (Player) e.getPlayer();
		
		//we find out whether the inventory is a quicksell inventory by checking if the confirm book is present and if the title is correct
		if(e.getInventory().contains(ConfirmBook.confirmBook) && e.getView().getTitle().equals("Quicksell inventory")) {
			
			//give the player all non MMOItems back
			getContents(e.getInventory(), p, true);
			
			//remove the current quicksell inventory
			plugin.inventories.remove(p);
		}
	}
	
	/*
	 * Give all players their items back if they look at the quicksell inventory
	 * while the server shuts down (they would be lost otherwise)
	 */
	
	public void closeInv() {
		for(Map.Entry<Player, Inventory> inventoryMap : plugin.inventories.entrySet()) {
		    for(ItemStack is : getContents(inventoryMap.getValue(), inventoryMap.getKey(), true))
		    	inventoryMap.getKey().getInventory().addItem(is);
		    
		    //close the inventory (if the server is reloaded, the inventory stays open and the player gets the items -> item duplication bug)
		    inventoryMap.getKey().closeInventory();
		}
		
		//unnecessary, but i like to do it anyways
		plugin.inventories.clear();
	}
	
	/*
	 * This method returns all of the MMOItems and gives
	 * the player all other items back if giveItems is true.
	 */
	
	private List<ItemStack> getContents(Inventory inv, Player p, boolean giveItems) {
		ItemStack[] contents = inv.getContents();
		
		//this is the list of MMOItems that will be returned
		List<ItemStack> items = new ArrayList<ItemStack>();
		
		for(ItemStack is : contents) {
			if(is == null)
				continue;
			
			//exclude the confirm book from the items list
			if(!is.equals(ConfirmBook.confirmBook)) {
				
				//add all marketable MMOItems to the items list
				NBTItem nbtis = NBTItem.get(is);
				
				if(nbtis.hasType() && plugin.marketableItems.contains(is.getItemMeta().getDisplayName()))
					items.add(is);
					
				
				//give the player all of the other items back if giveItems is activated
				else if(giveItems)
						p.getInventory().addItem(is);
			}
		}
		
		return items;
	}
	
	/*
	 * sells all of the items to the player
	 * return whether an item was sold
	 */
	
	private boolean sell(List<ItemStack> items, Player p) {
		if(items.isEmpty())
			return false;
		
		boolean itemSold = false;
		
		for(ItemStack item : items) {
			if(item == null)
				continue;
			
			//retrieve the payment ItemStack from the values map in the main class and give it to the player
			ItemStack payment = plugin.values.get(item.getItemMeta().getDisplayName());
			p.getInventory().addItem(payment);
			itemSold = true;
		}
		
		return itemSold;
	}
	
}
