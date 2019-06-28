package com.hm.achievement.listener.statistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.file.CommentedYamlConfiguration;

@Singleton
public class PlayerCommandsListener extends AbstractListener {

	@Inject
	public PlayerCommandsListener(@Named("main") CommentedYamlConfiguration mainConfig, int serverVersion,
			Map<String, List<Long>> sortedThresholds, CacheManager cacheManager, RewardParser rewardParser) {
		super(mainConfig, serverVersion, sortedThresholds, cacheManager, rewardParser);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();

		MultipleAchievements category = MultipleAchievements.PLAYERCOMMANDS;
		if (!shouldIncreaseBeTakenIntoAccount(player, category)) {
			return;
		}

		List<String> equivalentCommands = getEquivalentCommands(event.getMessage());
		Set<String> foundAchievements = new HashSet<>();
		for (String groupedPrefixes : mainConfig.getShallowKeys(category.toString())) {
			for (String prefix : StringUtils.split(groupedPrefixes, '|')) {
				if (player.hasPermission(category.toPermName() + '.' + StringUtils.deleteWhitespace(prefix))) {
					for (String equivalentCommand : equivalentCommands) {
						if (equivalentCommand.startsWith(prefix)) {
							foundAchievements.add(groupedPrefixes);
						}
					}
				}
			}
		}
		foundAchievements.forEach(achievement -> updateStatisticAndAwardAchievementsIfAvailable(player, category,
				achievement, 1));
	}

	/**
	 * Computes a list containing equivalent commands of an input command. For instance, if input is "/aach stats", the
	 * returned list is: ["aach stats", "advancedachievements stats", "aachievements stats", "aa stats"]
	 * 
	 * @param command
	 * @return the list of commands have the same effect as the input one
	 */
	private List<String> getEquivalentCommands(String command) {
		int firstSpaceIndex = command.indexOf(' ');
		String commandName;
		String commandParameters;
		if (firstSpaceIndex >= 0) {
			commandName = command.substring(1, firstSpaceIndex);
			// Command parameters start with an initial space.
			commandParameters = command.substring(firstSpaceIndex).toLowerCase();
		} else {
			commandName = command.substring(1);
			commandParameters = "";
		}

		PluginCommand pluginCommand = Bukkit.getPluginCommand(commandName);
		if (pluginCommand == null || pluginCommand.getAliases() == null) {
			return Arrays.asList(commandName.toLowerCase() + commandParameters);
		}

		List<String> equivalentCommands = new ArrayList<>(pluginCommand.getAliases().size() + 1);
		// Aliases don't contain the main plugin command, add it to the returned list.
		equivalentCommands.add(pluginCommand.getName().toLowerCase() + commandParameters);
		for (String alias : pluginCommand.getAliases()) {
			equivalentCommands.add(alias.toLowerCase() + commandParameters);
		}
		return equivalentCommands;
	}
}
