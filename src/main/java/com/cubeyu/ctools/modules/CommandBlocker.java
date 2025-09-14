package com.cubeyu.ctools.modules;

import com.cubeyu.ctools.CTools;
import com.cubeyu.ctools.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class CommandBlocker implements Listener, CommandExecutor, TabCompleter {
    private final CTools plugin;
    private boolean enabled;
    private String mode;
    private Set<String> blockedCommands;
    private Set<Pattern> blockedPatterns;
    private String message;

    public CommandBlocker(CTools plugin) {
        this.plugin = plugin;
        this.blockedCommands = new HashSet<>();
        this.blockedPatterns = new HashSet<>();
        loadConfig();
        
        // 注册命令和事件监听器
        if (enabled) {
            plugin.getCommand("blockcmd").setExecutor(this);
            plugin.getCommand("blockcmd").setTabCompleter(this);
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    public void reload() {
        // 卸载事件监听器
        if (enabled) {
            PlayerCommandPreprocessEvent.getHandlerList().unregister(this);
        }
        
        // 重新加载配置
        loadConfig();
        
        // 重新注册事件监听器
        if (enabled) {
            plugin.getCommand("blockcmd").setExecutor(this);
            plugin.getCommand("blockcmd").setTabCompleter(this);
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    private void loadConfig() {
        // 检查commandblock模块是否启用
        this.enabled = plugin.getConfig().getBoolean("modules.commandblock", true);
        
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("commandblock");
        if (config != null) {
            this.mode = config.getString("mode", "blacklist");
            this.message = config.getString("block-message", "&c你没有权限使用此命令！");
            
            // 加载被禁用的命令
            this.blockedCommands.clear();
            this.blockedPatterns.clear();
            
            List<String> commands = config.getStringList("blocked-commands");
            for (String cmd : commands) {
                if (cmd.contains("*")) {
                    // 将通配符转换为正则表达式
                    String pattern = cmd.replace("*", ".*");
                    blockedPatterns.add(Pattern.compile(pattern));
                } else {
                    blockedCommands.add(cmd.toLowerCase());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (!enabled) return;
        
        Player player = event.getPlayer();
        if (player.hasPermission("ctools.commandblock.bypass")) return;

        String command = event.getMessage().substring(1).split(" ")[0].toLowerCase();
        
        // 检查命令是否被禁用
        if (isCommandBlocked(command)) {
            event.setCancelled(true);
            player.sendMessage(ColorUtils.colorize(message));
        }
    }

    private boolean isCommandBlocked(String command) {
        if (mode.equalsIgnoreCase("blacklist")) {
            // 黑名单模式：检查命令是否在禁用列表中
            if (blockedCommands.contains(command)) {
                return true;
            }
            // 检查是否匹配任何正则表达式
            for (Pattern pattern : blockedPatterns) {
                if (pattern.matcher(command).matches()) {
                    return true;
                }
            }
            return false;
        } else {
            // 白名单模式：检查命令是否在允许列表中
            if (blockedCommands.contains(command)) {
                return false;
            }
            // 检查是否匹配任何正则表达式
            for (Pattern pattern : blockedPatterns) {
                if (pattern.matcher(command).matches()) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ctools.blockcmd")) {
            sender.sendMessage(ColorUtils.colorize("&c你没有权限使用此命令！"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ColorUtils.colorize("&c用法: /blockcmd <add|remove|list> [命令]"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "add":
                if (args.length < 2) {
                    sender.sendMessage(ColorUtils.colorize("&c请指定要禁用的命令！"));
                    return true;
                }
                addBlockedCommand(args[1]);
                sender.sendMessage(ColorUtils.colorize("&a已添加命令到禁用列表！"));
                break;
            case "remove":
                if (args.length < 2) {
                    sender.sendMessage(ColorUtils.colorize("&c请指定要解除禁用的命令！"));
                    return true;
                }
                removeBlockedCommand(args[1]);
                sender.sendMessage(ColorUtils.colorize("&a已从禁用列表中移除命令！"));
                break;
            case "list":
                listBlockedCommands(sender);
                break;
            default:
                sender.sendMessage(ColorUtils.colorize("&c无效的子命令！"));
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("add");
            completions.add("remove");
            completions.add("list");
        }
        return completions;
    }

    private void addBlockedCommand(String command) {
        if (!blockedCommands.contains(command)) {
            blockedCommands.add(command);
            saveConfig();
        }
    }

    private void removeBlockedCommand(String command) {
        blockedCommands.remove(command);
        saveConfig();
    }

    private void listBlockedCommands(CommandSender sender) {
        if (blockedCommands.isEmpty()) {
            sender.sendMessage(ColorUtils.colorize("&c当前没有禁用的命令！"));
            return;
        }

        sender.sendMessage(ColorUtils.colorize("&6=== 已禁用的命令列表 ==="));
        for (String cmd : blockedCommands) {
            sender.sendMessage(ColorUtils.colorize("&7- " + cmd));
        }
    }

    private void saveConfig() {
        plugin.getConfig().set("commandblock.blocked-commands", new ArrayList<>(blockedCommands));
        plugin.saveConfig();
    }
}