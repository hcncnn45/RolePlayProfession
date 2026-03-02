package io.Sriptirc_wp_1198.roleplayprofession.data;

import io.Sriptirc_wp_1198.roleplayprofession.Roleplayprofession;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * 数据管理器
 */
public class DataManager {
    private final Roleplayprofession plugin;
    private final Map<UUID, PlayerData> playerDataMap;
    private final File dataFolder;
    
    public DataManager(Roleplayprofession plugin) {
        this.plugin = plugin;
        this.playerDataMap = new HashMap<>();
        this.dataFolder = new File(plugin.getDataFolder(), "players");
        
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }
    
    /**
     * 加载玩家数据
     */
    public PlayerData loadPlayerData(UUID playerId, String playerName) {
        // 先从内存中查找
        if (playerDataMap.containsKey(playerId)) {
            return playerDataMap.get(playerId);
        }
        
        // 从文件加载
        File playerFile = new File(dataFolder, playerId.toString() + ".yml");
        PlayerData playerData;
        
        if (playerFile.exists()) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
                Map<String, Object> dataMap = config.getValues(false);
                playerData = PlayerData.fromMap(playerId, dataMap);
                playerData.setPlayerName(playerName);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "加载玩家数据失败: " + playerId, e);
                playerData = new PlayerData(playerId, playerName);
            }
        } else {
            playerData = new PlayerData(playerId, playerName);
        }
        
        playerDataMap.put(playerId, playerData);
        return playerData;
    }
    
    /**
     * 保存玩家数据
     */
    public void savePlayerData(UUID playerId) {
        PlayerData playerData = playerDataMap.get(playerId);
        if (playerData == null) {
            return;
        }
        
        File playerFile = new File(dataFolder, playerId.toString() + ".yml");
        try {
            YamlConfiguration config = new YamlConfiguration();
            
            // 将玩家数据转换为Map并保存
            Map<String, Object> dataMap = playerData.toMap();
            for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                config.set(entry.getKey(), entry.getValue());
            }
            
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "保存玩家数据失败: " + playerId, e);
        }
    }
    
    /**
     * 保存所有玩家数据
     */
    public void saveAllPlayerData() {
        for (UUID playerId : playerDataMap.keySet()) {
            savePlayerData(playerId);
        }
    }
    
    /**
     * 获取玩家数据
     */
    public PlayerData getPlayerData(UUID playerId) {
        return playerDataMap.get(playerId);
    }
    
    /**
     * 获取玩家数据（如果不存在则创建）
     */
    public PlayerData getOrCreatePlayerData(UUID playerId, String playerName) {
        PlayerData data = playerDataMap.get(playerId);
        if (data == null) {
            data = loadPlayerData(playerId, playerName);
        }
        return data;
    }
    
    /**
     * 移除玩家数据（玩家退出时）
     */
    public void removePlayerData(UUID playerId) {
        PlayerData playerData = playerDataMap.get(playerId);
        if (playerData != null) {
            // 更新游戏时间
            long currentTime = System.currentTimeMillis();
            long sessionTime = currentTime - playerData.getLastLogin();
            playerData.addPlayTime(sessionTime);
            
            // 保存并移除
            savePlayerData(playerId);
            playerDataMap.remove(playerId);
        }
    }
    
    /**
     * 自动保存任务
     */
    public void startAutoSave() {
        // 使用异步保存，避免阻塞主线程
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().info("自动保存玩家数据...");
            }
            
            long startTime = System.currentTimeMillis();
            saveAllPlayerData();
            long elapsedTime = System.currentTimeMillis() - startTime;
            
            if (elapsedTime > 1000) {
                plugin.getLogger().warning("数据保存耗时较长: " + elapsedTime + "ms");
            }
        }, 20L * 60 * 5, 20L * 60 * 5); // 每5分钟保存一次
    }
    
    /**
     * 获取所有在线玩家的数据
     */
    public Map<UUID, PlayerData> getOnlinePlayersData() {
        Map<UUID, PlayerData> onlineData = new HashMap<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = playerDataMap.get(player.getUniqueId());
            if (data != null) {
                onlineData.put(player.getUniqueId(), data);
            }
        }
        return onlineData;
    }
    
    /**
     * 清理过期数据（可选）
     */
    public void cleanupOldData(long days) {
        long cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000);
        File[] playerFiles = dataFolder.listFiles();
        
        if (playerFiles != null) {
            for (File file : playerFiles) {
                if (file.isFile() && file.getName().endsWith(".yml")) {
                    if (file.lastModified() < cutoffTime) {
                        try {
                            // 解析UUID
                            String fileName = file.getName();
                            String uuidStr = fileName.substring(0, fileName.length() - 4);
                            UUID playerId = UUID.fromString(uuidStr);
                            
                            // 如果玩家不在线，删除数据
                            if (Bukkit.getPlayer(playerId) == null) {
                                playerDataMap.remove(playerId);
                                file.delete();
                            }
                        } catch (IllegalArgumentException ignored) {
                            // 文件名不是有效的UUID，直接删除
                            file.delete();
                        }
                    }
                }
            }
        }
    }
}