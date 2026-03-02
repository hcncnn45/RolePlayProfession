package io.Sriptirc_wp_1198.roleplayprofession.profession;

import org.bukkit.ChatColor;
import org.bukkit.Material;

/**
 * 职业类型枚举
 */
public enum ProfessionType {
    POLICE("警察", "维护治安，抓捕罪犯", 
           Material.IRON_SWORD, ChatColor.BLUE, 
           "roleplay.police", 5, 500),
    
    FIREFIGHTER("消防员", "灭火救援，保护安全", 
                Material.WATER_BUCKET, ChatColor.RED, 
                "roleplay.fire", 3, 450),
    
    MEDIC("医护人员", "医疗急救，拯救生命", 
          Material.POTION, ChatColor.GREEN, 
          "roleplay.medic", 4, 480),
    
    CUSTOMS("海关人员", "检查物品，维护边境", 
            Material.PAPER, ChatColor.YELLOW, 
            "roleplay.customs", 6, 520),
    
    CHEF("厨师", "烹饪美食，服务顾客", 
         Material.COOKED_BEEF, ChatColor.GOLD, 
         "roleplay.chef", 2, 300),
    
    WAITER("服务员", "点餐送餐，服务周到", 
           Material.CAKE, ChatColor.LIGHT_PURPLE, 
           "roleplay.waiter", 1, 250),
    
    FARMER("农民", "种植收获，提供食材", 
           Material.WHEAT, ChatColor.DARK_GREEN, 
           "roleplay.farmer", 1, 280),
    
    NONE("无职业", "尚未选择职业", 
         Material.BARRIER, ChatColor.GRAY, 
         "roleplay.use", 0, 0);
    
    private final String displayName;
    private final String description;
    private final Material icon;
    private final ChatColor color;
    private final String permission;
    private final int unlockLevel;
    private final int baseSalary;
    
    ProfessionType(String displayName, String description, Material icon, 
                   ChatColor color, String permission, int unlockLevel, int baseSalary) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.color = color;
        this.permission = permission;
        this.unlockLevel = unlockLevel;
        this.baseSalary = baseSalary;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Material getIcon() {
        return icon;
    }
    
    public ChatColor getColor() {
        return color;
    }
    
    public String getPermission() {
        return permission;
    }
    
    public int getUnlockLevel() {
        return unlockLevel;
    }
    
    public int getBaseSalary() {
        return baseSalary;
    }
    
    public String getColoredName() {
        return color + displayName;
    }
    
    public static ProfessionType fromString(String name) {
        try {
            return ProfessionType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            for (ProfessionType type : values()) {
                if (type.getDisplayName().equalsIgnoreCase(name)) {
                    return type;
                }
            }
            return NONE;
        }
    }
    
    /**
     * 获取职业对应的技能类型
     */
    public SkillType getPrimarySkill() {
        switch (this) {
            case POLICE:
                return SkillType.ARREST;
            case FIREFIGHTER:
                return SkillType.FIREFIGHTING;
            case MEDIC:
                return SkillType.MEDICAL;
            case CUSTOMS:
                return SkillType.INSPECTION;
            case CHEF:
                return SkillType.COOKING;
            case WAITER:
                return SkillType.SERVICE;
            case FARMER:
                return SkillType.FARMING;
            default:
                return SkillType.GENERAL;
        }
    }
    
    /**
     * 获取职业对应的工具
     */
    public Material getTool() {
        switch (this) {
            case POLICE:
                return Material.IRON_SWORD;
            case FIREFIGHTER:
                return Material.WATER_BUCKET;
            case MEDIC:
                return Material.POTION;
            case CUSTOMS:
                return Material.PAPER;
            case CHEF:
                return Material.COOKED_BEEF;
            case WAITER:
                return Material.CAKE;
            case FARMER:
                return Material.IRON_HOE;
            default:
                return Material.STICK;
        }
    }
}