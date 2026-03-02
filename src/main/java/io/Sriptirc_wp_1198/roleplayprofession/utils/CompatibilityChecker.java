package io.Sriptirc_wp_1198.roleplayprofession.utils;

import io.Sriptirc_wp_1198.roleplayprofession.Roleplayprofession;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

/**
 * 兼容性检查工具
 * 检查插件在不同Minecraft版本和服务器软件上的兼容性
 */
public class CompatibilityChecker {
    private final Roleplayprofession plugin;
    private final Set<String> missingMaterials;
    private final Set<String> missingDamageCauses;
    
    // 支持的服务器软件
    private static final Set<String> SUPPORTED_SERVERS = new HashSet<>();
    
    static {
        SUPPORTED_SERVERS.add("CraftBukkit");
        SUPPORTED_SERVERS.add("Spigot");
        SUPPORTED_SERVERS.add("Paper");
        SUPPORTED_SERVERS.add("Purpur");
        SUPPORTED_SERVERS.add("Folia");
    }
    
    public CompatibilityChecker(Roleplayprofession plugin) {
        this.plugin = plugin;
        this.missingMaterials = new HashSet<>();
        this.missingDamageCauses = new HashSet<>();
    }
    
    /**
     * 执行全面的兼容性检查
     */
    public boolean checkCompatibility() {
        plugin.getLogger().info("开始兼容性检查...");
        
        boolean allPassed = true;
        
        // 检查服务器软件
        if (!checkServerSoftware()) {
            allPassed = false;
        }
        
        // 检查Bukkit API版本
        if (!checkBukkitVersion()) {
            allPassed = false;
        }
        
        // 检查Material兼容性
        if (!checkMaterialCompatibility()) {
            allPassed = false;
        }
        
        // 检查DamageCause兼容性
        if (!checkDamageCauseCompatibility()) {
            allPassed = false;
        }
        
        // 检查其他API
        if (!checkOtherAPIs()) {
            allPassed = false;
        }
        
        if (allPassed) {
            plugin.getLogger().info("兼容性检查通过！");
        } else {
            plugin.getLogger().warning("兼容性检查发现一些问题，插件可能无法正常工作");
        }
        
        return allPassed;
    }
    
    /**
     * 检查服务器软件
     */
    private boolean checkServerSoftware() {
        String serverName = Bukkit.getName();
        String serverVersion = Bukkit.getVersion();
        
        plugin.getLogger().info("服务器: " + serverName);
        plugin.getLogger().info("版本: " + serverVersion);
        
        boolean isSupported = false;
        for (String supported : SUPPORTED_SERVERS) {
            if (serverName.contains(supported)) {
                isSupported = true;
                break;
            }
        }
        
        if (!isSupported) {
            plugin.getLogger().warning("警告: 服务器软件 " + serverName + " 可能不完全支持");
            plugin.getLogger().warning("建议使用 Spigot、Paper 或 Purpur 以获得最佳兼容性");
            return false;
        }
        
        return true;
    }
    
    /**
     * 检查Bukkit API版本
     */
    private boolean checkBukkitVersion() {
        try {
            String bukkitVersion = Bukkit.getBukkitVersion();
            plugin.getLogger().info("Bukkit版本: " + bukkitVersion);
            
            // 检查是否支持1.20.x
            if (bukkitVersion.contains("1.20")) {
                return true;
            } else {
                plugin.getLogger().warning("警告: 插件为1.20.x设计，当前服务器版本: " + bukkitVersion);
                plugin.getLogger().warning("可能需要进行版本适配");
                return false;
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "无法获取Bukkit版本", e);
            return false;
        }
    }
    
    /**
     * 检查Material兼容性
     */
    private boolean checkMaterialCompatibility() {
        plugin.getLogger().info("检查Material兼容性...");
        
        // 插件中使用的所有Material
        Material[] materialsToCheck = {
            // 职业图标
            Material.IRON_SWORD,
            Material.WATER_BUCKET,
            Material.POTION,
            Material.PAPER,
            Material.COOKED_BEEF,
            Material.CAKE,
            Material.WHEAT,
            Material.BARRIER,
            
            // GUI物品
            Material.BLACK_STAINED_GLASS_PANE,
            Material.BLUE_STAINED_GLASS_PANE,
            Material.GREEN_STAINED_GLASS_PANE,
            Material.YELLOW_STAINED_GLASS_PANE,
            Material.PURPLE_STAINED_GLASS_PANE,
            Material.CYAN_STAINED_GLASS_PANE,
            Material.ARROW,
            Material.EMERALD,
            Material.EXPERIENCE_BOTTLE,
            Material.BOOK,
            Material.PLAYER_HEAD,
            Material.CLOCK,
            
            // 作物和食物
            Material.CARROTS,
            Material.POTATOES,
            Material.BEETROOTS,
            Material.MELON,
            Material.PUMPKIN,
            Material.SUGAR_CANE,
            Material.CACTUS,
            Material.BAMBOO,
            Material.KELP,
            Material.SEA_PICKLE,
            Material.SWEET_BERRY_BUSH,
            Material.PUMPKIN_PIE,
            Material.COOKIE,
            Material.BREAD,
            Material.GOLDEN_APPLE,
            Material.ENCHANTED_GOLDEN_APPLE
        };
        
        for (Material material : materialsToCheck) {
            try {
                // 尝试获取Material，如果不存在会抛出异常
                Material.valueOf(material.name());
            } catch (IllegalArgumentException e) {
                missingMaterials.add(material.name());
                plugin.getLogger().warning("不支持的Material: " + material.name());
            }
        }
        
        if (!missingMaterials.isEmpty()) {
            plugin.getLogger().warning("发现 " + missingMaterials.size() + " 个不支持的Material");
            return false;
        }
        
        return true;
    }
    
