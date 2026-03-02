package io.Sriptirc_wp_1198.roleplayprofession.utils;

import io.Sriptirc_wp_1198.roleplayprofession.Roleplayprofession;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

/**
 * 错误处理工具类
 * 集中处理插件中的异常和错误
 */
public class ErrorHandler {
    private final Roleplayprofession plugin;
    private final File errorLogFile;
    private final SimpleDateFormat dateFormat;
    
    public ErrorHandler(Roleplayprofession plugin) {
        this.plugin = plugin;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        // 创建错误日志目录
        File errorLogDir = new File(plugin.getDataFolder(), "error_logs");
        if (!errorLogDir.exists()) {
            errorLogDir.mkdirs();
        }
        
        // 创建错误日志文件
        String timestamp = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        this.errorLogFile = new File(errorLogDir, "errors-" + timestamp + ".log");
    }
    
    /**
     * 记录错误到日志文件和控制台
     */
    public void logError(String context, Throwable throwable) {
        String timestamp = dateFormat.format(new Date());
        String errorMessage = "[" + timestamp + "] [" + context + "] " + throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
        
        // 输出到控制台
        plugin.getLogger().log(Level.SEVERE, errorMessage, throwable);
        
        // 记录到文件
        logToFile(errorMessage, throwable);
        
        // 如果启用了调试模式，输出堆栈跟踪
        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getLogger().log(Level.SEVERE, "详细堆栈跟踪:", throwable);
        }
    }
    
    /**
     * 记录警告
     */
    public void logWarning(String context, String message) {
        String timestamp = dateFormat.format(new Date());
        String warningMessage = "[" + timestamp + "] [" + context + "] WARNING: " + message;
        
        plugin.getLogger().warning(warningMessage);
        logToFile(warningMessage, null);
    }
    
    /**
     * 记录信息
     */
    public void logInfo(String context, String message) {
        String timestamp = dateFormat.format(new Date());
        String infoMessage = "[" + timestamp + "] [" + context + "] INFO: " + message;
        
        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getLogger().info(infoMessage);
        }
        logToFile(infoMessage, null);
    }
    
    /**
     * 安全执行操作，捕获并记录异常
     */
    public void safeExecute(Runnable operation, String context) {
        try {
            operation.run();
        } catch (Exception e) {
            logError(context, e);
        }
    }
    
    /**
     * 安全执行并返回默认值
     */
    public <T> T safeGet(SupplierWithException<T> supplier, String context, T defaultValue) {
        try {
            return supplier.get();
        } catch (Exception e) {
            logError(context, e);
            return defaultValue;
        }
    }
    
    /**
     * 向玩家发送错误消息（如果玩家在线）
     */
    public void sendErrorMessage(Player player, String message) {
        if (player != null && player.isOnline()) {
            try {
                player.sendMessage("§c[错误] " + message);
            } catch (Exception e) {
                logError("向玩家发送错误消息", e);
            }
        }
    }
    
    /**
     * 向玩家发送警告消息
     */
    public void sendWarningMessage(Player player, String message) {
        if (player != null && player.isOnline()) {
            try {
                player.sendMessage("§e[警告] " + message);
            } catch (Exception e) {
                logError("向玩家发送警告消息", e);
            }
        }
    }
    
    /**
     * 向玩家发送成功消息
     */
    public void sendSuccessMessage(Player player, String message) {
        if (player != null && player.isOnline()) {
            try {
                player.sendMessage("§a[成功] " + message);
            } catch (Exception e) {
                logError("向玩家发送成功消息", e);
            }
        }
    }
    
    /**
     * 验证玩家数据是否有效
     */
    public boolean validatePlayerData(Player player) {
        if (player == null) {
            logWarning("验证玩家数据", "玩家为null");
            return false;
        }
        
        if (!player.isOnline()) {
            logWarning("验证玩家数据", "玩家 " + player.getName() + " 不在线");
            return false;
        }
        
        if (player.isDead()) {
            logWarning("验证玩家数据", "玩家 " + player.getName() + " 已死亡");
            return false;
        }
        
        return true;
    }
    
    /**
     * 验证位置是否有效
     */
    public boolean validateLocation(org.bukkit.Location location) {
        if (location == null) {
            logWarning("验证位置", "位置为null");
            return false;
        }
        
        if (location.getWorld() == null) {
            logWarning("验证位置", "位置的世界为null");
            return false;
        }
        
        return true;
    }
    
    /**
     * 验证物品是否有效
     */
    public boolean validateItem(org.bukkit.inventory.ItemStack item) {
        if (item == null) {
            logWarning("验证物品", "物品为null");
            return false;
        }
        
        if (item.getType() == org.bukkit.Material.AIR) {
            logWarning("验证物品", "物品类型为AIR");
            return false;
        }
        
        return true;
    }
    
    /**
     * 检查插件是否已正确初始化
     */
    public boolean isPluginInitialized() {
        if (plugin.getDataManager() == null) {
            logError("检查插件初始化", new IllegalStateException("数据管理器未初始化"));
            return false;
        }
        
        if (plugin.getConfigManager() == null) {
            logError("检查插件初始化", new IllegalStateException("配置管理器未初始化"));
            return false;
        }
        
        if (plugin.getEconomyManager() == null) {
            logError("检查插件初始化", new IllegalStateException("经济管理器未初始化"));
            return false;
        }
        
        return true;
    }
    
    /**
     * 生成错误报告
     */
    public String generateErrorReport(Throwable throwable, String context) {
        StringBuilder report = new StringBuilder();
        report.append("=== 错误报告 ===\n");
        report.append("时间: ").append(dateFormat.format(new Date())).append("\n");
        report.append("上下文: ").append(context).append("\n");
        report.append("插件版本: ").append(plugin.getDescription().getVersion()).append("\n");
        report.append("服务器: ").append(Bukkit.getName()).append("\n");
        report.append("服务器版本: ").append(Bukkit.getVersion()).append("\n");
        report.append("Bukkit版本: ").append(Bukkit.getBukkitVersion()).append("\n");
        report.append("\n");
        
        report.append("错误类型: ").append(throwable.getClass().getName()).append("\n");
        report.append("错误消息: ").append(throwable.getMessage()).append("\n");
        report.append("\n");
        
        report.append("堆栈跟踪:\n");
        for (StackTraceElement element : throwable.getStackTrace()) {
            report.append("  at ").append(element.toString()).append("\n");
        }
        
        // 添加原因
        Throwable cause = throwable.getCause();
        if (cause != null) {
            report.append("\n原因:\n");
            report.append(cause.getClass().getName()).append(": ").append(cause.getMessage()).append("\n");
        }
        
        return report.toString();
    }
    
    /**
     * 保存错误报告到文件
     */
    public void saveErrorReport(Throwable throwable, String context) {
        String report = generateErrorReport(throwable, context);
        
        File reportDir = new File(plugin.getDataFolder(), "error_reports");
        if (!reportDir.exists()) {
            reportDir.mkdirs();
        }
        
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        File reportFile = new File(reportDir, "error-report-" + timestamp + ".txt");
        
        try (FileWriter writer = new FileWriter(reportFile)) {
            writer.write(report);
            plugin.getLogger().info("错误报告已保存到: " + reportFile.getAbsolutePath());
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "无法保存错误报告", e);
        }
    }
    
    /**
     * 记录到文件
     */
    private void logToFile(String message, Throwable throwable) {
        try (FileWriter fw = new FileWriter(errorLogFile, true);
             PrintWriter pw = new PrintWriter(fw)) {
            
            pw.println(message);
            
            if (throwable != null) {
                throwable.printStackTrace(pw);
            }
            
            pw.println(); // 空行分隔
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "无法写入错误日志文件", e);
        }
    }
    
    /**
     * 清理旧的错误日志文件（保留最近7天）
     */
    public void cleanupOldErrorLogs(int daysToKeep) {
        File errorLogDir = new File(plugin.getDataFolder(), "error_logs");
        if (!errorLogDir.exists() || !errorLogDir.isDirectory()) {
            return;
        }
        
        long cutoffTime = System.currentTimeMillis() - (daysToKeep * 24L * 60 * 60 * 1000);
        File[] logFiles = errorLogDir.listFiles();
        
        if (logFiles != null) {
            for (File file : logFiles) {
                if (file.isFile() && file.getName().startsWith("errors-") && file.getName().endsWith(".log")) {
                    if (file.lastModified() < cutoffTime) {
                        if (file.delete()) {
                            logInfo("清理错误日志", "已删除旧日志文件: " + file.getName());
                        } else {
                            logWarning("清理错误日志", "无法删除日志文件: " + file.getName());
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 函数式接口，用于安全执行
     */
    @FunctionalInterface
    public interface SupplierWithException<T> {
        T get() throws Exception;
    }
    
    /**
     * 静态工具方法：安全执行
     */
    public static void safeExecuteStatic(Runnable operation, String context) {
        try {
            operation.run();
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "[" + context + "] " + e.getMessage(), e);
        }
    }
    
    /**
     * 静态工具方法：安全获取
     */
    public static <T> T safeGetStatic(SupplierWithException<T> supplier, String context, T defaultValue) {
        try {
            return supplier.get();
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "[" + context + "] " + e.getMessage(), e);
            return defaultValue;
        }
    }
}