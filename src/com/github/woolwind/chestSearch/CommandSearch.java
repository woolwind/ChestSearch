package com.github.woolwind.chestSearch;

import java.util.ArrayList;
import java.util.HashMap;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;
import org.inventivetalent.particle.ParticleEffect;

import us.talabrek.ultimateskyblock.api.IslandInfo;
import us.talabrek.ultimateskyblock.api.uSkyBlockAPI;

public class CommandSearch implements CommandExecutor{
    // This method is called, when somebody uses our command
	private final ChestSearch plugin;
	
	public CommandSearch (ChestSearch plugin){
		this.plugin = plugin;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {

		 if (sender instanceof Player) { 
			 	if (args.length != 1){
			 		return false;
			 	}
	            Player player = (Player) sender;
	            String[]parts = args[0].split(":");
	            String SearchItemName = parts[0];
	           
	            //convert SearchItemName into proper enum
	            
	            Integer searchRadius = plugin.getConfig().getInt("Limits.SearchRadius");
	            Integer searchHeight = plugin.getConfig().getInt("Limits.SearchHeight");
	            ArrayList<String> allowedWorlds = (ArrayList<String>) plugin.getConfig().getStringList("Limits.AllowedWorlds");
	            
	            Runnable runnable = new Runnable(){

					@Override
					public void run() {
	
						//sender.sendMessage ("searching area for " + searchItem.name());
						
						ArrayList<Location> locations = new ArrayList<Location>();
						HashMap<Location,Location> sparklerLocations = new HashMap<Location,Location>();
						Location center = player.getLocation();
						World world = center.getWorld();
			            if (allowedWorlds.contains("*") == false && allowedWorlds.contains(world.getName()) == false){
			            	sender.sendMessage("not allowed in this world");
			            	return;
			            }

			            if (plugin.getConfig().getBoolean("APISupport.uSkyblock") == true){
			            	Plugin plugin = Bukkit.getPluginManager().getPlugin("uSkyBlock");
			            	if (plugin instanceof uSkyBlockAPI && plugin.isEnabled()) {
			            		uSkyBlockAPI usb = (uSkyBlockAPI) plugin;
			            		IslandInfo ii = usb.getIslandInfo(player.getLocation());	
			            		String pname = player.getName();
			            		if (ii == null || ii.getLeader() != pname || ii.getMembers().contains(pname) == false){
			            			sender.sendMessage ("You must be on your island to search chests");
			            			return;
			            		}
			            	}
			            }
						int startx = (int) center.getX() - searchRadius;
						int starty = (int) center.getY();
						int startz = (int) center.getZ() - searchRadius;
						
						int endx = startx + (searchRadius * 2);
						int endz = startz + (searchRadius * 2);
						int endy = starty + searchHeight;
						
			            if (plugin.getConfig().getBoolean("APISupport.GriefPrevention") == true){
			            	Plugin plugin = Bukkit.getPluginManager().getPlugin("GriefPrevention");
			            	if (plugin instanceof GriefPrevention && plugin.isEnabled()) {
			            	
			            		Claim claim = GriefPrevention.instance.dataStore.getClaimAt(center, false, null);
			            		if (claim == null){
			            			sender.sendMessage("you can't search the wilderness");
			            			return;
			            		}
			            		if (claim.allowAccess(player)!= null){
			            			sender.sendMessage("You can only search chests in claims you can access");
			            			return;
			            		}
			            		Location corner1 =  new Location(world, startx, starty, startz);
			            		Location corner2 =  new Location(world, endx, endy, endz);
			            	}
			            }
			            String[]parts = args[0].split(":");
			            String SearchItemName = parts[0];
			           
			            Material searchItem = Material.getMaterial(SearchItemName.toUpperCase());
			            Byte SearchSubType = 0;
				        if (parts.length > 1 ){
				            	SearchSubType =  Byte.valueOf(parts[1]); 
				        }
				        
						if (searchItem == null){
							sender.sendMessage ("no such material: " + SearchItemName);
							return;
						}
						for (int y = starty; y < endy; y++) {
							//plugin.getLogger().info("searching at y = " + String.valueOf(y));
							for (int x = startx; x <= endx; x++){
								for (int z = startz; z <= endz; z++){
									Block block = world.getBlockAt(x, y, z);
									if ((block.getType() == Material.CHEST) || (block.getType() == Material.TRAPPED_CHEST)){
										Chest chest = (Chest) block.getState();
										Inventory inv = chest.getBlockInventory();
										HashMap<Integer, ? extends ItemStack> map = inv.all(searchItem);
										if (map.size() > 0){
											if (SearchSubType > 0){
												for ( ItemStack val :  map.values() ) {
													if ( val.getDurability() == SearchSubType ){
														locations.add(block.getLocation());
														sparklerLocations.put(block.getLocation(), getChestFront(block));
													}
												}
											}else{
												locations.add(block.getLocation());
												sparklerLocations.put(block.getLocation(), getChestFront(block));
											}
										}
									}
								}
							}
						}
						if (locations.size() == 0){
							sender.sendMessage("No " + SearchItemName + " found in nearby chests");
							return;
						}
						sender.sendMessage("found " + SearchItemName + " in " + String.valueOf(locations.size()) + " chests:");
						for (int i = 0; i < locations.size(); i++){
							Location loc = locations.get(i);
							sender.sendMessage ("X " + String.valueOf(loc.getX()) + " Y " + 
							String.valueOf(loc.getY()) +  " Z " + String.valueOf(loc.getZ()));
							if (plugin.getConfig().getBoolean("APISupport.ParticleEffects") == true){
								
	//							plugin.getLogger().info("chest at X " + String.valueOf(loc.getX()) + " Y " + 
	//									String.valueOf(loc.getY()) +  " Z " + String.valueOf(loc.getZ()));
								Location sparklerLocation = sparklerLocations.get(loc);
								
	//							plugin.getLogger().info("sparkle at X " + String.valueOf(sparklerLocation.getX()) + " Y " + 
	//									String.valueOf(sparklerLocation.getY()) +  " Z " + String.valueOf(sparklerLocation.getZ()));
								ParticleEffect.FLAME.send(Bukkit.getOnlinePlayers(), sparklerLocation, 0, 0, 0, 0, 3);
							}
						}
					}
	            };
	            if (Bukkit.isPrimaryThread()) {
	                runnable.run();
	            } else {
	                Bukkit.getScheduler().runTask(plugin, runnable);
	            }
	            return true;
		 }
		 sender.sendMessage("You must be a player!");
		 return false;
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