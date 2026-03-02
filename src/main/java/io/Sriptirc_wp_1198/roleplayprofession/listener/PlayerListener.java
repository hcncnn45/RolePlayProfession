package io.Sriptirc_wp_1198.roleplayprofession.listener;

import io.Sriptirc_wp_1198.roleplayprofession.Roleplayprofession;
import io.Sriptirc_wp_1198.roleplayprofession.data.DataManager;
import io.Sriptirc_wp_1198.roleplayprofession.data.PlayerData;
import io.Sriptirc_wp_1198.roleplayprofession.economy.EconomyManager;
import io.Sriptirc_wp_1198.roleplayprofession.profession.ProfessionType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 玩家监听器
 */
public class PlayerListener implements Listener {
    private final Roleplayprofession plugin;
    private final DataManager dataManager;
    private final EconomyManager economyManager;
    
    // 冷却时间记录
    private final Map<UUID, Long> arrestCooldown;
    private final Map<UUID, Long> rescueCooldown;
    private final Map<UUID, Long> fireCooldown;
    
    public PlayerListener(Roleplayprofession plugin, DataManager dataManager, EconomyManager economyManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.economyManager = economyManager;
        
        this.arrestCooldown = new HashMap<>();
        this.rescueCooldown = new HashMap<>();
        this.fireCooldown = new HashMap<>();
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // 加载玩家数据
        PlayerData data = dataManager.loadPlayerData(playerId, player.getName());
        
        // 更新登录时间
        data.setLastLogin(System.currentTimeMillis());
        
        // 给予每日登录奖励
        giveDailyLoginBonus(playerId);
        
        // 发送欢迎消息
        if (data.getProfession() != ProfessionType.NONE) {
            player.sendMessage("§6§l[职业系统] §a欢迎回来，" + data.getProfession().getColoredName() + "§a！");
            player.sendMessage("§7当前等级: §e" + data.getProfessionLevel() + 
                             " §7| 余额: " + economyManager.formatMoney(data.getMoney()));
        } else {
            player.sendMessage("§6§l[职业系统] §a欢迎来到角色扮演职业系统！");
            player.sendMessage("§7使用 §e/rp §7打开菜单选择你的职业！");
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // 移除玩家数据
        dataManager.removePlayerData(playerId);
        
        // 清理冷却记录
        arrestCooldown.remove(playerId);
        rescueCooldown.remove(playerId);
        fireCooldown.remove(playerId);
    }
    
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PlayerData data = dataManager.getPlayerData(player.getUniqueId());
        
        if (data != null && data.getProfession() == ProfessionType.MEDIC) {
            // 医护人员复活后给予医疗包
            // TODO: 给予医疗物品
            player.sendMessage("§a作为医护人员，你获得了一个医疗包！");
        }
    }
    
