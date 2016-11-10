package com.github.woolwind.chestSearch;
import java.util.ArrayList;
import java.util.HashMap;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.google.common.collect.Range;

import us.talabrek.ultimateskyblock.api.IslandInfo;
import us.talabrek.ultimateskyblock.api.uSkyBlockAPI;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class Validator {
	private final ChestSearch plugin;
	private Player player;
	
	private Range<Integer>  allX = null; 
	private Range<Integer>  allY= null; 
	private Range<Integer>  allZ = null; 
	
	public Validator (ChestSearch plugin){
		this.plugin = plugin;
	}
	
	public Range <Integer>getXRange(){
		return allX;
	}
	public Range <Integer> getYRange(){
		return allY;
	}
	public Range<Integer> getZRange(){
		return allZ;
	}
	
	public Boolean isValidPlayerLocation(Player player){
		      
        ArrayList<String> allowedWorlds = (ArrayList<String>) plugin.getConfig().getStringList("Limits.AllowedWorlds");
		World world = player.getWorld();
        if (allowedWorlds.contains("*") == false && allowedWorlds.contains(world.getName()) == false){
        	player.sendMessage("not allowed in this world");
        	return false;
        }
        this.player = player;
        Location loc  = player.getLocation();   
        Integer searchRadius = plugin.getConfig().getInt("Limits.SearchRadius");
        Integer searchHeight = plugin.getConfig().getInt("Limits.SearchHeight");
        allX = Range.closed( (int)loc.getX() - searchRadius, (int) loc.getX() + searchRadius);
        allY = Range.closed( (int)loc.getY(), (int) loc.getY() + searchHeight);
        allZ = Range.closed( (int)loc.getZ() - searchRadius, (int) loc.getZ() + searchRadius);

        if (plugin.getConfig().getBoolean("APISupport.uSkyblock") == true){
        	Plugin plugin = Bukkit.getPluginManager().getPlugin("uSkyBlock");
        	if (plugin instanceof uSkyBlockAPI && plugin.isEnabled()) {
        		uSkyBlockAPI usb = (uSkyBlockAPI) plugin;
        		IslandInfo ii = usb.getIslandInfo(player.getLocation());	
        		String pname = player.getName();
        		if (ii == null ||( ii.getLeader() != pname &&
        				ii.getMembers().contains(pname) == false)){
        			player.sendMessage ("You must be on your island to search chests");
        			return false;
        		}
        		WorldGuardPlugin wg = WGBukkit.getPlugin();

        		RegionManager rm = wg.getRegionManager(world);
        		ApplicableRegionSet  rset = rm.getApplicableRegions(player.getLocation());
        		ProtectedRegion islandregion = null;
        		for (ProtectedRegion region : rset) {
        		    // region here
        			String rname = region.getId();
        			if (rname.endsWith("island")){
        				islandregion = region;
        			}
        		}
        		if (islandregion == null){
        			plugin.getLogger().info("no worldguard region for island found");
        			player.sendMessage("sorry, had a problem");
        			return false;
        		}
        		BlockVector min = islandregion.getMinimumPoint();
        		BlockVector max = islandregion.getMaximumPoint();
        	
        		allX = Range.closed((int)min.getX(),(int)max.getX());
        		allY = Range.closed((int)min.getY(),(int)max.getY());
        		allZ = Range.closed((int)min.getZ(),(int)max.getZ());
        		
        	}
        }
 
        		     		
        if (plugin.getConfig().getBoolean("APISupport.GriefPrevention") == true){
        	Plugin plugin = Bukkit.getPluginManager().getPlugin("GriefPrevention");
        	if (plugin instanceof GriefPrevention && plugin.isEnabled()) {
        	
        		Claim claim = GriefPrevention.instance.dataStore.getClaimAt(player.getLocation(), false, null);
        		if (claim == null){
        			player.sendMessage("you can't search the wilderness");
        			return false;
        		}
        		if (claim.allowAccess(player)!= null){
        			player.sendMessage("You can only search chests in claims you can access");
        			return false;
        		}
         	}
        }
		return true;
	}
		
	public SearchResults validateResults(SearchResults results){
		ArrayList<Location> locations = results.getLocations();
		HashMap<Location,Location> sparklerLocations = results.getSparklerLocations();	
		//GriefPrevention API here, check all found chests are inside claim
		//Factions - check all found chests are within the fac lands
		if (plugin.getConfig().getBoolean("APISupport.GriefPrevention") == true){
			Plugin plugin = Bukkit.getPluginManager().getPlugin("GriefPrevention");
			if (plugin instanceof GriefPrevention && plugin.isEnabled()) {
				for (Location loc : locations){
					Claim claim = GriefPrevention.instance.dataStore.getClaimAt(loc, false, null);
					if (claim == null || claim.allowAccess(player) == null){
						locations.remove(loc);
					}
				}
			}
		}
		SearchResults  validatedResults = new SearchResults(locations,sparklerLocations);
		return validatedResults;
		}
}
