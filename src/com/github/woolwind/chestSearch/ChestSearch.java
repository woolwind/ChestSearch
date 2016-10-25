package com.github.woolwind.chestSearch;

import java.util.ArrayList;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ChestSearch extends JavaPlugin{
	
	FileConfiguration config = getConfig();
	
    // Fired when plugin is first enabled
    @Override
    public void onEnable() {
    	this.getConfig();
    	config.addDefault("APISupport.uSkyblock", false);
    	config.addDefault("APISupport.GriefPrevention", false);
		config.addDefault("APISupport.ParticleEffects", false);
		config.addDefault("Limits.SearchRadius", 20);
		config.addDefault("Limits.SearchHeight", 4);

		ArrayList<String> worlds = new ArrayList<String>();
		worlds.add("*");
		config.addDefault("Limits.AllowedWorlds",worlds);
		config.options().copyDefaults(true);
		saveConfig();
    	
		this.getCommand("search").setExecutor(new CommandSearch(this));
		this.getCommand("chestsearchreload").setExecutor(new CommandReload(this));
    }
    // Fired when plugin is disabled
    @Override
    public void onDisable() {

    }
}
