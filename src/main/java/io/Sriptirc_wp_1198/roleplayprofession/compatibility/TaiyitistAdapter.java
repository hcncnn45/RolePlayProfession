package io.Sriptirc_wp_1198.roleplayprofession.compatibility;

import io.Sriptirc_wp_1198.roleplayprofession.Roleplayprofession;
import io.Sriptirc_wp_1198.roleplayprofession.utils.CompatibilityChecker;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

/**
 * Taiyitist服务端专用适配器
 * Taiyitist是新一代Fabric Mod服务端（原Banner），兼容Bukkit/Spigot/Paper插件
 * 这个适配器处理Taiyitist特有的兼容性问题
 */
public class TaiyitistAdapter {
    private final Roleplayprofession plugin;
    private boolean isTaiyitist = false;
    private String serverVersion = "Unknown";
    
    // Taiyitist可能缺失或不完全支持的API
    private final Set<String> missingAPIs;
    private final Set<String> workaroundAPIs;
    
    public TaiyitistAdapter(Roleplayprofession plugin) {
        this.plugin = plugin;
        this.missingAPIs = new HashSet<>();
        this.workaroundAPIs = new HashSet<>();
        
        detectTaiyitist();
        checkTaiyitistCompatibility();
    }
    
    /**
     * 检测是否为Taiyitist服务端
     */
    private void detectTaiyitist() {
        String serverName = Bukkit.getName();
        String serverVersion = Bukkit.getVersion();
        
        this.serverVersion = serverVersion;
        
        // 检测Taiyitist特征
        if (serverName.contains("Taiyitist") || 
            serverName.contains("Banner") || 
            serverVersion.contains("Taiyitist") ||
            serverVersion.contains("Banner") ||
            serverName.contains("Fabric")) {
            
            isTaiyitist = true;
            plugin.getLogger().info("检测到Taiyitist(Fabric)服务端: " + serverName + " - " + serverVersion);
        }
        
        // 检测其他Fabric特征
        try {
            // 检查Fabric特有的类
            Class.forName("net.fabricmc.loader.api.FabricLoader");
            isTaiyitist = true;
            plugin.getLogger().info("检测到Fabric环境");
        } catch (ClassNotFoundException e) {
            // 不是Fabric环境
        }
    }
    
    /**
     * 检查Taiyitist兼容性
     */
    private void checkTaiyitistCompatibility() {
        if (!isTaiyitist) {
            return;
        }
        
        plugin.getLogger().info("开始Taiyitist兼容性检查...");
        
        // 检查Bukkit API完整性
        checkBukkitAPIs();
        
        // 检查事件系统
        checkEventSystem();
        
        // 检查调度器
        checkScheduler();
        
        // 检查NMS/反射相关功能
        checkNMSFeatures();
        
        // 生成兼容性报告
        generateCompatibilityReport();
    }
    
    /**
     * 检查Bukkit API完整性
     */
    private void checkBukkitAPIs() {
        // Fabric服务端可能缺失或不完全支持某些Bukkit API
        String[] apisToCheck = {
            "org.bukkit.advancement.Advancement",
            "org.bukkit.scoreboard.DisplaySlot",
            "org.bukkit.boss.BarColor",
            "org.bukkit.boss.BarStyle",
            "org.bukkit.map.MapView",
            "org.bukkit.persistence.PersistentDataType"
        };
        
        for (String api : apisToCheck) {
            try {
                Class.forName(api);
            } catch (ClassNotFoundException e) {
                missingAPIs.add(api);
                plugin.getLogger().warning("Taiyitist可能不支持API: " + api);
            }
        }
    }
    
    /**
     * 检查事件系统
     */
    private void checkEventSystem() {
        // Fabric服务端的事件系统可能与标准Bukkit有差异
        try {
            Class.forName("org.bukkit.event.Event");
            Class.forName("org.bukkit.event.Listener");
            Class.forName("org.bukkit.plugin.EventExecutor");
        } catch (ClassNotFoundException e) {
            missingAPIs.add("事件系统");
            plugin.getLogger().severe("Taiyitist事件系统不完整！");
        }
    }
    
