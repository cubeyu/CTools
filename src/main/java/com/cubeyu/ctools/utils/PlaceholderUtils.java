package com.cubeyu.ctools.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import me.clip.placeholderapi.PlaceholderAPI;

public class PlaceholderUtils {
    
    /**
     * 检查PlaceholderAPI是否已安装
     */
    public static boolean isPlaceholderAPIEnabled() {
        return Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }
    
    /**
     * 解析文本中的PlaceholderAPI变量
     */
    public static String parsePlaceholders(Player player, String text) {
        if (isPlaceholderAPIEnabled() && player != null) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
    }
}