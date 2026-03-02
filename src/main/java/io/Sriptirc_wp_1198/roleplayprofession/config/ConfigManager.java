package io.Sriptirc_wp_1198.roleplayprofession.config;

import io.Sriptirc_wp_1198.roleplayprofession.Roleplayprofession;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * 配置管理器 - 处理配置文件的加载、保存和版本控制
 */
public class ConfigManager {
    private final Roleplayprofession plugin;
    private FileConfiguration config;
    private File configFile;
    
    // 当前配置版本
    private static final int CURRENT_CONFIG_VERSION = 1;
    
    public ConfigManager(Roleplayprofession plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
    }
    
    /**
     * 加载配置文件
     */
    public void loadConfig() {
        // 确保插件数据文件夹存在
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        // 如果配置文件不存在，保存默认配置
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
            plugin.getLogger().info("已创建默认配置文件");
        }
        
        // 加载配置
        config = YamlConfiguration.loadConfiguration(configFile);
        
        // 检查配置版本
        checkConfigVersion();
        
        // 验证配置完整性
        validateConfig();
        
        plugin.getLogger().info("配置文件加载完成，版本: " + getConfigVersion());
    }
    
    /**
     * 检查配置版本，如果需要则更新
     */
    private void checkConfigVersion() {
        int savedVersion = config.getInt("ScriptIrc-config-version", 0);
        
        if (savedVersion < CURRENT_CONFIG_VERSION) {
            plugin.getLogger().warning("配置文件版本过旧 (" + savedVersion + ")，正在更新到版本 " + CURRENT_CONFIG_VERSION);
            updateConfig(savedVersion);
        } else if (savedVersion > CURRENT_CONFIG_VERSION) {
            plugin.getLogger().warning("配置文件版本 (" + savedVersion + ") 比插件版本 (" + CURRENT_CONFIG_VERSION + ") 更新，可能会出现问题");
        }
    }
    
    /**
     * 更新配置文件
     */
    private void updateConfig(int oldVersion) {
        // 备份旧配置文件
        File backupFile = new File(plugin.getDataFolder(), "config-backup-v" + oldVersion + ".yml");
        try {
            org.apache.commons.io.FileUtils.copyFile(configFile, backupFile);
            plugin.getLogger().info("已备份旧配置文件: " + backupFile.getName());
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "无法备份配置文件", e);
        }
        
        // 根据旧版本进行更新
        switch (oldVersion) {
            case 0:
                // 从版本0更新到版本1
                updateFromV0ToV1();
                break;
            // 未来版本更新可以在这里添加
        }
        
        // 更新版本号
        config.set("ScriptIrc-config-version", CURRENT_CONFIG_VERSION);
        
        // 保存更新后的配置
        saveConfig();
        
        plugin.getLogger().info("配置文件已更新到版本 " + CURRENT_CONFIG_VERSION);
    }
    
    /**
     * 从版本0更新到版本1
     */
    private void updateFromV0ToV1() {
        // 添加缺失的配置项
        if (!config.contains("plugin.debug")) {
            config.set("plugin.debug", false);
        }
        
        if (!config.contains("plugin.auto-save-interval")) {
            config.set("plugin.auto-save-interval", 300);
        }
        
        if (!config.contains("plugin.default-language")) {
            config.set("plugin.default-language", "zh_CN");
        }
        
        // 确保所有必要的配置项都存在
        ensureDefaultValues();
    }
    
    /**
     * 验证配置完整性
     */
    private void validateConfig() {
        // 检查必需配置项
        String[] requiredPaths = {
            "economy.currency-name",
            "economy.currency-symbol",
            "economy.start-money",
            "professions.unlock-levels.police",
            "professions.base-salary.police",
            "npc.spawn-density",
            "interactions.arrest.cooldown"
        };
        
        for (String path : requiredPaths) {
            if (!config.contains(path)) {
                plugin.getLogger().warning("缺少配置项: " + path + "，使用默认值");
                setDefaultValue(path);
            }
        }
        
        // 验证数值范围
        validateNumberRanges();
    }
    
    /**
     * 设置默认值
     */
    private void setDefaultValue(String path) {
        switch (path) {
            case "economy.currency-name":
                config.set(path, "职业币");
                break;
            case "economy.currency-symbol":
                config.set(path, "§6Ⓟ§f");
                break;
            case "economy.start-money":
                config.set(path, 1000.0);
                break;
            case "professions.unlock-levels.police":
                config.set(path, 5);
                break;
            case "professions.base-salary.police":
                config.set(path, 500);
                break;
            case "npc.spawn-density":
                config.set(path, 0.1);
                break;
            case "interactions.arrest.cooldown":
                config.set(path, 30);
                break;
            default:
                // 对于其他路径，尝试根据类型设置默认值
                if (path.endsWith(".cooldown")) {
                    config.set(path, 30);
                } else if (path.contains(".reward")) {
                    config.set(path, 100.0);
                } else if (path.contains(".probability")) {
                    config.set(path, 50);
                }
                break;
        }
    }
    
    /**
     * 验证数值范围
     */
    private void validateNumberRanges() {
        // 概率值必须在0-100之间
        validateRange("npc.behavior-probability.walk", 0, 100, 40);
        validateRange("npc.behavior-probability.talk", 0, 100, 20);
        validateRange("npc.behavior-probability.work", 0, 100, 30);
        validateRange("npc.behavior-probability.rest", 0, 100, 10);
        validateRange("npc.call-police-probability", 0, 100, 70);
        validateRange("npc.call-medic-probability", 0, 100, 80);
        
        // 冷却时间必须大于0
        validateMin("interactions.arrest.cooldown", 1, 30);
        validateMin("interactions.rescue.cooldown", 1, 20);
        validateMin("interactions.service.order-cooldown", 1, 10);
        
        // 金额必须大于等于0
        validateMin("economy.start-money", 0, 1000);
        validateMin("economy.daily-login-bonus", 0, 100);
        validateMin("professions.base-salary.police", 0, 500);
        
        // 等级必须大于0
        validateMin("professions.unlock-levels.police", 1, 5);
        validateMin("professions.max-level", 1, 50);
    }
    
    private void validateRange(String path, int min, int max, int defaultValue) {
        int value = config.getInt(path, defaultValue);
        if (value < min || value > max) {
            config.set(path, defaultValue);
            plugin.getLogger().warning("配置项 " + path + " 的值 " + value + " 超出范围 [" + min + "-" + max + "]，已重置为 " + defaultValue);
        }
    }
    
    private void validateMin(String path, int min, int defaultValue) {
        int value = config.getInt(path, defaultValue);
        if (value < min) {
            config.set(path, defaultValue);
            plugin.getLogger().warning("配置项 " + path + " 的值 " + value + " 小于最小值 " + min + "，已重置为 " + defaultValue);
        }
    }
    
    private void validateMin(String path, double min, double defaultValue) {
        double value = config.getDouble(path, defaultValue);
        if (value < min) {
            config.set(path, defaultValue);
            plugin.getLogger().warning("配置项 " + path + " 的值 " + value + " 小于最小值 " + min + "，已重置为 " + defaultValue);
        }
    }
    
    /**
     * 确保所有配置项都有默认值
     */
    private void ensureDefaultValues() {
        // 插件设置
        config.addDefault("plugin.debug", false);
        config.addDefault("plugin.auto-save-interval", 300);
        config.addDefault("plugin.default-language", "zh_CN");
        
        // 经济系统
        config.addDefault("economy.currency-name", "职业币");
        config.addDefault("economy.currency-symbol", "§6Ⓟ§f");
        config.addDefault("economy.start-money", 1000.0);
        config.addDefault("economy.daily-login-bonus", 100.0);
        config.addDefault("economy.salary-interval", 60);
        
        // 职业系统
        config.addDefault("professions.unlock-levels.police", 5);
        config.addDefault("professions.unlock-levels.firefighter", 3);
        config.addDefault("professions.unlock-levels.medic", 4);
        config.addDefault("professions.unlock-levels.customs", 6);
        config.addDefault("professions.unlock-levels.chef", 2);
        config.addDefault("professions.unlock-levels.waiter", 1);
        config.addDefault("professions.unlock-levels.farmer", 1);
        
        config.addDefault("professions.base-salary.police", 500);
        config.addDefault("professions.base-salary.firefighter", 450);
        config.addDefault("professions.base-salary.medic", 480);
        config.addDefault("professions.base-salary.customs", 520);
        config.addDefault("professions.base-salary.chef", 300);
        config.addDefault("professions.base-salary.waiter", 250);
        config.addDefault("professions.base-salary.farmer", 280);
        
        config.addDefault("professions.max-level", 50);
        config.addDefault("professions.level-up-multiplier", 1.5);
        
        // NPC系统
        config.addDefault("npc.spawn-density", 0.1);
        config.addDefault("npc.max-npcs", 100);
        config.addDefault("npc.activity-range", 16);
        config.addDefault("npc.refresh-interval", 30);
        
        config.addDefault("npc.behavior-probability.walk", 40);
        config.addDefault("npc.behavior-probability.talk", 20);
        config.addDefault("npc.behavior-probability.work", 30);
        config.addDefault("npc.behavior-probability.rest", 10);
        
        config.addDefault("npc.call-police-probability", 70);
        config.addDefault("npc.call-medic-probability", 80);
        
        // 任务系统
        config.addDefault("quests.daily-quests-per-player", 3);
        config.addDefault("quests.emergency-quest-cooldown", 300);
        config.addDefault("quests.reward-multiplier", 1.0);
        
        config.addDefault("quests.profession-quest-weights.police", 25);
        config.addDefault("quests.profession-quest-weights.firefighter", 20);
        config.addDefault("quests.profession-quest-weights.medic", 20);
        config.addDefault("quests.profession-quest-weights.customs", 15);
        config.addDefault("quests.profession-quest-weights.chef", 10);
        config.addDefault("quests.profession-quest-weights.waiter", 5);
        config.addDefault("quests.profession-quest-weights.farmer", 5);
        
        // 技能系统
        config.addDefault("skills.skill-points-per-hour", 1);
        config.addDefault("skills.max-skill-level", 10);
        config.addDefault("skills.upgrade-cost-multiplier", 2.0);
        
        // 互动系统
        config.addDefault("interactions.arrest.cooldown", 30);
        config.addDefault("interactions.arrest.max-distance", 10.0);
        config.addDefault("interactions.arrest.reward", 200.0);
        
        config.addDefault("interactions.rescue.cooldown", 20);
        config.addDefault("interactions.rescue.max-distance", 8.0);
        config.addDefault("interactions.rescue.reward", 150.0);
        
        config.addDefault("interactions.service.order-cooldown", 10);
        config.addDefault("interactions.service.delivery-reward", 50.0);
        config.addDefault("interactions.service.cooking-reward", 80.0);
        
        // GUI设置
        config.addDefault("gui.title-color", "§6§l");
        config.addDefault("gui.button-color", "§a");
        config.addDefault("gui.info-color", "§7");
        config.addDefault("gui.error-color", "§c");
        config.addDefault("gui.auto-close-time", 30);
        
        // 数据库设置
        config.addDefault("database.storage-type", "yml");
        config.addDefault("database.sqlite-file", "roleplay.db");
        config.addDefault("database.save-interval", 60);
        
        // 配置版本
        config.addDefault("ScriptIrc-config-version", CURRENT_CONFIG_VERSION);
        
        config.options().copyDefaults(true);
    }
    
    /**
     * 保存配置文件
     */
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "无法保存配置文件", e);
        }
    }
    
    /**
     * 重新加载配置文件
     */
    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
        validateConfig();
        plugin.getLogger().info("配置文件已重新加载");
    }
    
    /**
     * 获取配置
     */
    public FileConfiguration getConfig() {
        return config;
    }
    
    /**
     * 获取配置版本
     */
    public int getConfigVersion() {
        return config.getInt("ScriptIrc-config-version", 0);
    }
    
    /**
     * 检查调试模式
     */
    public boolean isDebugEnabled() {
        return config.getBoolean("plugin.debug", false);
    }
    
    /**
     * 获取自动保存间隔（秒）
     */
    public int getAutoSaveInterval() {
        return config.getInt("plugin.auto-save-interval", 300);
    }
    
    /**
     * 安全获取字符串配置，避免null
     */
    public String getString(String path, String def) {
        String value = config.getString(path);
        return value != null ? value : def;
    }
    
    /**
     * 安全获取整数配置，避免异常
     */
    public int getInt(String path, int def) {
        try {
            return config.getInt(path, def);
        } catch (Exception e) {
            plugin.getLogger().warning("获取配置 " + path + " 时出错: " + e.getMessage());
            return def;
        }
    }
    
    /**
     * 安全获取双精度配置，避免异常
     */
    public double getDouble(String path, double def) {
        try {
            return config.getDouble(path, def);
        } catch (Exception e) {
            plugin.getLogger().warning("获取配置 " + path + " 时出错: " + e.getMessage());
            return def;
        }
    }
    
    /**
     * 安全获取布尔配置，避免异常
     */
    public boolean getBoolean(String path, boolean def) {
        try {
            return config.getBoolean(path, def);
        } catch (Exception e) {
            plugin.getLogger().warning("获取配置 " + path + " 时出错: " + e.getMessage());
            return def;
        }
    }
}