package com.cubeyu.ctools.modules;

import com.cubeyu.ctools.CTools;
import com.cubeyu.ctools.utils.ColorUtils;
import com.cubeyu.ctools.utils.PlaceholderUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public class LeaveManager implements Listener {
    private final CTools plugin;
    private boolean enabled;
    private int delay;
    private List<String> messages;
    private boolean soundEnabled;
    private String soundType;

    public LeaveManager(CTools plugin) {
        this.plugin = plugin;
        loadConfig();
        
        // 注册事件监听器
        if (enabled) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    public void reload() {
        // 卸载事件监听器
        if (enabled) {
            PlayerQuitEvent.getHandlerList().unregister(this);
        }
        
        // 重新加载配置
        loadConfig();
        
        // 重新注册事件监听器
        if (enabled) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    private void loadConfig() {
        // 检查leave模块是否启用
        this.enabled = plugin.getConfig().getBoolean("modules.leave", true);
        
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("leave");
        if (config != null) {
            this.delay = config.getInt("delay", 20);
            this.messages = config.getStringList("messages");
            
            // 加载声音配置
            this.soundEnabled = config.getBoolean("sound.enabled", true);
            this.soundType = config.getString("sound.type", "ENTITY_ITEM_BREAK");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!enabled) return;

        Player player = event.getPlayer();
        
        // 取消原版的退出服务器提示消息
        event.setQuitMessage(null);
        
        // 延迟发送自定义退出消息
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (String message : messages) {
                // 替换基础占位符
                String processedMessage = message
                    .replace("%player_name%", player.getName())
                    .replace("%server%", plugin.getServer().getName());
                
                // 使用PlaceholderAPI解析更复杂的变量
                processedMessage = PlaceholderUtils.parsePlaceholders(player, processedMessage);
                
                // 发送带颜色的消息给所有在线玩家
                Component component = ColorUtils.colorize(processedMessage);
                Bukkit.getServer().sendMessage(component);
            }
            
            // 播放提示音给所有在线玩家（不包括退出的玩家）
            if (soundEnabled) {
                try {
                    Sound sound = Sound.valueOf(soundType);
                    float volume = 1.0f;
                    float pitch = 1.0f;
                    
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        // 跳过退出的玩家
                        if (!onlinePlayer.getUniqueId().equals(player.getUniqueId())) {
                            onlinePlayer.playSound(onlinePlayer.getLocation(), sound, volume, pitch);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("无法播放退出音效: 无效的声音类型 '" + soundType + "'");
                }
            }
        }, delay);
    }
}