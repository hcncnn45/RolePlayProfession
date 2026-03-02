package io.Sriptirc_wp_1198.roleplayprofession.economy;

import io.Sriptirc_wp_1198.roleplayprofession.Roleplayprofession;
import io.Sriptirc_wp_1198.roleplayprofession.data.DataManager;
import io.Sriptirc_wp_1198.roleplayprofession.data.PlayerData;
import io.Sriptirc_wp_1198.roleplayprofession.profession.ProfessionType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 经济管理器
 */
public class EconomyManager {
    private final Roleplayprofession plugin;
    private final DataManager dataManager;
    private final DecimalFormat decimalFormat;
    
    // 商店物品价格
    private final Map<String, Double> shopPrices;
    
    public EconomyManager(Roleplayprofession plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.decimalFormat = new DecimalFormat("#,##0.00");
        
        // 初始化商店价格
        this.shopPrices = new HashMap<>();
        initializeShopPrices();
    }
    
    private void initializeShopPrices() {
        // 警察装备
        shopPrices.put("police_baton", 500.0);
        shopPrices.put("police_uniform", 1000.0);
        shopPrices.put("handcuffs", 300.0);
        
        // 消防装备
        shopPrices.put("fire_axe", 400.0);
        shopPrices.put("fire_uniform", 800.0);
        shopPrices.put("fire_extinguisher", 600.0);
        
        // 医疗装备
        shopPrices.put("first_aid_kit", 200.0);
        shopPrices.put("medic_uniform", 700.0);
        shopPrices.put("stretcher", 400.0);
        
        // 厨师装备
        shopPrices.put("chef_knife", 150.0);
        shopPrices.put("chef_hat", 100.0);
        shopPrices.put("cooking_pot", 250.0);
        
        // 农民装备
        shopPrices.put("farming_hoe", 120.0);
        shopPrices.put("watering_can", 80.0);
        shopPrices.put("seeds_pack", 50.0);
        
        // 通用物品
        shopPrices.put("work_uniform", 300.0);
        shopPrices.put("communication_device", 400.0);
        shopPrices.put("training_manual", 150.0);
    }
    
    /**
     * 获取玩家余额
     */
    public double getBalance(UUID playerId) {
        PlayerData data = dataManager.getPlayerData(playerId);
        return data != null ? data.getMoney() : 0;
    }
    
    /**
     * 格式化金额显示
     */
    public String formatMoney(double amount) {
        return plugin.getConfig().getString("economy.currency-symbol", "§6Ⓟ§f") + decimalFormat.format(amount);
    }
    
