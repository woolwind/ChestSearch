package com.github.woolwind.chestSearch;
import java.util.ArrayList;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import us.talabrek.ultimateskyblock.api.IslandInfo;
import us.talabrek.ultimateskyblock.api.uSkyBlockAPI;

public class Validator {
	private final ChestSearch plugin;
	
	public Validator (ChestSearch plugin){
		this.plugin = plugin;
	}
	
	public Boolean okToSearch(Player player){
		
        Integer searchRadius = plugin.getConfig().getInt("Limits.SearchRadius");
        Integer searchHeight = plugin.getConfig().getInt("Limits.SearchHeight");
        ArrayList<String> allowedWorlds = (ArrayList<String>) plugin.getConfig().getStringList("Limits.AllowedWorlds");
		World world = player.getWorld();
        if (allowedWorlds.contains("*") == false && allowedWorlds.contains(world.getName()) == false){
        	player.sendMessage("not allowed in this world");
        	return false;
        }
        if (plugin.getConfig().getBoolean("APISupport.uSkyblock") == true){
        	Plugin plugin = Bukkit.getPluginManager().getPlugin("uSkyBlock");
        	if (plugin instanceof uSkyBlockAPI && plugin.isEnabled()) {
        		uSkyBlockAPI usb = (uSkyBlockAPI) plugin;
        		IslandInfo ii = usb.getIslandInfo(player.getLocation());	
        		String pname = player.getName();
        		if (ii == null || ii.getLeader() != pname || ii.getMembers().contains(pname) == false){
        			player.sendMessage ("You must be on your island to search chests");
        			return false;
        		}
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
}
