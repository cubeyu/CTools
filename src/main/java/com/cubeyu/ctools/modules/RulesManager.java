package com.cubeyu.ctools.modules;

import com.cubeyu.ctools.CTools;
import com.cubeyu.ctools.utils.ColorUtils;
import com.cubeyu.ctools.utils.GuiUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RulesManager implements Listener, CommandExecutor, TabCompleter {
    private final CTools plugin;
    private boolean enabled;
    private String title;
    private List<String> content;
    private int linesPerPage;
    private final Map<Player, Integer> playerPages;

    public RulesManager(CTools plugin) {
        this.plugin = plugin;
        this.playerPages = new HashMap<>();
        loadConfig();
        
        // 注册命令和事件监听器
        if (enabled) {
            plugin.getCommand("rules").setExecutor(this);
            plugin.getCommand("rules").setTabCompleter(this);
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    public void reload() {
        // 卸载事件监听器
        if (enabled) {
            InventoryClickEvent.getHandlerList().unregister(this);
        }
        
        // 重新加载配置
        loadConfig();
        
        // 重新注册事件监听器
        if (enabled) {
            plugin.getCommand("rules").setExecutor(this);
            plugin.getCommand("rules").setTabCompleter(this);
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    private void loadConfig() {
        // 检查rules模块是否启用
        this.enabled = plugin.getConfig().getBoolean("modules.rules", true);
        
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("rules");
        if (config != null) {
            this.title = config.getString("title", "&6=== 萌新指南 ===");
            this.content = config.getStringList("content");
            this.linesPerPage = config.getInt("lines-per-page", 45);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtils.colorize("&c该命令只能由玩家执行！"));
            return true;
        }

        Player player = (Player) sender;
        showRules(player, 0);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().title().equals(ColorUtils.colorize(title))) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        if (clickedItem.getType() == Material.ARROW) {
            // 下一页
            int currentPage = playerPages.getOrDefault(player, 0);
            showRules(player, currentPage + 1);
        } else if (clickedItem.getType() == Material.BARRIER) {
            // 上一页
            int currentPage = playerPages.getOrDefault(player, 0);
            showRules(player, currentPage - 1);
        }
    }

    private void showRules(Player player, int page) {
        int totalPages = (int) Math.ceil((double) content.size() / linesPerPage);
        if (page < 0 || page >= totalPages) return;

        // 创建GUI
        Inventory gui = GuiUtils.createGui(title, 54);
        playerPages.put(player, page);

        // 添加规则内容
        int startIndex = page * linesPerPage;
        int endIndex = Math.min(startIndex + linesPerPage, content.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            String rule = content.get(i);
            ItemStack item = GuiUtils.createButton(
                Material.PAPER,
                rule,
                "&7规则 #" + (i + 1)
            );
            gui.addItem(item);
        }

        // 添加翻页按钮
        if (page > 0) {
            gui.setItem(45, GuiUtils.createPageButton(false));
        }
        if (page < totalPages - 1) {
            gui.setItem(53, GuiUtils.createPageButton(true));
        }

        player.openInventory(gui);
    }
}