    /**
     * 给玩家加钱
     */
    public boolean addMoney(UUID playerId, double amount) {
        if (amount <= 0) return false;
        
        PlayerData data = dataManager.getPlayerData(playerId);
        if (data != null) {
            data.addMoney(amount);
            
            // 通知玩家
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.sendMessage("§a+" + formatMoney(amount));
            }
            return true;
        }
        return false;
    }
    
    /**
     * 从玩家扣钱
     */
    public boolean removeMoney(UUID playerId, double amount) {
        if (amount <= 0) return false;
        
        PlayerData data = dataManager.getPlayerData(playerId);
        if (data != null && data.removeMoney(amount)) {
            // 通知玩家
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.sendMessage("§c-" + formatMoney(amount));
            }
            return true;
        }
        return false;
    }
    
    /**
     * 转账
     */
    public boolean transferMoney(UUID fromPlayerId, UUID toPlayerId, double amount) {
        if (amount <= 0) return false;
        
        PlayerData fromData = dataManager.getPlayerData(fromPlayerId);
        PlayerData toData = dataManager.getPlayerData(toPlayerId);
        
        if (fromData != null && toData != null && fromData.removeMoney(amount)) {
            toData.addMoney(amount);
            
            // 通知双方
            Player fromPlayer = Bukkit.getPlayer(fromPlayerId);
            Player toPlayer = Bukkit.getPlayer(toPlayerId);
            
            if (fromPlayer != null) {
                fromPlayer.sendMessage("§c转账给 " + toData.getPlayerName() + ": -" + formatMoney(amount));
            }
            if (toPlayer != null) {
                toPlayer.sendMessage("§a收到来自 " + fromData.getPlayerName() + " 的转账: +" + formatMoney(amount));
            }
            return true;
        }
        return false;
    }
    
    /**
     * 发放工资
     */
    public void paySalary(UUID playerId) {
        PlayerData data = dataManager.getPlayerData(playerId);
        if (data == null || data.getProfession() == ProfessionType.NONE) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        long lastSalaryTime = data.getLastSalaryTime();
        long salaryInterval = plugin.getConfig().getLong("economy.salary-interval", 60) * 60 * 1000;
        
        if (currentTime - lastSalaryTime >= salaryInterval) {
            double salary = data.getSalary();
            if (salary > 0) {
                data.addMoney(salary);
                data.setLastSalaryTime(currentTime);
                
                Player player = Bukkit.getPlayer(playerId);
                if (player != null) {
                    player.sendMessage("§6§l[工资发放] §a你收到了 " + formatMoney(salary) + " 的工资！");
                }
            }
        }
    }
    
    /**
     * 发放所有在线玩家工资
     */
    public void payAllSalaries() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            paySalary(player.getUniqueId());
        }
    }
    
    /**
     * 开始自动工资发放任务
     */
    public void startSalaryTask() {
        long interval = plugin.getConfig().getLong("economy.salary-interval", 60) * 60 * 20; // 转换为tick
        
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            plugin.getLogger().info("发放工资...");
            payAllSalaries();
        }, interval, interval);
    }
    
    /**
     * 购买物品
     */
    public boolean purchaseItem(UUID playerId, String itemId) {
        Double price = shopPrices.get(itemId);
        if (price == null) {
            return false;
        }
        
        PlayerData data = dataManager.getPlayerData(playerId);
        if (data != null && data.removeMoney(price)) {
            // 这里可以添加物品给予逻辑
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.sendMessage("§a成功购买物品: " + getItemDisplayName(itemId));
                // TODO: 给予玩家实际物品
            }
            return true;
        }
        return false;
    }
    
    /**
     * 获取物品显示名称
     */
    private String getItemDisplayName(String itemId) {
        switch (itemId) {
            case "police_baton": return "警棍";
            case "police_uniform": return "警服";
            case "handcuffs": return "手铐";
            case "fire_axe": return "消防斧";
            case "fire_uniform": return "消防服";
            case "fire_extinguisher": return "灭火器";
            case "first_aid_kit": return "急救包";
            case "medic_uniform": return "医疗服";
            case "stretcher": return "担架";
            case "chef_knife": return "厨师刀";
            case "chef_hat": return "厨师帽";
            case "cooking_pot": return "烹饪锅";
            case "farming_hoe": return "农用锄";
            case "watering_can": return "浇水壶";
            case "seeds_pack": return "种子包";
            case "work_uniform": return "工作服";
            case "communication_device": return "通讯设备";
            case "training_manual": return "训练手册";
            default: return "未知物品";
        }
    }
    
    /**
     * 获取物品价格
     */
    public double getItemPrice(String itemId) {
        return shopPrices.getOrDefault(itemId, 0.0);
    }
    
    /**
     * 获取所有商店物品
     */
    public Map<String, Double> getShopItems() {
        return new HashMap<>(shopPrices);
    }
    
    /**
     * 设置物品价格（管理员用）
     */
    public void setItemPrice(String itemId, double price) {
        if (price >= 0) {
            shopPrices.put(itemId, price);
        }
    }
    
    /**
     * 给予每日登录奖励
     */
    public void giveDailyLoginBonus(UUID playerId) {
        double bonus = plugin.getConfig().getDouble("economy.daily-login-bonus", 100.0);
        addMoney(playerId, bonus);
        
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            player.sendMessage("§6§l[每日奖励] §a你获得了 " + formatMoney(bonus) + " 登录奖励！");
        }
    }
    
    /**
     * 完成任务奖励
     */
    public void giveQuestReward(UUID playerId, double baseReward, boolean isEmergency) {
        PlayerData data = dataManager.getPlayerData(playerId);
        if (data == null) return;
        
        double multiplier = isEmergency ? 2.0 : 1.0;
        double reward = baseReward * multiplier * (1 + (data.getProfessionLevel() - 1) * 0.05);
        
        addMoney(playerId, reward);
        
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            String type = isEmergency ? "紧急任务" : "日常任务";
            player.sendMessage("§6§l[任务完成] §a" + type + "奖励: " + formatMoney(reward));
        }
    }
}