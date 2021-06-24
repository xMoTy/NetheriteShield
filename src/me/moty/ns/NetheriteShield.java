package me.moty.ns;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.SmithingRecipe;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Consumer;

public class NetheriteShield extends JavaPlugin implements Listener {
	public FileConfiguration config;
	private String displayName;
	private String permissionNode;
	private String noPermission;
	private boolean craftable, smithing, hideAttribute, fallMitigation, fireResistance, fireProof;
	public DyeColor baseColor;
	public List<Pattern> patterns;
	private HashMap<Attribute, Double> attributes = new HashMap<>();
	private NamespacedKey key = new NamespacedKey(this, "neitherite-shield");
	private NamespacedKey crafting = new NamespacedKey(this, "shaped-nethrite-shield");

	@Override
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
		getVersion(version -> {
			if (!this.getDescription().getVersion().equalsIgnoreCase(version)) {
				this.getLogger().info(ChatColor.LIGHT_PURPLE + "There is a new update available: " + version);
			}
		});
		new Metrics(this, 11196);
		reloadConfiguration();

		getCommand("netheriteshield").setExecutor(new CommandNetheriteShield(this));
		getServer().getPluginManager().registerEvents(this, this);
	}

	@SuppressWarnings("unchecked")
	public void reloadConfiguration() {
		this.config = this.getConfig();
		this.displayName = config.isSet("display-name") ? config.getString("display-name") : "Netherite Shield";
		this.permissionNode = config.isSet("permission-node")
				? !config.getString("permission-node").equalsIgnoreCase("none") ? config.getString("permission-node")
						: null
				: null;
		this.noPermission = config.isSet("no-permission") ? config.getString("no-permission")
				: "&cYou don't have permission to smith Netherite Shield!";
		this.patterns = config.isSet("patterns") ? (List<Pattern>) config.getList("patterns")
				: Arrays.asList(new Pattern(DyeColor.GRAY, PatternType.GRADIENT),
						new Pattern(DyeColor.GRAY, PatternType.BORDER),
						new Pattern(DyeColor.BLACK, PatternType.TRIANGLE_TOP));
		this.baseColor = config.isSet("base-color") ? DyeColor.valueOf(config.getString("base-color")) : DyeColor.BLACK;
		this.craftable = config.isSet("crafting") ? config.getBoolean("crafting.enabled") : false;
		this.smithing = config.isSet("smithing") ? config.getBoolean("smithing") : true;
		this.fallMitigation = config.isSet("features.fall-damage-mitigation")
				? config.getBoolean("features.fall-damage-mitigation")
				: true;
		this.fireResistance = config.isSet("features.fire-resistance") ? config.getBoolean("features.fire-resistance")
				: true;
		this.fireProof = config.isSet("features.fire-proof") ? config.getBoolean("features.fire-proof") : true;
		this.hideAttribute = config.isSet("hide-attribute") ? config.getBoolean("hide-attribute") : false;

		if (smithing) {
			SmithingRecipe smith = new SmithingRecipe(key, new ItemStack(Material.SHIELD),
					new RecipeChoice.MaterialChoice(Material.SHIELD),
					new RecipeChoice.MaterialChoice(Material.NETHERITE_INGOT));

			if (this.getServer().getRecipe(key) != null) {
				this.getServer().removeRecipe(key);
			}
			this.getServer().addRecipe(smith);
		}
		if (config.isSet("attributes")) {
			for (String key : config.getConfigurationSection("attributes").getKeys(false)) {
				attributes.put(Attribute.valueOf("GENERIC_" + key), config.getDouble("attributes." + key));
			}
		} else {
			attributes.put(Attribute.GENERIC_ATTACK_DAMAGE, 2D);
			attributes.put(Attribute.GENERIC_ATTACK_SPEED, -1.5D);
			attributes.put(Attribute.GENERIC_MOVEMENT_SPEED, -0.02D);
			attributes.put(Attribute.GENERIC_MAX_HEALTH, 4D);
		}
		if (this.getServer().getRecipe(crafting) != null) {
			this.getServer().removeRecipe(crafting);
		}
		if (craftable) {
			ShapedRecipe craft = new ShapedRecipe(crafting, makeShield(null));
			List<String> shape = config.isSet("crafting.shape") ? config.getStringList("crafting.shape")
					: Arrays.asList("WNW", "WWW", " W ");
			craft.shape(shape.toArray(new String[3]));
			if (config.isSet("crafting.items")) {
				for (String key : config.getConfigurationSection("crafting.items").getKeys(false)) {
					if (config.get("crafting.items." + key) instanceof String) {
						craft.setIngredient(key.charAt(0), Material.valueOf(config.getString("crafting.items." + key)));
					} else {
						craft.setIngredient(key.charAt(0),
								new RecipeChoice.MaterialChoice(config.getStringList("crafting.items." + key).stream()
										.map(mat -> Material.valueOf(mat)).collect(Collectors.toList())));
					}
				}
			} else {
				craft.setIngredient('W', new RecipeChoice.MaterialChoice(Tag.PLANKS));
				craft.setIngredient('N', Material.NETHERITE_INGOT);
			}
			this.getServer().addRecipe(craft);
		}

	}

	public void getVersion(final Consumer<String> consumer) {
		Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
			try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + "91813")
					.openStream(); Scanner scanner = new Scanner(inputStream)) {
				if (scanner.hasNext()) {
					consumer.accept(scanner.next());
				}
			} catch (IOException exception) {
				this.getLogger().info("Cannot look for updates: " + exception.getMessage());
			}
		});
	}

	public ItemStack makeShield(ItemStack shield) {
		if (shield == null || shield.getType() != Material.SHIELD) {
			shield = new ItemStack(Material.SHIELD);
		}
		ItemMeta meta = shield.getItemMeta();
		BlockStateMeta bmeta = (BlockStateMeta) meta;

		Banner banner = (Banner) bmeta.getBlockState();
		banner.setBaseColor(baseColor);
		for (Pattern pat : patterns) {
			banner.addPattern(pat);
		}
		banner.update();
		bmeta.setBlockState(banner);

		if (!shield.getItemMeta().hasDisplayName())
			meta.setDisplayName(ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', displayName));

		meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
		if (!attributes.isEmpty())
			for (Attribute att : attributes.keySet()) {
				meta.addAttributeModifier(att, new AttributeModifier(UUID.randomUUID(), att.name(), attributes.get(att),
						AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));
				if (att != Attribute.GENERIC_ATTACK_DAMAGE && att != Attribute.GENERIC_ATTACK_KNOCKBACK
						&& att != Attribute.GENERIC_ATTACK_SPEED)
					meta.addAttributeModifier(att, new AttributeModifier(UUID.randomUUID(), att.name(),
							attributes.get(att), AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.OFF_HAND));
			}

		meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "neitherite-shield");

		if (hideAttribute) {
			meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		}
		shield.setItemMeta(bmeta);

		return shield;
	}

	public void setPatterns(ItemStack shield) {
		if (shield == null) {
			config.set("patterns", null);
			config.set("base-color", null);
			saveConfig();
			reloadConfiguration();
			return;
		}
		ItemMeta meta = shield.getItemMeta();
		BlockStateMeta bmeta = (BlockStateMeta) meta;
		Banner banner = (Banner) bmeta.getBlockState();
		if (banner.numberOfPatterns() > 0) {
			config.set("patterns", banner.getPatterns());
			config.set("base-color", banner.getBaseColor().name());
			saveConfig();
			reloadConfiguration();
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (e.getInventory().getType() == InventoryType.SMITHING && smithing) {
			if (e.isLeftClick() && e.getRawSlot() == 2) {
				if (e.getInventory().getItem(0).getType() == Material.SHIELD
						&& e.getInventory().getItem(1).getType() == Material.NETHERITE_INGOT
						&& e.getInventory().getItem(e.getRawSlot()).getType() == Material.SHIELD) {
					if (this.permissionNode != null && !e.getWhoClicked().hasPermission(this.permissionNode)) {
						e.setCancelled(true);
						e.getWhoClicked().closeInventory();
						e.getWhoClicked().sendMessage(ChatColor.translateAlternateColorCodes('&', this.noPermission));
						return;
					}
					if (e.getInventory().getItem(0).getItemMeta().getPersistentDataContainer().has(key,
							PersistentDataType.STRING)) {
						e.setCancelled(true);
						return;
					}
					ItemStack source = e.getInventory().getItem(0);
					e.getInventory().setItem(e.getRawSlot(), makeShield((source)));
					return;
				}
			}
		} else if (e.getInventory().getType() == InventoryType.WORKBENCH && craftable) {
			if (e.isLeftClick() && e.getRawSlot() == 0) {
				if (e.getInventory().getItem(0) == null || !e.getInventory().getItem(0).hasItemMeta())
					return;
				if (!e.getInventory().getItem(0).getItemMeta().getPersistentDataContainer().has(key,
						PersistentDataType.STRING))
					return;
				if (this.permissionNode != null && !e.getWhoClicked().hasPermission(this.permissionNode)) {
					e.setCancelled(true);
					e.getWhoClicked().closeInventory();
					e.getWhoClicked().sendMessage(ChatColor.translateAlternateColorCodes('&', this.noPermission));
					return;
				}
			}
		}
	}

	@EventHandler
	public void onRaiseShield(PlayerInteractEvent e) {
		if (!this.fireResistance) {
			return;
		}
		if (e.getMaterial() == Material.SHIELD
				&& e.getItem().getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING)
				&& (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			if (e.getPlayer().getFireTicks() != 0 && e.getPlayer().getLocation().getBlock().getType() != Material.LAVA
					&& e.getPlayer().getLocation().getBlock().getType() != Material.FIRE) {
				ItemMeta meta = e.getItem().getItemMeta();
				Damageable damage = (Damageable) meta;
				damage.setDamage(damage.getDamage() + (e.getPlayer().getFireTicks() / 20 / 2));
				e.getItem().setItemMeta(meta);
				e.getPlayer().setFireTicks(0);
				return;
			}
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntityType() == EntityType.DROPPED_ITEM) {
			if (!this.fireProof)
				return;
			if (!(e.getEntity() instanceof Item))
				return;
			if (e.getCause() != DamageCause.LAVA && e.getCause() != DamageCause.FIRE
					&& e.getCause() != DamageCause.FIRE_TICK && e.getCause() != DamageCause.HOT_FLOOR)
				return;
			ItemStack is = ((Item) e.getEntity()).getItemStack();
			if (is.getType() == Material.SHIELD
					&& is.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
				e.setCancelled(true);
				return;
			}
		} else if (e.getEntityType() == EntityType.PLAYER) {
			Player p = (Player) e.getEntity();
			if (e.getCause() != DamageCause.FALL)
				return;
			if (!this.fallMitigation)
				return;
			if (p.getInventory().getItemInMainHand().getType() == Material.SHIELD
					|| p.getInventory().getItemInOffHand().getType() == Material.SHIELD) {
				ItemStack is = p.getInventory().getItemInMainHand().getType() == Material.SHIELD
						? p.getInventory().getItemInMainHand()
						: p.getInventory().getItemInOffHand();
				if (is.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
					ItemMeta meta = is.getItemMeta();
					Damageable damage = (Damageable) meta;
					damage.setDamage(damage.getDamage() + (int) (e.getDamage() / 2));
					is.setItemMeta(meta);
					e.setDamage(e.getDamage() / 2);
					return;
				}
			}
		}
	}

}
