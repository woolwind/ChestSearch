package com.github.woolwind.chestSearch;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandReload implements CommandExecutor{
    // This method is called, when somebody uses our command
	private final ChestSearch plugin;
	
	public CommandReload(ChestSearch plugin){
		this.plugin = plugin;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
			plugin.reloadConfig();
			if (sender.hasPermission("chestsearch.admin"))
			sender.sendMessage("configuration loaded from file");
			return true;
	}
}