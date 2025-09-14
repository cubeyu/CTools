package com.cubeyu.ctools.modules;

import com.cubeyu.ctools.CTools;
import com.cubeyu.ctools.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;

public class WelcomeManager implements Listener {
    private final CTools plugin;
    private boolean enabled;
    private int delay;
    private List<String> messages;
    private boolean soundEnabled;
    private String soundType;

    public WelcomeManager(CTools plugin) {
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
            PlayerJoinEvent.getHandlerList().unregister(this);
        }
        
        // 重新加载配置
        loadConfig();
        
        // 重新注册事件监听器
        if (enabled) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    private void loadConfig() {
        // 检查welcome模块是否启用
        this.enabled = plugin.getConfig().getBoolean("modules.welcome", true);
        
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("welcome");
        if (config != null) {
            this.delay = config.getInt("delay", 20);
            this.messages = config.getStringList("messages");
            
            // 加载声音配置
            this.soundEnabled = config.getBoolean("sound.enabled", true);
            this.soundType = config.getString("sound.type", "ENTITY_PLAYER_LEVELUP");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!enabled) return;

        Player player = event.getPlayer();
        
        // 首先取消原版的进入服务器提示消息（对所有玩家都有效，包括管理员）
        event.setJoinMessage(null);
        
        // 注意：不再检查权限，所有玩家都将看到欢迎消息
        // if (player.hasPermission("ctools.welcome.bypass")) return;
        
        // 延迟发送自定义欢迎消息
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (String message : messages) {
                // 替换占位符
                String processedMessage = message
                    .replace("%player_name%", player.getName())
                    .replace("%server%", plugin.getServer().getName());
                
                // 发送带颜色的消息给所有在线玩家
                Component component = ColorUtils.colorize(processedMessage);
                Bukkit.getServer().sendMessage(component);
            }
            
            // 播放提示音给所有在线玩家
            if (soundEnabled) {
                try {
                    Sound sound = Sound.valueOf(soundType);
                    float volume = 1.0f;
                    float pitch = 1.0f;
                    
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        onlinePlayer.playSound(onlinePlayer.getLocation(), sound, volume, pitch);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("无法播放欢迎音效: 无效的声音类型 '" + soundType + "'");
                }
            }
        }, delay);
    }
}