    @EventHandler
    public void onPlayerLevelChange(PlayerLevelChangeEvent event) {
        Player player = event.getPlayer();
        int newLevel = event.getNewLevel();
        int oldLevel = event.getOldLevel();
        
        if (newLevel > oldLevel) {
            PlayerData data = dataManager.getPlayerData(player.getUniqueId());
            if (data != null) {
                // 等级提升奖励
                int levelDiff = newLevel - oldLevel;
                double reward = levelDiff * 100;
                economyManager.addMoney(player.getUniqueId(), reward);
                
                player.sendMessage("§6§l[等级提升] §a恭喜升到 " + newLevel + " 级！");
                player.sendMessage("§7获得奖励: " + economyManager.formatMoney(reward));
            }
        }
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerData data = dataManager.getPlayerData(player.getUniqueId());
        
        if (data != null) {
            // 死亡惩罚
            double penalty = data.getMoney() * 0.05; // 损失5%的钱
            if (penalty > 0) {
                data.removeMoney(penalty);
                player.sendMessage("§c死亡惩罚: 损失 " + economyManager.formatMoney(penalty));
            }
            
            // 如果是医护人员死亡，额外惩罚
            if (data.getProfession() == ProfessionType.MEDIC) {
                player.sendMessage("§c作为医护人员，你的死亡造成了更大的损失！");
            }
        }
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player damager = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();
        
        PlayerData damagerData = dataManager.getPlayerData(damager.getUniqueId());
        PlayerData victimData = dataManager.getPlayerData(victim.getUniqueId());
        
        if (damagerData == null || victimData == null) return;
        
        // 警察抓捕逻辑
        if (damagerData.getProfession() == ProfessionType.POLICE && 
            damagerData.isOnDuty() && 
            victimData.getProfession() != ProfessionType.POLICE) {
            
            // 检查冷却
            if (isOnCooldown(damager.getUniqueId(), arrestCooldown, 
                plugin.getConfig().getLong("interactions.arrest.cooldown", 30))) {
                damager.sendMessage("§c抓捕技能冷却中！");
                return;
            }
            
            // 检查距离
            double maxDistance = plugin.getConfig().getDouble("interactions.arrest.max-distance", 10.0);
            if (damager.getLocation().distance(victim.getLocation()) > maxDistance) {
                damager.sendMessage("§c目标太远了！");
                return;
            }
            
            // 抓捕成功
            double successRate = 0.7 + (damagerData.getSkillLevel(damagerData.getProfession().getPrimarySkill()) * 0.05);
            if (Math.random() < successRate) {
                // 抓捕成功
                damagerData.addArrest();
                double reward = plugin.getConfig().getDouble("interactions.arrest.reward", 200.0);
                economyManager.addMoney(damager.getUniqueId(), reward);
                
                damager.sendMessage("§a成功抓捕罪犯！奖励: " + economyManager.formatMoney(reward));
                victim.sendMessage("§c你被警察逮捕了！");
                
                // 设置冷却
                setCooldown(damager.getUniqueId(), arrestCooldown);
                
                // 广播消息
                Bukkit.broadcastMessage("§6§l[警察行动] §c" + victim.getName() + " §7被 §9" + 
                                      damager.getName() + " §7逮捕！");
            } else {
                damager.sendMessage("§c抓捕失败！目标逃脱了");
            }
            
            event.setCancelled(true); // 取消伤害
        }
        
        // 医护人员救援逻辑（当玩家攻击受伤的玩家时）
        if (damagerData.getProfession() == ProfessionType.MEDIC && 
            damagerData.isOnDuty() && 
            victim.getHealth() < 10) {
            
            // 检查冷却
            if (isOnCooldown(damager.getUniqueId(), rescueCooldown, 
                plugin.getConfig().getLong("interactions.rescue.cooldown", 20))) {
                damager.sendMessage("§c救援技能冷却中！");
                return;
            }
            
            // 救援成功
            damagerData.addRescue();
            double reward = plugin.getConfig().getDouble("interactions.rescue.reward", 150.0);
            economyManager.addMoney(damager.getUniqueId(), reward);
            
            // 治疗受害者
            victim.setHealth(Math.min(victim.getHealth() + 10, victim.getMaxHealth()));
            
            damager.sendMessage("§a成功救援伤员！奖励: " + economyManager.formatMoney(reward));
            victim.sendMessage("§a你被医护人员治疗了！");
            
            // 设置冷却
            setCooldown(damager.getUniqueId(), rescueCooldown);
            
            event.setCancelled(true); // 取消伤害
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        
        Player player = (Player) event.getEntity();
        PlayerData data = dataManager.getPlayerData(player.getUniqueId());
        
        if (data == null) return;
        
        // 消防员灭火逻辑（当玩家受到火焰伤害时）
        if (data.getProfession() == ProfessionType.FIREFIGHTER && 
            data.isOnDuty() && 
            (event.getCause() == EntityDamageEvent.DamageCause.FIRE ||
             event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK ||
             event.getCause() == EntityDamageEvent.DamageCause.LAVA)) {
            
            // 检查冷却
            if (isOnCooldown(player.getUniqueId(), fireCooldown, 
                plugin.getConfig().getLong("interactions.rescue.cooldown", 20))) {
                return;
            }
            
            // 灭火成功
            data.addFireExtinguished();
            double reward = plugin.getConfig().getDouble("interactions.rescue.reward", 150.0) * 0.5;
            economyManager.addMoney(player.getUniqueId(), reward);
            
            player.sendMessage("§a成功灭火！奖励: " + economyManager.formatMoney(reward));
            
            // 设置冷却
            setCooldown(player.getUniqueId(), fireCooldown);
            
            // 取消火焰伤害
            event.setCancelled(true);
            player.setFireTicks(0);
        }
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerData data = dataManager.getPlayerData(player.getUniqueId());
        
        if (data == null) return;
        
        // 农民收获逻辑
        if (data.getProfession() == ProfessionType.FARMER && data.isOnDuty()) {
            Material blockType = event.getBlock().getType();
            
            if (isCrop(blockType)) {
                data.addCropHarvested();
                double reward = 10 * data.getSkillLevel(data.getProfession().getPrimarySkill());
                economyManager.addMoney(player.getUniqueId(), reward);
                
                player.sendMessage("§a收获作物！奖励: " + economyManager.formatMoney(reward));
            }
        }
    }
    
    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        PlayerData data = dataManager.getPlayerData(player.getUniqueId());
        
        if (data == null) return;
        
        // 厨师烹饪逻辑
        if (data.getProfession() == ProfessionType.CHEF && data.isOnDuty()) {
            Material resultType = event.getRecipe().getResult().getType();
            
            if (isFood(resultType)) {
                data.addMealCooked();
                double reward = 20 * data.getSkillLevel(data.getProfession().getPrimarySkill());
                economyManager.addMoney(player.getUniqueId(), reward);
                
                player.sendMessage("§a成功烹饪！奖励: " + economyManager.formatMoney(reward));
            }
        }
    }
    
