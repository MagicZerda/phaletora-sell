package de.magiczerda.phaletora.sell.utils;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ConfirmBook {
	
	public static ItemStack confirmBook;
	
	public ConfirmBook() {
		confirmBook = new ItemStack(Material.BOOK);
		ItemMeta cbm = confirmBook.getItemMeta();
		cbm.setDisplayName("Confirm");
		cbm.setLore(Arrays.asList("Click this item to sell your items"));
		confirmBook.setItemMeta(cbm);
	}
	
}
