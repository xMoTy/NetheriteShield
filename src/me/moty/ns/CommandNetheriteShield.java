package me.moty.ns;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class CommandNetheriteShield implements CommandExecutor, TabCompleter {
	private NetheriteShield ns;

	public CommandNetheriteShield(NetheriteShield ns) {
		this.ns = ns;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
		if (!sender.isOp())
			return false;
		if (args.length < 1)
			return false;
		if (args[0].equalsIgnoreCase("set")) {
			if (!(sender instanceof Player))
				return false;
			Player p = (Player) sender;
			if (p.getInventory().getItemInMainHand().getType() == Material.SHIELD) {
				ns.setPatterns(p.getInventory().getItemInMainHand());
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSet successfully!"));
				return true;
			} else if (p.getInventory().getItemInMainHand().getType().name().contains("BANNER")) {
				ns.setPatterns(p.getInventory().getItemInMainHand());
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSet successfully!"));
				return true;
			}
		} else if (args[0].equalsIgnoreCase("reset")) {
			ns.setPatterns(null);
			if (sender instanceof Player)
				((Player) sender).sendMessage(ChatColor.translateAlternateColorCodes('&', "&aReset successfully!"));
			else
				sender.sendMessage("Reset successfully!");
		} else if (args[0].equalsIgnoreCase("reload")) {
			ns.reloadConfig();
			ns.reloadConfiguration();
			if (sender instanceof Player)
				((Player) sender).sendMessage(ChatColor.translateAlternateColorCodes('&', "&aReloaded successfully!"));
			else
				sender.sendMessage("Reloaded successfully!");
			return true;
		} else if (args[0].equalsIgnoreCase("get")) {
			if (!(sender instanceof Player))
				return false;
			Player p = (Player) sender;
			p.getInventory().addItem(this.ns.makeShield(null));
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aThe shield has sent to your inventory!"));
			return true;
		} else if (args[0].equalsIgnoreCase("give")) {
			if (args.length < 2)
				return false;
			if (this.ns.getServer().getPlayer(args[1]) == null)
				return false;
			Player p = this.ns.getServer().getPlayer(args[1]);
			p.getInventory().addItem(this.ns.makeShield(null));
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&aThe shield has sent to %player%'s inventory!".replace("%player%", p.getName())));
			return true;
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String arg2, String[] args) {
		if (!sender.isOp())
			return null;
		if (args.length == 1)
			return Arrays.asList("set", "reset", "reload", "get", "give");
		return null;
	}
}
