package io.Sriptirc_wp_1198.roleplayprofession.profession;

import org.bukkit.ChatColor;

/**
 * 技能类型枚举
 */
public enum SkillType {
    // 通用技能
    GENERAL("通用", "基础能力", ChatColor.GRAY),
    
    // 职业专属技能
    ARREST("抓捕", "提高抓捕成功率", ChatColor.BLUE),
    FIREFIGHTING("消防", "提高灭火效率", ChatColor.RED),
    MEDICAL("医疗", "提高治疗效果", ChatColor.GREEN),
    INSPECTION("检查", "提高检查准确率", ChatColor.YELLOW),
    COOKING("烹饪", "提高烹饪品质", ChatColor.GOLD),
    SERVICE("服务", "提高服务满意度", ChatColor.LIGHT_PURPLE),
    FARMING("农业", "提高种植产量", ChatColor.DARK_GREEN),
    
    // 特殊技能
    LEADERSHIP("领导力", "提高团队效率", ChatColor.AQUA),
    COMMUNICATION("沟通", "提高交流效果", ChatColor.WHITE),
    PHYSICAL("体能", "提高体力恢复", ChatColor.DARK_RED);
    
    private final String displayName;
    private final String description;
    private final ChatColor color;
    
    SkillType(String displayName, String description, ChatColor color) {
        this.displayName = displayName;
        this.description = description;
        this.color = color;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public ChatColor getColor() {
        return color;
    }
    
    public String getColoredName() {
        return color + displayName;
    }
    
    /**
     * 获取技能升级所需经验
     */
    public int getRequiredExp(int level) {
        if (level <= 0) return 100;
        return (int) (100 * Math.pow(1.5, level - 1));
    }
    
    /**
     * 获取技能效果加成
     */
    public double getEffectMultiplier(int level) {
        return 1.0 + (level * 0.05);
    }
    
    /**
     * 获取技能最大等级
     */
    public int getMaxLevel() {
        return 10;
    }
}