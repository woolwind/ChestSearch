package com.github.woolwind.chestSearch;


import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.Bukkit;
import org.inventivetalent.particle.ParticleEffect;


public class CommandSearch implements CommandExecutor{
    // This method is called, when somebody uses our command
	private final ChestSearch plugin;
	private Player player;
	private String searchItemName;
	
	public CommandSearch (ChestSearch plugin){
		this.plugin = plugin;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {

		 if (sender instanceof Player) { 
			 	if (args.length < 1){
			 		return false;
			 	}
	            player = (Player) sender;
	           
	            //convert SearchItemName into proper enum
	            
	            String[]parts = args[0].split(":");
	            searchItemName = parts[0];
	            String lore = null;	
	            String name = null;
	           	          
	            Byte SearchSubType = 0;
	            if (parts.length > 1 ){
	                	SearchSubType =  Byte.valueOf(parts[1]); 
	            }
	            for (int i = 1; i< args.length; i++){
	            	String arg = args[i];
	            	if (arg.startsWith("lore=")){
	            		String[]t = arg.split("=");
	            		lore = t[1];
	            		lore = lore.replace("\"","");
	            	}
	            	if (arg.startsWith("namee=")){
	            		String[]t = arg.split("=");
	            		name = t[1];
	            		name = name.replace("\"","");
	            	}
	            }
	                    	            
	            SearchJob searchjob = new SearchJob(plugin,this);
	            searchjob.setPlayer(player);
	            Material searchItem = Material.getMaterial(searchItemName.toUpperCase());
	           	if (searchItem == null){
	           		player.sendMessage ("no such material: " + searchItemName);
	          		return true;
	     		}
	            searchjob.setMaterial(searchItem);
	            searchjob.setSubtype(SearchSubType);
	            searchjob.setName(name);
	            searchjob.setLore(lore);
	            
	            if (Bukkit.isPrimaryThread()) {
	                searchjob.run();
	            } else {
	                Bukkit.getScheduler().runTask(plugin, searchjob);
	            }
		 }
		 sender.sendMessage("You must be a player!");
		 return false;
	}
	
	public void searchJobDidComplete(ArrayList<Location> locations, HashMap<Location,Location> sparklerLocations){
		if (locations.size() == 0){
			player.sendMessage("No " + searchItemName + " found in nearby chests");
			return;
		}
		player.sendMessage("found " + searchItemName + " in " + String.valueOf(locations.size()) + " chests:");
		for (int i = 0; i < locations.size(); i++){
			Location loc = locations.get(i);
			player.sendMessage ("X " + String.valueOf(loc.getX()) + " Y " + 
			String.valueOf(loc.getY()) +  " Z " + String.valueOf(loc.getZ()));
			if (plugin.getConfig().getBoolean("APISupport.ParticleEffects") == true){
				Location sparklerLocation = sparklerLocations.get(loc);
				ParticleEffect.FLAME.send(Bukkit.getOnlinePlayers(), sparklerLocation, 0, 0, 0, 0, 3);
			}
		}
	}
}
	
