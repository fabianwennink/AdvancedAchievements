package com.hm.achievement.listener.statistics;

import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.file.CommentedYamlConfiguration;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;

/**
 * Listener class to deal with Jobs Reborn achievements.
 */
@Singleton
public class VoteListener extends AbstractListener {

    @Inject
    public VoteListener(@Named("main") CommentedYamlConfiguration mainConfig, int serverVersion,
                        Map<String, List<Long>> sortedThresholds, CacheManager cacheManager, RewardParser rewardParser) {
        super(mainConfig, serverVersion, sortedThresholds, cacheManager, rewardParser);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerVote(VotifierEvent event) {
        if (event.getVote().getUsername() == null || event.getVote().getUsername().equals("")) {
            return;
        }

        Player player = Bukkit.getPlayer(event.getVote().getUsername());
        if(player != null) {
            updateStatisticAndAwardAchievementsIfAvailable(player, NormalAchievements.VOTES, 1);
        }
    }
}