    /**
     * 检查调度器
     */
    private void checkScheduler() {
        try {
            Class.forName("org.bukkit.scheduler.BukkitScheduler");
            Class.forName("org.bukkit.scheduler.BukkitTask");
            
            // 测试调度器功能
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getLogger().info("Taiyitist调度器测试通过");
            });
            
        } catch (Exception e) {
            missingAPIs.add("调度器");
            plugin.getLogger().warning("Taiyitist调度器可能有问题: " + e.getMessage());
        }
    }
    
    /**
     * 检查NMS/反射相关功能
     */
    private void checkNMSFeatures() {
        // Fabric服务端通常不支持NMS（net.minecraft.server）相关功能
        try {
            // 尝试访问NMS类（通常会在Fabric服务端失败）
            Class.forName("net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + ".EntityPlayer");
            plugin.getLogger().warning("检测到NMS访问，在Taiyitist上可能不稳定");
            workaroundAPIs.add("NMS访问");
        } catch (ClassNotFoundException e) {
            // 正常情况，Fabric服务端通常没有NMS
        }
    }
    
    /**
     * 生成兼容性报告
     */
    private void generateCompatibilityReport() {
        if (!isTaiyitist || missingAPIs.isEmpty()) {
            return;
        }
        
        StringBuilder report = new StringBuilder();
        report.append("=== Taiyitist兼容性报告 ===\n");
        report.append("服务器: ").append(Bukkit.getName()).append("\n");
        report.append("版本: ").append(serverVersion).append("\n");
        report.append("检测到Fabric环境: ").append(isTaiyitist).append("\n");
        report.append("\n");
        
        if (!missingAPIs.isEmpty()) {
            report.append("缺失或不完全支持的API (").append(missingAPIs.size()).append("):\n");
            for (String api : missingAPIs) {
                report.append("  - ").append(api).append("\n");
            }
            report.append("\n");
        }
        
        if (!workaroundAPIs.isEmpty()) {
            report.append("已启用兼容性解决方案 (").append(workaroundAPIs.size()).append("):\n");
            for (String api : workaroundAPIs) {
                report.append("  - ").append(api).append("\n");
            }
            report.append("\n");
        }
        
        report.append("建议:\n");
        report.append("1. 避免使用NMS/反射相关功能\n");
        report.append("2. 使用标准的Bukkit API\n");
        report.append("3. 测试所有事件监听器\n");
        report.append("4. 验证调度器功能\n");
        report.append("5. 定期检查插件更新\n");
        
        plugin.getLogger().info(report.toString());
    }
    
    /**
     * 获取安全的Material（Taiyitist可能缺少某些Material）
     */
    public Material getSafeMaterial(String materialName, Material defaultMaterial) {
        try {
            Material material = Material.valueOf(materialName);
            
            // 在Taiyitist上特别检查一些可能缺失的Material
            if (isTaiyitist) {
                // Fabric可能缺少某些较新的Material
                switch (materialName) {
                    case "PLAYER_HEAD":
                        // 玩家头颅在Fabric上可能有问题
                        try {
                            // 尝试创建玩家头颅物品
                            org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(material);
                            if (item.getType() == Material.AIR) {
                                return Material.SKELETON_SKULL; // 备用
                            }
                        } catch (Exception e) {
                            return Material.SKELETON_SKULL; // 备用
                        }
                        break;
                        
                    case "BARRIER":
                        // 屏障方块
                        try {
                            Material.valueOf("BARRIER");
                        } catch (IllegalArgumentException e) {
                            return Material.BEDROCK; // 备用
                        }
                        break;
                        
                    case "KNOWLEDGE_BOOK":
                        // 知识之书
                        try {
                            Material.valueOf("KNOWLEDGE_BOOK");
                        } catch (IllegalArgumentException e) {
                            return Material.BOOK; // 备用
                        }
                        break;
                }
            }
            
            return material;
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Taiyitist不支持Material: " + materialName + "，使用备用: " + defaultMaterial);
            return defaultMaterial;
        }
    }
    
    /**
     * 安全注册事件监听器（Taiyitist可能需要特殊处理）
     */
    public void safeRegisterEvents(org.bukkit.event.Listener listener) {
        try {
            Bukkit.getPluginManager().registerEvents(listener, plugin);
            plugin.getLogger().info("成功注册事件监听器: " + listener.getClass().getSimpleName());
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Taiyitist注册事件监听器失败: " + listener.getClass().getSimpleName(), e);
            
            // 尝试使用反射注册（备用方案）
            try {
                java.lang.reflect.Method method = Bukkit.getPluginManager().getClass()
                    .getMethod("registerEvents", org.bukkit.event.Listener.class, org.bukkit.plugin.Plugin.class);
                method.invoke(Bukkit.getPluginManager(), listener, plugin);
                plugin.getLogger().info("通过反射成功注册事件监听器");
            } catch (Exception ex) {
                plugin.getLogger().severe("无法注册事件监听器，相关功能可能无法使用");
            }
        }
    }
    
    /**
     * 安全执行调度任务（Taiyitist调度器可能有限制）
     */
    public org.bukkit.scheduler.BukkitTask safeRunTask(Runnable task, String taskName) {
        try {
            return Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    task.run();
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Taiyitist任务执行失败: " + taskName, e);
                }
            });
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Taiyitist创建任务失败: " + taskName, e);
            return null;
        }
    }
    
    public org.bukkit.scheduler.BukkitTask safeRunTaskAsync(Runnable task, String taskName) {
        try {
            return Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    task.run();
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Taiyitist异步任务执行失败: " + taskName, e);
                }
            });
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Taiyitist创建异步任务失败: " + taskName, e);
            return null;
        }
    }
    
    public org.bukkit.scheduler.BukkitTask safeRunTaskLater(Runnable task, long delay, String taskName) {
        try {
            return Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try {
                    task.run();
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Taiyitist延迟任务执行失败: " + taskName, e);
                }
            }, delay);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Taiyitist创建延迟任务失败: " + taskName, e);
            return null;
        }
    }
    
    public org.bukkit.scheduler.BukkitTask safeRunTaskTimer(Runnable task, long delay, long period, String taskName) {
        try {
            return Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                try {
                    task.run();
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Taiyitist定时任务执行失败: " + taskName, e);
                }
            }, delay, period);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Taiyitist创建定时任务失败: " + taskName, e);
            return null;
        }
    }
    
    /**
     * 检查DamageCause兼容性（Fabric可能缺少某些伤害原因）
     */
    public boolean isDamageCauseSupported(EntityDamageEvent.DamageCause cause) {
        if (!isTaiyitist) {
            return true;
        }
        
        try {
            // 尝试获取伤害原因
            EntityDamageEvent.DamageCause.valueOf(cause.name());
            return true;
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Taiyitist不支持DamageCause: " + cause.name());
            return false;
        }
    }
    
    /**
     * 获取替代的DamageCause
     */
    public EntityDamageEvent.DamageCause getAlternativeDamageCause(EntityDamageEvent.DamageCause original) {
        if (!isTaiyitist) {
            return original;
        }
        
        // Fabric可能缺少某些伤害原因，提供替代方案
        switch (original) {
            case MELTING: // 融化（可能不存在）
                return EntityDamageEvent.DamageCause.FIRE;
            case CUSTOM: // 自定义伤害
                return EntityDamageEvent.DamageCause.ENTITY_ATTACK;
            case CRAMMING: // 拥挤伤害
                return EntityDamageEvent.DamageCause.ENTITY_ATTACK;
            default:
                return original;
        }
    }
    
    /**
     * 检查是否应该启用插件
     */
    public boolean shouldEnablePlugin() {
        if (!isTaiyitist) {
            return true;
        }
        
        // 检查关键API
        String[] criticalAPIs = {
            "org.bukkit.plugin.java.JavaPlugin",
            "org.bukkit.event.Listener",
            "org.bukkit.scheduler.BukkitScheduler",
            "org.bukkit.command.CommandExecutor"
        };
        
        for (String api : criticalAPIs) {
            try {
                Class.forName(api);
            } catch (ClassNotFoundException e) {
                plugin.getLogger().severe("Taiyitist缺少关键API: " + api + "，插件无法启用！");
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 获取Taiyitist兼容性建议
     */
    public String getCompatibilityAdvice() {
        if (!isTaiyitist) {
            return "非Taiyitist服务端，无需特殊兼容性处理";
        }
        
        StringBuilder advice = new StringBuilder();
        advice.append("=== Taiyitist兼容性建议 ===\n");
        advice.append("\n");
        advice.append("Taiyitist是Fabric Mod服务端，兼容Bukkit插件时请注意：\n");
        advice.append("\n");
        advice.append("1. API限制:\n");
        advice.append("   - 避免使用NMS/反射功能\n");
        advice.append("   - 使用标准Bukkit API\n");
        advice.append("   - 测试所有Material和DamageCause\n");
        advice.append("\n");
        advice.append("2. 事件系统:\n");
        advice.append("   - 事件注册可能需要特殊处理\n");
        advice.append("   - 测试所有事件监听器\n");
        advice.append("   - 注意事件执行顺序\n");
        advice.append("\n");
        advice.append("3. 调度器:\n");
        advice.append("   - 异步任务可能有限制\n");
        advice.append("   - 定时任务需要测试\n");
        advice.append("   - 避免高频任务\n");
        advice.append("\n");
        advice.append("4. 性能优化:\n");
        advice.append("   - Fabric性能特征不同\n");
        advice.append("   - 监控内存使用\n");
        advice.append("   - 优化事件处理\n");
        advice.append("\n");
        advice.append("5. 调试建议:\n");
        advice.append("   - 启用调试模式\n");
        advice.append("   - 查看详细日志\n");
        advice.append("   - 测试所有功能\n");
        
        return advice.toString();
    }
    
    /**
     * 是否为Taiyitist服务端
     */
    public boolean isTaiyitist() {
        return isTaiyitist;
    }
    
    /**
     * 获取服务器版本
     */
    public String getServerVersion() {
        return serverVersion;
    }
    
    /**
     * 获取缺失的API列表
     */
    public Set<String> getMissingAPIs() {
        return new HashSet<>(missingAPIs);
    }
    
    /**
     * 获取已启用解决方案的API列表
     */
    public Set<String> getWorkaroundAPIs() {
        return new HashSet<>(workaroundAPIs);
    }
}