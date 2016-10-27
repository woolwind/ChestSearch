package com.github.woolwind.chestSearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.inventivetalent.particle.ParticleEffect;
import org.bukkit.entity.Player;

public class SearchJob implements Runnable{

	private final CommandSearch caller;
	private final ChestSearch plugin;
	private Player player = null;
	
	private Material searchItem = null;
    private Byte searchSubType = 0;
	private String name = null;
	private String lore = null;
	
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

		ArrayList<Location> locations = new ArrayList<Location>();
		HashMap<Location,Location> sparklerLocations = new HashMap<Location,Location>();
		Location center = player.getLocation();
        Integer searchRadius = plugin.getConfig().getInt("Limits.SearchRadius");
        Integer searchHeight = plugin.getConfig().getInt("Limits.SearchHeight");

		Validator validator = new Validator(plugin);
		
		if ( validator.okToSearch(player) == false){
			return;
		}						

		int startx = (int) center.getX() - searchRadius;
		int starty = (int) center.getY();
		int startz = (int) center.getZ() - searchRadius;
		
		int endx = startx + (searchRadius * 2);
		int endz = startz + (searchRadius * 2);
		int endy = starty + searchHeight;
		

	
		for (int y = starty; y < endy; y++) {
			//plugin.getLogger().info("searching at y = " + String.valueOf(y));
			for (int x = startx; x <= endx; x++){
				for (int z = startz; z <= endz; z++){
					Block block = player.getWorld().getBlockAt(x, y, z);
					if ((block.getType() == Material.CHEST) || (block.getType() == Material.TRAPPED_CHEST)){
						Chest chest = (Chest) block.getState();
						Inventory inv = chest.getBlockInventory();
						HashMap<Integer, ? extends ItemStack> map = inv.all(searchItem);
						if (map.size() > 0){
							for ( ItemStack val :  map.values() ) {
								//plugin.getLogger().info("subtype " + String.valueOf(SearchSubType) + 
								//" lore [" + lore + "]");
								if ( (searchSubType ==  val.getDurability())){
									ItemMeta im = val.getItemMeta();
									List<String> lores = im.getLore();
									
									//player.sendMessage("item lores: " +  lores);
									if (lore == null || (lores != null &&  lores.contains(lore))){													
										locations.add(block.getLocation());
										sparklerLocations.put(block.getLocation(), getChestFront(block));
									}		
								}
							}
						}
					}
				}
			}
		}
		caller.searchJobDidComplete(locations, sparklerLocations);
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