    /**
     * 检查DamageCause兼容性
     */
    private boolean checkDamageCauseCompatibility() {
        plugin.getLogger().info("检查DamageCause兼容性...");
        
        // 插件中使用的所有DamageCause
        EntityDamageEvent.DamageCause[] causesToCheck = {
            EntityDamageEvent.DamageCause.FIRE,
            EntityDamageEvent.DamageCause.FIRE_TICK,
            EntityDamageEvent.DamageCause.LAVA
        };
        
        for (EntityDamageEvent.DamageCause cause : causesToCheck) {
            try {
                // 尝试获取DamageCause，如果不存在会抛出异常
                EntityDamageEvent.DamageCause.valueOf(cause.name());
            } catch (IllegalArgumentException e) {
                missingDamageCauses.add(cause.name());
                plugin.getLogger().warning("不支持的DamageCause: " + cause.name());
            }
        }
        
        if (!missingDamageCauses.isEmpty()) {
            plugin.getLogger().warning("发现 " + missingDamageCauses.size() + " 个不支持的DamageCause");
            return false;
        }
        
        return true;
    }
    
    /**
     * 检查其他API
     */
    private boolean checkOtherAPIs() {
        plugin.getLogger().info("检查其他API兼容性...");
        
        boolean allPassed = true;
        
        // 检查YAML配置API
        try {
            Class.forName("org.bukkit.configuration.file.YamlConfiguration");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("缺少YAML配置API，插件无法运行！");
            allPassed = false;
        }
        
        // 检查事件API
        try {
            Class.forName("org.bukkit.event.Event");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("缺少事件API，插件无法运行！");
            allPassed = false;
        }
        
        // 检查调度器API
        try {
            Class.forName("org.bukkit.scheduler.BukkitScheduler");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("缺少调度器API，插件无法运行！");
            allPassed = false;
        }
        
        // 检查命令API
        try {
            Class.forName("org.bukkit.command.CommandExecutor");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("缺少命令API，插件无法运行！");
            allPassed = false;
        }
        
        return allPassed;
    }
    
    /**
     * 获取不支持的Material列表
     */
    public Set<String> getMissingMaterials() {
        return new HashSet<>(missingMaterials);
    }
    
    /**
     * 获取不支持的DamageCause列表
     */
    public Set<String> getMissingDamageCauses() {
        return new HashSet<>(missingDamageCauses);
    }
    
    /**
     * 生成兼容性报告
     */
    public String generateCompatibilityReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== 兼容性检查报告 ===\n");
        report.append("服务器: ").append(Bukkit.getName()).append("\n");
        report.append("版本: ").append(Bukkit.getVersion()).append("\n");
        report.append("Bukkit版本: ").append(Bukkit.getBukkitVersion()).append("\n");
        report.append("\n");
        
        if (!missingMaterials.isEmpty()) {
            report.append("不支持的Material (").append(missingMaterials.size()).append("):\n");
            for (String material : missingMaterials) {
                report.append("  - ").append(material).append("\n");
            }
            report.append("\n");
        }
        
        if (!missingDamageCauses.isEmpty()) {
            report.append("不支持的DamageCause (").append(missingDamageCauses.size()).append("):\n");
            for (String cause : missingDamageCauses) {
                report.append("  - ").append(cause).append("\n");
            }
            report.append("\n");
        }
        
        // 检查Folia支持
        if (PerformanceUtils.isFoliaSupported()) {
            report.append("✓ 支持Folia多线程\n");
        } else {
            report.append("✗ 不支持Folia多线程\n");
        }
        
        // 检查TPS
        double tps = PerformanceUtils.getServerTPS();
        report.append("服务器TPS: ").append(String.format("%.2f", tps)).append("\n");
        
        PerformanceUtils.PerformanceStatus status = PerformanceUtils.getPerformanceStatus();
        report.append("性能状态: ").append(status).append("\n");
        
        return report.toString();
    }
    
    /**
     * 获取替代Material（用于兼容性修复）
     */
    public static Material getAlternativeMaterial(Material original) {
        // 为不存在的Material提供替代品
        switch (original.name()) {
            case "SWEET_BERRY_BUSH":
                // 如果甜浆果丛不存在，使用甜浆果
                try {
                    return Material.valueOf("SWEET_BERRIES");
                } catch (IllegalArgumentException e) {
                    return Material.APPLE; // 备用
                }
            case "SEA_PICKLE":
                return Material.KELP;
            case "BAMBOO":
                return Material.SUGAR_CANE;
            default:
                // 尝试使用更通用的替代品
                if (original.name().contains("STAINED_GLASS_PANE")) {
                    try {
                        return Material.valueOf("GLASS_PANE");
                    } catch (IllegalArgumentException e) {
                        return Material.GLASS;
                    }
                }
                return Material.STONE; // 最终备用
        }
    }
    
    /**
     * 检查并修复Material引用
     */
    public static Material getSafeMaterial(String materialName, Material defaultMaterial) {
        try {
            return Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            // 尝试获取替代品
            try {
                Material original = Material.valueOf(materialName);
                return getAlternativeMaterial(original);
            } catch (IllegalArgumentException e2) {
                return defaultMaterial;
            }
        }
    }
    
    /**
     * 检查插件是否应该启用
     */
    public boolean shouldEnablePlugin() {
        // 如果缺少关键API，不应该启用插件
        try {
            Class.forName("org.bukkit.configuration.file.YamlConfiguration");
            Class.forName("org.bukkit.event.Event");
            Class.forName("org.bukkit.scheduler.BukkitScheduler");
            Class.forName("org.bukkit.command.CommandExecutor");
            return true;
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("缺少关键API，插件无法启用！");
            return false;
        }
    }
}