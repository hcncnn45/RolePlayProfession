package io.Sriptirc_wp_1198.roleplayprofession.data;

import io.Sriptirc_wp_1198.roleplayprofession.profession.ProfessionType;
import io.Sriptirc_wp_1198.roleplayprofession.profession.SkillType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 玩家数据类
 */
public class PlayerData {
    private final UUID playerId;
    private String playerName;
    
    // 职业相关
    private ProfessionType profession;
    private int professionLevel;
    private int professionExp;
    private long lastSalaryTime;
    
    // 经济相关
    private double money;
    private int skillPoints;
    
    // 技能数据
    private Map<SkillType, Integer> skills;
    
    // 任务相关
    private int completedQuests;
    private int failedQuests;
    private long lastQuestTime;
    
    // 统计相关
    private int arrestsMade;
    private int rescuesMade;
    private int firesExtinguished;
    private int patientsTreated;
    private int inspectionsMade;
    private int mealsCooked;
    private int ordersServed;
    private int cropsHarvested;
    
    // 状态相关
    private boolean isOnDuty;
    private long playTime;
    private long lastLogin;
    
    public PlayerData(UUID playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.profession = ProfessionType.NONE;
        this.professionLevel = 1;
        this.professionExp = 0;
        this.lastSalaryTime = System.currentTimeMillis();
        this.money = 1000; // 初始资金
        this.skillPoints = 0;
        
        // 初始化技能
        this.skills = new HashMap<>();
        for (SkillType skill : SkillType.values()) {
            this.skills.put(skill, 1); // 初始等级1
        }
        
        // 初始化统计
        this.completedQuests = 0;
        this.failedQuests = 0;
        this.lastQuestTime = 0;
        
        this.arrestsMade = 0;
        this.rescuesMade = 0;
        this.firesExtinguished = 0;
        this.patientsTreated = 0;
        this.inspectionsMade = 0;
        this.mealsCooked = 0;
        this.ordersServed = 0;
        this.cropsHarvested = 0;
        
        this.isOnDuty = false;
        this.playTime = 0;
        this.lastLogin = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public UUID getPlayerId() {
        return playerId;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    public ProfessionType getProfession() {
        return profession;
    }
    
    public void setProfession(ProfessionType profession) {
        this.profession = profession;
    }
    
    public int getProfessionLevel() {
        return professionLevel;
    }
    
    public void setProfessionLevel(int professionLevel) {
        this.professionLevel = professionLevel;
    }
    
    public int getProfessionExp() {
        return professionExp;
    }
    
    public void setProfessionExp(int professionExp) {
        this.professionExp = professionExp;
    }
    
    public long getLastSalaryTime() {
        return lastSalaryTime;
    }
    
    public void setLastSalaryTime(long lastSalaryTime) {
        this.lastSalaryTime = lastSalaryTime;
    }
    
    public double getMoney() {
        return money;
    }
    
    public void setMoney(double money) {
        this.money = money;
    }
    
    public void addMoney(double amount) {
        this.money += amount;
    }
    
    public boolean removeMoney(double amount) {
        if (this.money >= amount) {
            this.money -= amount;
            return true;
        }
        return false;
    }
    
    public int getSkillPoints() {
        return skillPoints;
    }
    
    public void setSkillPoints(int skillPoints) {
        this.skillPoints = skillPoints;
    }
    
    public void addSkillPoints(int points) {
        this.skillPoints += points;
    }
    
    public boolean useSkillPoints(int points) {
        if (this.skillPoints >= points) {
            this.skillPoints -= points;
            return true;
        }
        return false;
    }
    
    public Map<SkillType, Integer> getSkills() {
        return skills;
    }
    
    public int getSkillLevel(SkillType skill) {
        return skills.getOrDefault(skill, 1);
    }
    
    public void setSkillLevel(SkillType skill, int level) {
        skills.put(skill, Math.min(level, skill.getMaxLevel()));
    }
    
    public boolean upgradeSkill(SkillType skill) {
        int currentLevel = getSkillLevel(skill);
        if (currentLevel >= skill.getMaxLevel()) {
            return false;
        }
        
        int requiredPoints = skill.getRequiredExp(currentLevel) / 100;
        if (useSkillPoints(requiredPoints)) {
            setSkillLevel(skill, currentLevel + 1);
            return true;
        }
        return false;
    }
    
    public int getCompletedQuests() {
        return completedQuests;
    }
    
    public void setCompletedQuests(int completedQuests) {
        this.completedQuests = completedQuests;
    }
    
    public void addCompletedQuest() {
        this.completedQuests++;
    }
    
    public int getFailedQuests() {
        return failedQuests;
    }
    
    public void setFailedQuests(int failedQuests) {
        this.failedQuests = failedQuests;
    }
    
    public void addFailedQuest() {
        this.failedQuests++;
    }
    
    public long getLastQuestTime() {
        return lastQuestTime;
    }
    
    public void setLastQuestTime(long lastQuestTime) {
        this.lastQuestTime = lastQuestTime;
    }
    
    // 统计相关方法
    public int getArrestsMade() {
        return arrestsMade;
    }
    
    public void addArrest() {
        this.arrestsMade++;
        addProfessionExp(50);
    }
    
    public int getRescuesMade() {
        return rescuesMade;
    }
    
    public void addRescue() {
        this.rescuesMade++;
        addProfessionExp(40);
    }
    
    public int getFiresExtinguished() {
        return firesExtinguished;
    }
    
    public void addFireExtinguished() {
        this.firesExtinguished++;
        addProfessionExp(30);
    }
    
    public int getPatientsTreated() {
        return patientsTreated;
    }
    
    public void addPatientTreated() {
        this.patientsTreated++;
        addProfessionExp(35);
    }
    
    public int getInspectionsMade() {
        return inspectionsMade;
    }
    
    public void addInspection() {
        this.inspectionsMade++;
        addProfessionExp(25);
    }
    
    public int getMealsCooked() {
        return mealsCooked;
    }
    
    public void addMealCooked() {
        this.mealsCooked++;
        addProfessionExp(20);
    }
    
    public int getOrdersServed() {
        return ordersServed;
    }
    
    public void addOrderServed() {
        this.ordersServed++;
        addProfessionExp(15);
    }
    
    public int getCropsHarvested() {
        return cropsHarvested;
    }
    
    public void addCropHarvested() {
        this.cropsHarvested++;
        addProfessionExp(10);
    }
    
    // 状态相关
    public boolean isOnDuty() {
        return isOnDuty;
    }
    
    public void setOnDuty(boolean onDuty) {
        this.isOnDuty = onDuty;
    }
    
    public long getPlayTime() {
        return playTime;
    }
    
    public void setPlayTime(long playTime) {
        this.playTime = playTime;
    }
    
    public void addPlayTime(long time) {
        this.playTime += time;
    }
    
    public long getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    // 业务逻辑方法
    public void addProfessionExp(int exp) {
        this.professionExp += exp;
        
        // 检查升级
        int requiredExp = getRequiredExpForNextLevel();
        while (this.professionExp >= requiredExp && this.professionLevel < 50) {
            this.professionExp -= requiredExp;
            this.professionLevel++;
            requiredExp = getRequiredExpForNextLevel();
            
            // 升级奖励
            this.skillPoints += 5;
            this.money += 100 * this.professionLevel;
            
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.sendMessage("§6§l[职业系统] §a恭喜你升到 " + professionLevel + " 级！");
                player.sendMessage("§7获得 §e5 技能点 §7和 §6" + (100 * professionLevel) + " 职业币§7！");
            }
        }
    }
    
    public int getRequiredExpForNextLevel() {
        return (int) (100 * Math.pow(1.5, professionLevel - 1));
    }
    
    public double getSalary() {
        if (profession == ProfessionType.NONE) {
            return 0;
        }
        return profession.getBaseSalary() * (1 + (professionLevel - 1) * 0.1);
    }
    
    public boolean canChangeProfession() {
        return System.currentTimeMillis() - lastSalaryTime > 24 * 60 * 60 * 1000; // 24小时冷却
    }
    
    public void resetProfessionChangeCooldown() {
        this.lastSalaryTime = System.currentTimeMillis();
    }
    
    /**
     * 转换为Map用于保存
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        
        map.put("playerName", playerName);
        map.put("profession", profession.name());
        map.put("professionLevel", professionLevel);
        map.put("professionExp", professionExp);
        map.put("lastSalaryTime", lastSalaryTime);
        map.put("money", money);
        map.put("skillPoints", skillPoints);
        
        // 保存技能
        Map<String, Integer> skillMap = new HashMap<>();
        for (Map.Entry<SkillType, Integer> entry : skills.entrySet()) {
            skillMap.put(entry.getKey().name(), entry.getValue());
        }
        map.put("skills", skillMap);
        
        // 保存统计
        map.put("completedQuests", completedQuests);
        map.put("failedQuests", failedQuests);
        map.put("lastQuestTime", lastQuestTime);
        
        map.put("arrestsMade", arrestsMade);
        map.put("rescuesMade", rescuesMade);
        map.put("firesExtinguished", firesExtinguished);
        map.put("patientsTreated", patientsTreated);
        map.put("inspectionsMade", inspectionsMade);
        map.put("mealsCooked", mealsCooked);
        map.put("ordersServed", ordersServed);
        map.put("cropsHarvested", cropsHarvested);
        
        map.put("isOnDuty", isOnDuty);
        map.put("playTime", playTime);
        map.put("lastLogin", lastLogin);
        
        return map;
    }
    
    /**
     * 从Map加载数据
     */
    public static PlayerData fromMap(UUID playerId, Map<String, Object> map) {
        String playerName = (String) map.getOrDefault("playerName", "Unknown");
        PlayerData data = new PlayerData(playerId, playerName);
        
        data.profession = ProfessionType.fromString((String) map.getOrDefault("profession", "NONE"));
        data.professionLevel = (int) map.getOrDefault("professionLevel", 1);
        data.professionExp = (int) map.getOrDefault("professionExp", 0);
        data.lastSalaryTime = (long) map.getOrDefault("lastSalaryTime", System.currentTimeMillis());
        data.money = ((Number) map.getOrDefault("money", 1000.0)).doubleValue();
        data.skillPoints = (int) map.getOrDefault("skillPoints", 0);
        
        // 加载技能
        if (map.containsKey("skills")) {
            Map<String, Integer> skillMap = (Map<String, Integer>) map.get("skills");
            for (Map.Entry<String, Integer> entry : skillMap.entrySet()) {
                try {
                    SkillType skill = SkillType.valueOf(entry.getKey());
                    data.skills.put(skill, entry.getValue());
                } catch (IllegalArgumentException ignored) {}
            }
        }
        
        // 加载统计
        data.completedQuests = (int) map.getOrDefault("completedQuests", 0);
        data.failedQuests = (int) map.getOrDefault("failedQuests", 0);
        data.lastQuestTime = (long) map.getOrDefault("lastQuestTime", 0L);
        
        data.arrestsMade = (int) map.getOrDefault("arrestsMade", 0);
        data.rescuesMade = (int) map.getOrDefault("rescuesMade", 0);
        data.firesExtinguished = (int) map.getOrDefault("firesExtinguished", 0);
        data.patientsTreated = (int) map.getOrDefault("patientsTreated", 0);
        data.inspectionsMade = (int) map.getOrDefault("inspectionsMade", 0);
        data.mealsCooked = (int) map.getOrDefault("mealsCooked", 0);
        data.ordersServed = (int) map.getOrDefault("ordersServed", 0);
        data.cropsHarvested = (int) map.getOrDefault("cropsHarvested", 0);
        
        data.isOnDuty = (boolean) map.getOrDefault("isOnDuty", false);
        data.playTime = (long) map.getOrDefault("playTime", 0L);
        data.lastLogin = (long) map.getOrDefault("lastLogin", System.currentTimeMillis());
        
        return data;
    }
}