package com.cubeyu.ctools;

import com.cubeyu.ctools.modules.WelcomeManager;
import com.cubeyu.ctools.modules.RulesManager;
import com.cubeyu.ctools.modules.CommandBlocker;
import com.cubeyu.ctools.modules.LeaveManager;
import com.cubeyu.ctools.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CTools extends JavaPlugin implements CommandExecutor, TabCompleter {
    private static CTools instance;
    private FileConfiguration config;
    private Logger logger;
    private WelcomeManager welcomeManager;
    private LeaveManager leaveManager;
    private RulesManager rulesManager;
    private CommandBlocker commandBlocker;

    @Override
    public void onEnable() {
        // 初始化实例
        instance = this;
        logger = getLogger();

        // 保存默认配置
        saveDefaultConfig();
        config = getConfig();



        // 初始化模块
        initializeModules();

        // 注册主命令
        getCommand("ctools").setExecutor(this);
        getCommand("ctools").setTabCompleter(this);

        // 炫酷的插件启用日志
        logger.info("------------------------------------------");
        logger.info("          ╔═╗╔╦╗╔═╗╦  ╦╔═╗╔═╗              ");
        logger.info("          ║ ║ ║ ║  ║  ║║╣ ╠╣               ");
        logger.info("          ╚═╝ ╩ ╚═╝╩═╝╩╚═╝╚                ");
        logger.info("                                           ");
        logger.info("        CTools 插件已成功启用！             ");
        logger.info("        版本: " + getDescription().getVersion());
        logger.info("        作者QQ：3144855127                  ");
        logger.info("------------------------------------------");
    }

    @Override
    public void onDisable() {
        logger.info("CTools 插件已禁用！作者QQ：3144855127");
    }

    private void initializeModules() {
        // 检查配置并初始化各个模块
        if (config.getBoolean("modules.welcome", true)) {
            welcomeManager = new WelcomeManager(this);
            logger.info("欢迎模块已启用");
        }

        if (config.getBoolean("modules.leave", true)) {
            leaveManager = new LeaveManager(this);
            logger.info("退服提示模块已启用");
        }

        if (config.getBoolean("modules.rules", true)) {
            rulesManager = new RulesManager(this);
            logger.info("规则模块已启用");
        }

        if (config.getBoolean("modules.commandblock", true)) {
            commandBlocker = new CommandBlocker(this);
            logger.info("命令禁用模块已启用");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 统一使用admin权限检查
        if (!sender.hasPermission("ctools.admin")) {
            sender.sendMessage(ColorUtils.colorize("&c你没有权限使用此命令！"));
            return true;
        }
        
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(ColorUtils.colorize("&6=== CTools 命令帮助 ==="));
            sender.sendMessage(ColorUtils.colorize("&e/rules &7- 查看萌新指南"));
            sender.sendMessage(ColorUtils.colorize("&e/ctools help &7- 查看插件帮助"));
            sender.sendMessage(ColorUtils.colorize("&e/ctools reload &7- 重载插件配置"));
            sender.sendMessage(ColorUtils.colorize("&e/blockcmd <add&f&l|&eremove&f&l|&elist> [命令:不要带/] &7- 管理命令"));
            sender.sendMessage(ColorUtils.colorize("&a作者QQ：3144855127"));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            // 调用reloadPlugin方法处理重载
            reloadPlugin();

            sender.sendMessage(ColorUtils.colorize("&aCTools 插件配置已重载！作者QQ：3144855127"));
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1 && sender.hasPermission("ctools.admin")) {
            completions.add("help");
            completions.add("reload");
        }
        return completions;
    }

    private void reloadPlugin() {
        // 检查配置文件是否存在，如果不存在则重新生成
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            logger.info("配置文件不存在，正在重新生成默认配置文件...");
            saveDefaultConfig();
        }
        
        // 重新加载配置文件
        reloadConfig();
        config = getConfig();
        
        // 使用现有模块实例的reload方法，避免重复注册事件监听器
        if (welcomeManager != null) {
            welcomeManager.reload();
        }
        if (leaveManager != null) {
            leaveManager.reload();
        }
        if (rulesManager != null) {
            rulesManager.reload();
        }
        if (commandBlocker != null) {
            commandBlocker.reload();
        }
    }

    public static CTools getInstance() {
        return instance;
    }
}