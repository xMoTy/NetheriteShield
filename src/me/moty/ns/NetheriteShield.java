package me.moty.ns;

import java.io.File;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmithingRecipe;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class NetheriteShield extends JavaPlugin implements Listener {
	private FileConfiguration config;
	private ItemStack shield;
	private NamespacedKey key = new NamespacedKey(this, "neitherite-shield");

	public void onEnable() {
		Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GRAY + "�ССССССССССССССССССССС�");
		Bukkit.getConsoleSender()
				.sendMessage(ChatColor.LIGHT_PURPLE + "NetheriteShield" + ChatColor.WHITE + " Enabled");
		Bukkit.getConsoleSender()
				.sendMessage(ChatColor.WHITE + "Powered by xMoTy#3812 | Version. " + getDescription().getVersion());
		Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GRAY + "�ССССССССССССССССССССС�");
		if (!new File(getDataFolder().getAbsolutePath() + "/config.yml").exists()) {
			getDataFolder().mkdir();
			saveResource("config.yml", false);
		}
		this.config = this.getConfig();

		shield = new ItemStack(Material.SHIELD);

		ItemMeta meta = shield.getItemMeta();
		BlockStateMeta bmeta = (BlockStateMeta) meta;

		Banner banner = (Banner) bmeta.getBlockState();
		banner.setBaseColor(DyeColor.BLACK);
		banner.addPattern(new Pattern(DyeColor.GRAY, PatternType.GRADIENT));
		banner.addPattern(new Pattern(DyeColor.GRAY, PatternType.BORDER));
		banner.addPattern(new Pattern(DyeColor.BLACK, PatternType.TRIANGLE_TOP));
		banner.update();
		bmeta.setBlockState(banner);
		meta.setDisplayName(
				ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', config.getString("display-name")));
		meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);

		meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier(UUID.randomUUID(),
				"GENERIC_ATTACK_SPEED", -1.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));
		meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(UUID.randomUUID(),
				"GENERIC_ATTACK_DAMAGE", 2, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));
		meta.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(UUID.randomUUID(),
				"GENERIC_MOVEMENT_SPEED", -0.02, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));
		meta.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(UUID.randomUUID(),
				"GENERIC_MOVEMENT_SPEED", -0.02, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.OFF_HAND));
		meta.addAttributeModifier(Attribute.GENERIC_MAX_HEALTH, new AttributeModifier(UUID.randomUUID(),
				"GENERIC_MAX_HEALTH", 4, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));
		meta.addAttributeModifier(Attribute.GENERIC_MAX_HEALTH, new AttributeModifier(UUID.randomUUID(),
				"GENERIC_MAX_HEALTH", 4, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.OFF_HAND));
		meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "neitherite-shield");
		shield.setItemMeta(bmeta);

		this.saveConfig();

		SmithingRecipe sr = new SmithingRecipe(key, shield, new RecipeChoice.MaterialChoice(Material.SHIELD),
				new RecipeChoice.MaterialChoice(Material.NETHERITE_INGOT));
		if (this.getServer().getRecipe(key) != null) {
			this.getServer().removeRecipe(key);
		}
		this.getServer().addRecipe(sr);

		getServer().getPluginManager().registerEvents(this, this);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (e.getInventory().getType() == InventoryType.SMITHING) {
			if (e.isLeftClick() && e.getRawSlot() == 2) {
				if (e.getInventory().getItem(e.getRawSlot()).getType() == Material.SHIELD) {
					e.getInventory().setItem(e.getRawSlot(), this.shield);
				}
			}
		}
	}

	@EventHandler
	public void onRaiseShield(PlayerInteractEvent e) {
		if (e.getMaterial() == Material.SHIELD
				&& e.getItem().getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING)
				&& (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			if (e.getPlayer().getFireTicks() != 0) {
				ItemMeta meta = e.getItem().getItemMeta();
				Damageable damage = (Damageable) meta;
				damage.setDamage(damage.getDamage() + (e.getPlayer().getFireTicks() / 20 / 2));
				e.getItem().setItemMeta(meta);
				e.getPlayer().setFireTicks(0);
			}
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntityType() == EntityType.DROPPED_ITEM) {
			if (!(e.getEntity() instanceof Item))
				return;
			ItemStack is = ((Item) e.getEntity()).getItemStack();
			if (is.getType() == Material.SHIELD
					&& is.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
				e.setCancelled(true);
			}
		}
	}

}
