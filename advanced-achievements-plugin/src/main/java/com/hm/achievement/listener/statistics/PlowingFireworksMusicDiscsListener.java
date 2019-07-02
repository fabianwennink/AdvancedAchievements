package com.hm.achievement.listener.statistics;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.Category;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Listener class to deal with HoePlowings, Fireworks and MusicDiscs achievements.
 *
 * @author Pyves
 *
 */
@Singleton
public class PlowingFireworksMusicDiscsListener extends AbstractRateLimitedListener {

	private final Set<Category> disabledCategories;

	@Inject
	public PlowingFireworksMusicDiscsListener(@Named("main") CommentedYamlConfiguration mainConfig,
			int serverVersion, Map<String, List<Long>> sortedThresholds, CacheManager cacheManager,
			RewardParser rewardParser, AdvancedAchievements advancedAchievements,
			@Named("lang") CommentedYamlConfiguration langConfig, Logger logger, Set<Category> disabledCategories) {
		super(mainConfig, serverVersion, sortedThresholds, cacheManager, rewardParser, advancedAchievements, langConfig,
				logger);
		this.disabledCategories = disabledCategories;
	}

	@EventHandler(priority = EventPriority.MONITOR) // Do NOT set ignoreCancelled to true, see SPIGOT-4793.
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.useItemInHand() == Result.DENY || !event.hasItem()) {
			return;
		}

		NormalAchievements category = getCategory(event);
		if (category == null || disabledCategories.contains(category)) {
			return;
		}

		if (category == NormalAchievements.MUSICDISCS && isInCooldownPeriod(event.getPlayer(), true, category)) {
			return;
		}

		updateStatisticAndAwardAchievementsIfAvailable(event.getPlayer(), category, 1);
	}

	/**
	 * Determines which achievement category this event belongs to.
	 * 
	 * @param event
	 * @return the achievement category or null if none match
	 */
	private NormalAchievements getCategory(PlayerInteractEvent event) {
		Block clickedBlock = event.getClickedBlock();
		Material materialInHand = event.getMaterial();
		if (isFirework(materialInHand) && canAccommodateFireworkLaunch(clickedBlock, event.getPlayer(), event.getAction())) {
			return NormalAchievements.FIREWORKS;
		} else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (materialInHand.name().contains("HOE") && canBePlowed(clickedBlock)) {
				return NormalAchievements.HOEPLOWING;
			} else if (materialInHand.isRecord() && clickedBlock.getType() == Material.JUKEBOX) {
				return NormalAchievements.MUSICDISCS;
			}
		}
		return null;
	}

	/**
	 * Determines whether a material can be plowed with a hoe.
	 *
	 * @param block
	 *
	 * @return true if the block can be plowed, false otherwise
	 */
	private boolean canBePlowed(Block block) {
		return (serverVersion < 13 && block.getType() == Material.GRASS || block.getType() == Material.DIRT
				|| serverVersion >= 13 && block.getType() == Material.GRASS_BLOCK)
				&& block.getRelative(BlockFace.UP).getType() == Material.AIR;
	}

	/**
	 * Determines whether the used material is a firework.
	 *
	 * @param material
	 * @return true if the material is a firework, false otherwise
	 */
	private boolean isFirework(Material material) {
		return serverVersion >= 13 ? material == Material.FIREWORK_ROCKET : "FIREWORK".equals(material.name());
	}

	/**
	 * Determines whether a firework can be launched when interacting with this block.
	 *
	 * @param clickedBlock
	 * @param player
	 * @param action
	 * @return true if the material can be used to launch a firework, false otherwise
	 */
	private boolean canAccommodateFireworkLaunch(Block clickedBlock, Player player, Action action) {
		// Players can launch fireworks without interacting with a block only if they're gliding.
		if (serverVersion >= 9 && player.isGliding() && action == Action.RIGHT_CLICK_AIR) {
			return true;
		} else if (action != Action.RIGHT_CLICK_BLOCK) {
			return false;
		}
		Material clickedMaterial = clickedBlock.getType();
		if (!player.isSneaking()) {
			if (serverVersion >= 14 && clickedMaterial == Material.SWEET_BERRY_BUSH
					&& ((Ageable) clickedBlock.getBlockData()).getAge() > 1) {
				return false;
			}
			// The following materials only prevent firework launches whilst not sneaking.
			switch (clickedMaterial.name()) {
				case "FURNACE":
				case "DISPENSER":
				case "CHEST":
				case "NOTE_BLOCK":
				case "LEVER":
				case "STONE_BUTTON":
				case "ENDER_CHEST":
				case "BEACON":
				case "ANVIL":
				case "TRAPPED_CHEST":
				case "HOPPER":
				case "DROPPER":
				case "BREWING_STAND":
				case "CRAFTING_TABLE":
				case "ACACIA_BUTTON":
				case "BIRCH_BUTTON":
				case "DARK_OAK_BUTTON":
				case "JUNGLE_BUTTON":
				case "OAK_BUTTON":
				case "SPRUCE_BUTTON":
				case "ACACIA_DOOR":
				case "BIRCH_DOOR":
				case "DARK_OAK_DOOR":
				case "JUNGLE_DOOR":
				case "OAK_DOOR":
				case "SPRUCE_DOOR":
				case "ACACIA_FENCE_GATE":
				case "BIRCH_FENCE_GATE":
				case "DARK_OAK_FENCE_GATE":
				case "JUNGLE_FENCE_GATE":
				case "OAK_FENCE_GATE":
				case "SPRUCE_FENCE_GATE":
				case "ENCHANTING_TABLE":
				case "ACACIA_TRAPDOOR":
				case "BIRCH_TRAPDOOR":
				case "DARK_OAK_TRAPDOOR":
				case "JUNGLE_TRAPDOOR":
				case "OAK_TRAPDOOR":
				case "SPRUCE_TRAPDOOR":
				case "ACACIA_BOAT":
				case "BIRCH_BOAT":
				case "DARK_OAK_BOAT":
				case "JUNGLE_BOAT":
				case "OAK_BOAT":
				case "SPRUCE_BOAT":
				case "BLACK_BED":
				case "BLUE_BED":
				case "BROWN_BED":
				case "CYAN_BED":
				case "GRAY_BED":
				case "GREEN_BED":
				case "LIGHT_BLUE_BED":
				case "LIGHT_GRAY_BED":
				case "LIME_BED":
				case "MAGENTA_BED":
				case "ORANGE_BED":
				case "PINK_BED":
				case "PURPLE_BED":
				case "RED_BED":
				case "WHITE_BED":
				case "YELLOW_BED":
				case "CAKE":
				case "COMPARATOR":
				case "REPEATER":
				case "COMMAND_BLOCK":
				case "ARMOR_STAND":
				case "CHAIN_COMMAND_BLOCK":
				case "REPEATING_COMMAND_BLOCK":
				case "BLACK_SHULKER_BOX":
				case "BLUE_SHULKER_BOX":
				case "BROWN_SHULKER_BOX":
				case "CYAN_SHULKER_BOX":
				case "GRAY_SHULKER_BOX":
				case "GREEN_SHULKER_BOX":
				case "LIGHT_BLUE_SHULKER_BOX":
				case "LIME_SHULKER_BOX":
				case "MAGENTA_SHULKER_BOX":
				case "ORANGE_SHULKER_BOX":
				case "PINK_SHULKER_BOX":
				case "PURPLE_SHULKER_BOX":
				case "RED_SHULKER_BOX":
				case "WHITE_SHULKER_BOX":
				case "YELLOW_SHULKER_BOX":
				case "SHULKER_BOX":
				case "BARREL":
				case "BELL":
				case "BLAST_FURNACE":
				case "CARTOGRAPHY_TABLE":
				case "GRINDSTONE":
				case "LOOM":
				case "SMOKER":
				case "STONECUTTER":
				case "ACACIA_SIGN":
				case "ACACIA_WALL_SIGN":
				case "BIRCH_SIGN":
				case "BIRCH_WALL_SIGN":
				case "DARK_OAK_SIGN":
				case "DARK_OAK_WALL_SIGN":
				case "JUNGLE_SIGN":
				case "JUNGLE_WALL_SIGN":
				case "OAK_SIGN":
				case "OAK_WALL_SIGN":
				case "SPRUCE_SIGN":
				case "SPRUCE_WALL_SIGN":
					// Pre Minecraft 1.14":
				case "SIGN":
				case "WALL_SIGN":
					// Pre Minecraft 1.13":
				case "WORKBENCH":
				case "BURNING_FURNACE":
				case "WOOD_BUTTON":
				case "TRAP_DOOR":
				case "FENCE_GATE":
				case "ENCHANTMENT_TABLE":
				case "WOOD_DOOR":
				case "WOODEN_DOOR":
				case "BOAT":
				case "BED_BLOCK":
				case "CAKE_BLOCK":
				case "REDSTONE_COMPARATOR_OFF":
				case "REDSTONE_COMPARATOR_ON":
				case "DIODE_BLOCK_OFF":
				case "DIODE_BLOCK_ON":
				case "COMMAND":
				case "BOAT_ACACIA":
				case "BOAT_BIRCH":
				case "BOAT_DARK_OAK":
				case "BOAT_JUNGLE":
				case "BOAT_SPRUCE":
				case "COMMAND_REPEATING":
				case "COMMAND_CHAIN":
				case "SILVER_SHULKER_BOX":
				case "SIGN_POST":
					return false;
				default:
					break;
			}
		}
		// The following materials prevent firework launches regardless of whether the player is sneaking or not.
		switch (clickedMaterial.name()) {
			case "PAINTING":
			case "ITEM_FRAME":
			case "MINECART":
			case "HOPPER_MINECART":
			case "TNT_MINECART":
			case "COMMAND_BLOCK_MINECART":
			case "FURNACE_MINECART":
			case "CHEST_MINECART":
				// Pre Minecraft 1.13":
			case "EXPLOSIVE_MINECART":
			case "COMMAND_MINECART":
			case "POWERED_MINECART":
			case "STORAGE_MINECART":
				return false;
			default:
				return true;
		}
	}
}
