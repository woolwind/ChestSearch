package com.github.woolwind.chestSearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Player;

import com.google.common.collect.Range;

public class SearchJob implements Runnable{

	private final CommandSearch caller;
	private final ChestSearch plugin;
	private Player player = null;
	
	private Material searchItem = null;
    private Byte searchSubType = 0;
	private String name = null;
	private String lore = null;
	
	private Validator validator;
	
	public SearchJob (ChestSearch plugin, CommandSearch caller){
		this.plugin = plugin;
		this.caller = caller;
	}
	public void setPlayer (Player player){
		this.player = player;
	}
	public void setMaterial (Material material){
		this.searchItem = material;
	}
	public void setSubtype (Byte searchSubType){
		this.searchSubType = searchSubType;
	}
	public void setName(String name){
		this.name = name;
	}
	public void setLore(String lore){
		this.lore = lore;
	}
	
	@Override
	public void run() {
		validator = new Validator(plugin);
		if ( validator.isValidPlayerLocation(player) == false){
			return;
		}
		Range <Integer> allX = validator.getXRange();
		Range <Integer> allY = validator.getYRange();
		Range <Integer> allZ = validator.getZRange();
		org.bukkit.World world = player.getWorld(); 
		plugin.getLogger().info("starting search runner");
		new BukkitRunnable() {
			@Override
			public void run() {				
				ArrayList<Location> chestLocations = new ArrayList<Location>();
				for (Integer y = allY.lowerEndpoint(); y <= allY.upperEndpoint(); y++  ) {
					//plugin.getLogger().info("searching at y = " + String.valueOf(y));
					for(Integer x = allX.lowerEndpoint(); x <= allX.upperEndpoint(); x++  ){
						for (Integer z = allZ.lowerEndpoint(); z <= allZ.upperEndpoint(); z++  ){
							Block block = world.getBlockAt(x, y, z); 				// unclear if safe for async
							if ((block.getType() == Material.CHEST) || (block.getType() == Material.TRAPPED_CHEST)){
								chestLocations.add(block.getLocation());
							}
						}	
					}
				}
				chestJobDidComplete(chestLocations);
			}		
		}.runTaskAsynchronously(plugin);
	}
	
	private void chestJobDidComplete(ArrayList<Location> chestLocations){
		ArrayList<Location> locations = new ArrayList<Location>();
		HashMap<Location,Location> sparklerLocations = new HashMap<Location,Location>();
		for  (Location loc: chestLocations){
			Block block = loc.getBlock();
			Chest chest = (Chest) block.getState();
			Inventory inv = chest.getBlockInventory();
			HashMap<Integer, ? extends ItemStack> map = inv.all(searchItem);
			if (map.size() > 0){
				for (ItemStack val :  map.values() ) {
					if ( (searchSubType ==  val.getDurability())){
						ItemMeta im = val.getItemMeta();
						List<String> lores = im.getLore();
						if (lore == null || (lores != null &&  lores.contains(lore))){													
							locations.add(block.getLocation());
							sparklerLocations.put(block.getLocation(), getChestFront(block));
						}		
					}
				}
			}
		}
		SearchResults results = new SearchResults (locations, sparklerLocations);
		results = validator.validateResults(results);
		caller.searchJobDidComplete(results);
	}

    private Location getChestFront(Block chestBlock) {      
    	MaterialData data = chestBlock.getState().getData();
    	BlockFace primaryDirection = ((org.bukkit.material.Chest) data).getFacing();
    	// get center of chest block
    	double px = chestBlock.getX() + 0.5;
    	double py = chestBlock.getY() + 0.5; 
    	double pz = chestBlock.getZ() + 0.5;
    	// and move outwards 1 block in direction chest faces
    	if (primaryDirection == BlockFace.NORTH) {
    		// Neg Z                                                                                                                                                            
    		pz -= 1; // start one block in the north dir.                                                                                                                       
        } else if (primaryDirection == BlockFace.SOUTH) {
           // Pos Z                                                                                                                                                            
           	pz += 1; // start one block in the south dir                                                                                                                        
        } else if (primaryDirection == BlockFace.WEST) {
            // Neg X                                                                                                                                                            
        	px -= 1; // start one block in the west dir                                                                                                                         
        } else if (primaryDirection == BlockFace.EAST) {
            // Pos X                                                                                                                                                            
        	px += 1; // start one block in the east dir                                                                                                                         
        }
        return (new Location(chestBlock.getWorld(), px, py, pz));
    }
}
