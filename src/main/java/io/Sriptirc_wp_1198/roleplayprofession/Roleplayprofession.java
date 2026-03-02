package io.Sriptirc_wp_1198.roleplayprofession;

import io.Sriptirc_wp_1198.roleplayprofession.command.CommandHandler;
import io.Sriptirc_wp_1198.roleplayprofession.data.DataManager;
import io.Sriptirc_wp_1198.roleplayprofession.economy.EconomyManager;
import io.Sriptirc_wp_1198.roleplayprofession.gui.GUIManager;
import io.Sriptirc_wp_1198.roleplayprofession.listener.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class Roleplayprofession extends JavaPlugin {
    
    private static Roleplayprofession instance;
    private ConfigManager configManager;
    private DataManager dataManager;
    private EconomyManager economyManager;
    private GUIManager guiManager;
    private CommandHandler commandHandler;
    private PlayerListener playerListener;
    private CompatibilityChecker compatibilityChecker;
    private PerformanceUtils performanceUtils;
    private TaiyitistAdapter taiyitistAdapter;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Taiyitist兼容性适配
        taiyitistAdapter = new TaiyitistAdapter(this);
        if (!taiyitistAdapter.shouldEnablePlugin()) {
            getLogger().severe("插件无法在Taiyitist服务端上启用！");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        
        // 标准兼容性检查
        compatibilityChecker = new CompatibilityChecker(this);
        if (!compatibilityChecker.shouldEnablePlugin()) {
            getLogger().severe("插件无法启用，缺少关键API！");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        
        boolean compatibilityPassed = compatibilityChecker.checkCompatibility();
        if (!compatibilityPassed) {
            getLogger().warning("兼容性检查发现问题，插件将继续运行但可能不稳定");
            getLogger().info(compatibilityChecker.generateCompatibilityReport());
        }
        
        // 初始化性能工具
        performanceUtils = new PerformanceUtils(this);
        
        // 初始化配置管理器
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        
        // 初始化管理器
        initializeManagers();
        
        // 注册监听器
        registerListeners();
        
        // 注册命令
        registerCommands();
        
        // 启动定时任务
        startTasks();
        
        getLogger().info("角色扮演职业系统已启用！");
        getLogger().info("插件版本: " + getDescription().getVersion());
        getLogger().info("作者: " + getDescription().getAuthors());
        getLogger().info("配置版本: " + configManager.getConfigVersion());
        
        // 输出Taiyitist兼容性建议
        if (taiyitistAdapter.isTaiyitist()) {
            getLogger().info(taiyitistAdapter.getCompatibilityAdvice());
        }
        
        // 输出兼容性报告到日志
        if (configManager.isDebugEnabled()) {
            getLogger().info(compatibilityChecker.generateCompatibilityReport());
        }
    }
    
    @Override
    public void onDisable() {
        // 保存所有数据
        if (dataManager != null) {
            dataManager.saveAllPlayerData();
            getLogger().info("玩家数据已保存");
        }
        
        // 清理性能工具
        if (performanceUtils != null) {
            performanceUtils.cancelAllTasks();
        }
        
        getLogger().info("角色扮演职业系统已禁用");
    }
    
    private void initializeManagers() {
        try {
            // 初始化数据管理器
            dataManager = new DataManager(this);
            
            // 初始化经济管理器
            economyManager = new EconomyManager(this, dataManager);
            
            // 初始化GUI管理器
            guiManager = new GUIManager(this, dataManager, economyManager);
            
            // 初始化命令处理器
            commandHandler = new CommandHandler(this, dataManager, economyManager, guiManager);
            
            getLogger().info("所有管理器初始化完成");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "初始化管理器时发生错误", e);
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }
    
    private void registerListeners() {
        try {
            playerListener = new PlayerListener(this, dataManager, economyManager);
            getLogger().info("监听器注册完成");
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "注册监听器时发生错误", e);
        }
    }
    
    private void registerCommands() {
        try {
            // 注册主命令
            getCommand("roleplay").setExecutor(commandHandler);
            getCommand("roleplay").setTabCompleter(commandHandler);
            
            getCommand("rpjob").setExecutor(commandHandler);
            getCommand("rpjob").setTabCompleter(commandHandler);
            
            getCommand("rpeconomy").setExecutor(commandHandler);
            getCommand("rpeconomy").setTabCompleter(commandHandler);
            
            getCommand("rpnpc").setExecutor(commandHandler);
            getCommand("rpnpc").setTabCompleter(commandHandler);
            
            getLogger().info("命令注册完成");
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "注册命令时发生错误", e);
        }
    }
    
    private void startTasks() {
        try {
            // 启动自动保存任务
            dataManager.startAutoSave();
            
            // 启动工资发放任务
            economyManager.startSalaryTask();
            
            getLogger().info("定时任务已启动");
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "启动定时任务时发生错误", e);
        }
    }
    
    public static Roleplayprofession getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public DataManager getDataManager() {
        return dataManager;
    }
    
    public EconomyManager getEconomyManager() {
        return economyManager;
    }
    
    public GUIManager getGuiManager() {
        return guiManager;
    }
    
    public CommandHandler getCommandHandler() {
        return commandHandler;
    }
    
    public PlayerListener getPlayerListener() {
        return playerListener;
    }
    
    /**
     * 重载配置（供命令使用）
     */
    public void reloadPluginConfig() {
        if (configManager != null) {
            configManager.reloadConfig();
        }
    }
}
