package me.moty.ns;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class CommandNetheriteShield implements CommandExecutor {
	private NetheriteShield ns;

	public CommandNetheriteShield(NetheriteShield ns) {
		this.ns = ns;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
		if (!sender.isOp()) {
			return false;
		}
		if (args.length < 1) {
			return false;
		}
		if (args[0].equalsIgnoreCase("set")) {
			if (!(sender instanceof Player))
				return false;
			Player p = (Player) sender;
			if (p.getInventory().getItemInMainHand().getType() == Material.SHIELD) {
				ns.setPatterns(p.getInventory().getItemInMainHand());
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSet successfully!"));
				return true;
			}
		} else if (args[0].equalsIgnoreCase("reset")) {
			ns.setPatterns(null);
			if (sender instanceof Player) {
				((Player) sender).sendMessage(ChatColor.translateAlternateColorCodes('&', "&aReset successfully!"));
			} else {
				sender.sendMessage("Reset successfully!");
			}
		} else if (args[0].equalsIgnoreCase("reload")) {
			ns.reloadConfig();
			ns.reloadConfiguration();
			if (sender instanceof Player) {
				((Player) sender).sendMessage(ChatColor.translateAlternateColorCodes('&', "&aReloaded successfully!"));
			} else {
				sender.sendMessage("Reloaded successfully!");
			}
			return true;
		}
		return false;
	}

}
