package com.hm.achievement.command.executable;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.text.StringEscapeUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.lang.LangHelper;
import com.hm.achievement.lang.command.CmdLang;
import com.hm.achievement.lang.command.HelpLang;
import com.hm.mcshared.file.CommentedYamlConfiguration;
import com.hm.mcshared.particle.FancyMessageSender;

/**
 * Class in charge of displaying the plugin's help (/aach help).
 *
 * @author Pyves
 */
@Singleton
@CommandSpec(name = "help", permission = "", minArgs = 0, maxArgs = Integer.MAX_VALUE)
public class HelpCommand extends AbstractCommand {

	private final int serverVersion;
	private final Logger logger;

	private ChatColor configColor;
	private String configIcon;

	private String langCommandList;
	private String langCommandTop;
	private String langCommandBook;
	private String langCommandWeek;
	private String langCommandStats;
	private String langCommandMonth;
	private String langCommandToggle;
	private String langCommandReload;
	private String langCommandGenerate;
	private String langCommandInspect;
	private String langCommandGive;
	private String langCommandAdd;
	private String langCommandReset;
	private String langCommandCheck;
	private String langCommandDelete;
	private String langTip;

	@Inject
	public HelpCommand(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("lang") CommentedYamlConfiguration langConfig, StringBuilder pluginHeader, int serverVersion,
			Logger logger) {
		super(mainConfig, langConfig, pluginHeader);
		this.serverVersion = serverVersion;
		this.logger = logger;
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		configColor = ChatColor.getByChar(mainConfig.getString("Color", "5"));
		configIcon = StringEscapeUtils.unescapeJava(mainConfig.getString("Icon", "\u2618"));

		langCommandList = header("/aach list") + LangHelper.get(HelpLang.LIST, langConfig);
		langCommandTop = header("/aach top") + LangHelper.get(HelpLang.TOP, langConfig);
		langCommandBook = header("/aach book") + LangHelper.get(HelpLang.BOOK, langConfig);
		langCommandWeek = header("/aach week") + LangHelper.get(HelpLang.WEEK, langConfig);
		langCommandStats = header("/aach stats") + LangHelper.get(HelpLang.STATS, langConfig);
		langCommandMonth = header("/aach month") + LangHelper.get(HelpLang.MONTH, langConfig);
		langCommandToggle = header("/aach toggle") + LangHelper.get(HelpLang.TOGGLE, langConfig);
		langCommandReload = header("/aach reload") + LangHelper.get(HelpLang.RELOAD, langConfig);
		langCommandGenerate = header("/aach generate") + LangHelper.get(HelpLang.GENERATE, langConfig);
		langCommandGive = header("/aach give <ach> player") + translateColorCodes(LangHelper.getEachReplaced(HelpLang.GIVE,
			langConfig, new String[] { "ACH", "NAME" }, new String[] { "ach", "player" }));
		langCommandInspect = header("/aach inspect <ach>")
				+ translateColorCodes(LangHelper.getReplacedOnce(HelpLang.INSPECT, "ACH", "ach", langConfig));
		langCommandAdd = header("/aach add &ox cat player") + LangHelper.get(HelpLang.ADD, langConfig);
		langCommandReset = header("/aach reset <cat> player")
				+ LangHelper.getReplacedOnce(HelpLang.RESET, "CAT", "cat", langConfig);
		langCommandCheck = header("/aach check <ach> player")
				+ translateColorCodes(LangHelper.getEachReplaced(HelpLang.CHECK, langConfig, new String[] { "ACH", "NAME" },
						new String[] { "ach", "player" }));
		langCommandDelete = header("/aach delete <ach> player")
				+ translateColorCodes(LangHelper.getEachReplaced(HelpLang.DELETE,
						langConfig, new String[] { "ACH", "NAME" }, new String[] { "ach", "player" }));
		langTip = ChatColor.GRAY + translateColorCodes(LangHelper.get(CmdLang.AACH_TIP, langConfig));
	}

	private String header(String command) {
		return pluginHeader.toString() + configColor + command + ChatColor.GRAY + " > ";
	}

	@Override
	void onExecute(CommandSender sender, String[] args) {
		// Header.
		sender.sendMessage(configColor + "------------ " + configIcon + translateColorCodes(" &lCustom Achievements ")
				+ configColor + configIcon + configColor + " ------------");

		if (sender.hasPermission("achievement.list")) {
			sender.sendMessage(langCommandList);
		}

		if (sender.hasPermission("achievement.top")) {
			sender.sendMessage(langCommandTop);
		}

		if (sender.hasPermission("achievement.book")) {
			sender.sendMessage(langCommandBook);
		}

		if (sender.hasPermission("achievement.week")) {
			sender.sendMessage(langCommandWeek);
		}

		if (sender.hasPermission("achievement.stats")) {
			sender.sendMessage(langCommandStats);
		}

		if (sender.hasPermission("achievement.month")) {
			sender.sendMessage(langCommandMonth);
		}

		if (sender.hasPermission("achievement.toggle")) {
			sender.sendMessage(langCommandToggle);
		}

		if (sender.hasPermission("achievement.reload")) {
			sender.sendMessage(langCommandReload);
		}

		if (serverVersion >= 12 && sender.hasPermission("achievement.generate")) {
			sender.sendMessage(langCommandGenerate);
		}

		if (sender.hasPermission("achievement.inspect")) {
			sender.sendMessage(langCommandInspect);
		}

		if (sender.hasPermission("achievement.give")) {
			sender.sendMessage(langCommandGive);
		}

		if (sender.hasPermission("achievement.add")) {
			sender.sendMessage(langCommandAdd);
		}

		if (sender.hasPermission("achievement.reset")) {
			sender.sendMessage(langCommandReset);
		}

		if (sender.hasPermission("achievement.check")) {
			sender.sendMessage(langCommandCheck);
		}

		if (sender.hasPermission("achievement.delete")) {
			sender.sendMessage(langCommandDelete);
		}

		sender.sendMessage(configColor + "------------ " + configIcon + translateColorCodes(" &lCustom Achievements ")
			+ configColor + configIcon + configColor + " ------------");

		// Empty line.
		sender.sendMessage(configColor + " ");
	}
}
