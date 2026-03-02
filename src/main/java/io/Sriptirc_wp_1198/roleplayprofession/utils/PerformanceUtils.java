package io.Sriptirc_wp_1198.roleplayprofession.utils;

import io.Sriptirc_wp_1198.roleplayprofession.Roleplayprofession;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * 性能优化工具类
 */
public class PerformanceUtils {
    private final Roleplayprofession plugin;
    
    // 冷却时间管理（线程安全）
    private final Map<UUID, Long> cooldownMap;
    
    // 性能监控
    private long lastPerformanceCheck;
    private int eventCount;
    private int maxEventsPerSecond;
    
    // 异步任务管理
    private final Map<String, BukkitTask> asyncTasks;
    
    public PerformanceUtils(Roleplayprofession plugin) {
        this.plugin = plugin;
        this.cooldownMap = new ConcurrentHashMap<>();
        this.asyncTasks = new HashMap<>();
        this.lastPerformanceCheck = System.currentTimeMillis();
        this.eventCount = 0;
        this.maxEventsPerSecond = 100; // 每秒最大事件处理数
        
        // 启动性能监控
        startPerformanceMonitor();
    }
    
    /**
     * 检查冷却时间（线程安全）
     */
    public boolean checkCooldown(UUID playerId, String action, long cooldownSeconds) {
        String key = playerId.toString() + ":" + action;
        Long lastTime = cooldownMap.get(key);
        
        if (lastTime == null) {
            cooldownMap.put(key, System.currentTimeMillis());
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        long cooldownMillis = cooldownSeconds * 1000;
        
        if (currentTime - lastTime < cooldownMillis) {
            return true;
        }
        
        cooldownMap.put(key, currentTime);
        return false;
    }
    
    /**
     * 设置冷却时间（线程安全）
     */
    public void setCooldown(UUID playerId, String action) {
        String key = playerId.toString() + ":" + action;
        cooldownMap.put(key, System.currentTimeMillis());
    }
    
    /**
     * 获取剩余冷却时间（秒）
     */
    public long getRemainingCooldown(UUID playerId, String action, long cooldownSeconds) {
        String key = playerId.toString() + ":" + action;
        Long lastTime = cooldownMap.get(key);
        
        if (lastTime == null) {
            return 0;
        }
        
        long currentTime = System.currentTimeMillis();
        long cooldownMillis = cooldownSeconds * 1000;
        long remaining = cooldownMillis - (currentTime - lastTime);
        
        return Math.max(0, remaining / 1000);
    }
    
    /**
     * 清理过期的冷却记录
     */
    public void cleanupExpiredCooldowns(long expireSeconds) {
        long expireTime = System.currentTimeMillis() - (expireSeconds * 1000);
        
        cooldownMap.entrySet().removeIf(entry -> {
            if (entry.getValue() < expireTime) {
                return true;
            }
            return false;
        });
    }
    
    /**
     * 性能监控：检查事件频率
     */
    public boolean checkEventRate() {
        eventCount++;
        long currentTime = System.currentTimeMillis();
        
        // 每秒重置计数器
        if (currentTime - lastPerformanceCheck >= 1000) {
            if (eventCount > maxEventsPerSecond) {
                plugin.getLogger().warning("高事件频率警告: " + eventCount + " 事件/秒");
            }
            
            eventCount = 0;
            lastPerformanceCheck = currentTime;
        }
        
        // 如果事件频率过高，限制处理
        if (eventCount > maxEventsPerSecond * 2) {
            plugin.getLogger().severe("事件频率过高，可能影响性能: " + eventCount + " 事件/秒");
            return false;
        }
        
        return true;
    }
    
    /**
     * 启动性能监控任务
     */
    private void startPerformanceMonitor() {
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            // 每5分钟清理一次过期冷却记录
            cleanupExpiredCooldowns(300); // 5分钟
            
            // 监控内存使用
            monitorMemoryUsage();
            
        }, 20L * 60 * 5, 20L * 60 * 5); // 每5分钟执行一次
        
        asyncTasks.put("performance_monitor", task);
    }
    
    /**
     * 监控内存使用
     */
    private void monitorMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / 1024 / 1024; // MB
        long freeMemory = runtime.freeMemory() / 1024 / 1024; // MB
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory() / 1024 / 1024; // MB
        
        double usagePercentage = (double) usedMemory / maxMemory * 100;
        
        if (usagePercentage > 80) {
            plugin.getLogger().warning("高内存使用警告: " + usedMemory + "MB/" + maxMemory + "MB (" + 
                                      String.format("%.1f", usagePercentage) + "%)");
            
            // 建议清理
            if (usagePercentage > 90) {
                plugin.getLogger().warning("内存使用过高，建议重启服务器或优化插件");
            }
        }
    }
    
    /**
     * 异步执行任务（避免阻塞主线程）
     */
    public void runAsync(Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                task.run();
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "异步任务执行失败", e);
            }
        });
    }
    
    /**
     * 延迟执行任务
     */
    public void runLater(Runnable task, long delayTicks) {
        Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }
    
    /**
     * 注册定时任务
     */
    public BukkitTask runTimer(Runnable task, long delayTicks, long periodTicks, String taskName) {
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
        
        if (taskName != null) {
            asyncTasks.put(taskName, bukkitTask);
        }
        
        return bukkitTask;
    }
    
    /**
     * 取消定时任务
     */
    public void cancelTask(String taskName) {
        BukkitTask task = asyncTasks.get(taskName);
        if (task != null) {
            task.cancel();
            asyncTasks.remove(taskName);
        }
    }
    
    /**
     * 取消所有定时任务
     */
    public void cancelAllTasks() {
        for (BukkitTask task : asyncTasks.values()) {
            if (task != null) {
                task.cancel();
            }
        }
        asyncTasks.clear();
    }
    
    /**
     * 安全执行可能抛出异常的操作
     */
    public static void safeExecute(Runnable operation, String operationName) {
        try {
            operation.run();
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "执行操作失败: " + operationName, e);
        }
    }
    
    /**
     * 验证位置是否有效（防止空指针）
     */
    public static boolean isValidLocation(org.bukkit.Location location) {
        return location != null && location.getWorld() != null;
    }
    
    /**
     * 验证玩家是否有效
     */
    public static boolean isValidPlayer(org.bukkit.entity.Player player) {
        return player != null && player.isOnline() && !player.isDead();
    }
    
    /**
     * 限制数值范围
     */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
    
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * 格式化文件大小
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    /**
     * 检查服务器是否支持Folia
     */
    public static boolean isFoliaSupported() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    /**
     * 获取服务器TPS
     */
    public static double getServerTPS() {
        try {
            // 尝试获取Paper/Spigot的TPS
            double[] tps = Bukkit.getTPS();
            return tps[0]; // 最近1分钟的TPS
        } catch (Exception e) {
            // 如果无法获取，返回默认值
            return 20.0;
        }
    }
    
    /**
     * 检查服务器性能状态
     */
    public static PerformanceStatus getPerformanceStatus() {
        double tps = getServerTPS();
        
        if (tps >= 18.0) {
            return PerformanceStatus.GOOD;
        } else if (tps >= 15.0) {
            return PerformanceStatus.WARNING;
        } else {
            return PerformanceStatus.CRITICAL;
        }
    }
    
    public enum PerformanceStatus {
        GOOD, WARNING, CRITICAL
    }
}