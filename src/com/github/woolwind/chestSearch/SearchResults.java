package com.github.woolwind.chestSearch;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;

public class SearchResults {
	private final ArrayList<Location> locations;
	private final HashMap<Location,Location> sparklerLocations;
	
	public SearchResults (ArrayList<Location> locations, HashMap<Location,Location> sparklerLocations){
		this.locations =locations;
		this.sparklerLocations = sparklerLocations;
	}
	
	public ArrayList<Location>	getLocations(){
		return locations;
	}
	public  HashMap<Location,Location> getSparklerLocations(){
		return sparklerLocations;
	}
	
	public Location getSparklerAt(Location location){
		return sparklerLocations.get(location);
	}
}
