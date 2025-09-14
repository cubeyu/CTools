package com.cubeyu.ctools.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GuiUtils {
    
    /**
     * 创建一个新的GUI界面
     * @param title 界面标题
     * @param size 界面大小（必须是9的倍数）
     * @return 创建的界面
     */
    public static Inventory createGui(String title, int size) {
        return Bukkit.createInventory(null, size, ColorUtils.colorize(title));
    }

    /**
     * 创建一个GUI按钮
     * @param material 物品材质
     * @param name 物品名称
     * @param lore 物品描述
     * @return 创建的按钮
     */
    public static ItemStack createButton(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.displayName(ColorUtils.colorize(name));
            
            if (lore != null && lore.length > 0) {
                List<Component> loreList = new ArrayList<>();
                for (String line : lore) {
                    loreList.add(ColorUtils.colorize(line));
                }
                meta.lore(loreList);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }

    /**
     * 创建一个翻页按钮
     * @param isNext 是否是下一页按钮
     * @return 创建的翻页按钮
     */
    public static ItemStack createPageButton(boolean isNext) {
        return createButton(
            isNext ? Material.ARROW : Material.BARRIER,
            isNext ? "&a下一页" : "&c上一页",
            isNext ? "&7点击查看下一页" : "&7点击返回上一页"
        );
    }
} 