    @EventHandler
    public void onFurnaceSmelt(FurnaceSmeltEvent event) {
        // 厨师烹饪逻辑（熔炉）
        // TODO: 需要记录哪个玩家放置的熔炉
    }
    
    /**
     * 给予每日登录奖励
     */
    private void giveDailyLoginBonus(UUID playerId) {
        long lastLogin = dataManager.getPlayerData(playerId).getLastLogin();
        long currentTime = System.currentTimeMillis();
        long oneDay = 24 * 60 * 60 * 1000;
        
        if (currentTime - lastLogin >= oneDay) {
            economyManager.giveDailyLoginBonus(playerId);
        }
    }
    
    /**
     * 检查是否在冷却中
     */
    private boolean isOnCooldown(UUID playerId, Map<UUID, Long> cooldownMap, long cooldownSeconds) {
        if (!cooldownMap.containsKey(playerId)) {
            return false;
        }
        
        long lastTime = cooldownMap.get(playerId);
        long currentTime = System.currentTimeMillis();
        long cooldownMillis = cooldownSeconds * 1000;
        
        return currentTime - lastTime < cooldownMillis;
    }
    
    /**
     * 设置冷却时间
     */
    private void setCooldown(UUID playerId, Map<UUID, Long> cooldownMap) {
        cooldownMap.put(playerId, System.currentTimeMillis());
    }
    
    /**
     * 判断是否为作物
     */
    private boolean isCrop(Material material) {
        return material == Material.WHEAT || 
               material == Material.CARROTS || 
               material == Material.POTATOES || 
               material == Material.BEETROOTS || 
               material == Material.MELON || 
               material == Material.PUMPKIN ||
               material == Material.SUGAR_CANE ||
               material == Material.CACTUS ||
               material == Material.BAMBOO ||
               material == Material.KELP ||
               material == Material.SEA_PICKLE ||
               material == Material.SWEET_BERRY_BUSH;
    }
    
    /**
     * 判断是否为食物
     */
    private boolean isFood(Material material) {
        return material.isEdible() || 
               material == Material.CAKE || 
               material == Material.PUMPKIN_PIE ||
               material == Material.COOKIE ||
               material == Material.BREAD ||
               material == Material.GOLDEN_APPLE ||
               material == Material.ENCHANTED_GOLDEN_APPLE;
    }
}