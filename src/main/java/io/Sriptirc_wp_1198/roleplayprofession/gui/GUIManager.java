package io.Sriptirc_wp_1198.roleplayprofession.gui;

import io.Sriptirc_wp_1198.roleplayprofession.Roleplayprofession;
import io.Sriptirc_wp_1198.roleplayprofession.data.DataManager;
import io.Sriptirc_wp_1198.roleplayprofession.data.PlayerData;
import io.Sriptirc_wp_1198.roleplayprofession.economy.EconomyManager;
import io.Sriptirc_wp_1198.roleplayprofession.profession.ProfessionType;
import io.Sriptirc_wp_1198.roleplayprofession.profession.SkillType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * GUI管理器
 */
public class GUIManager implements Listener {
    private final Roleplayprofession plugin;
    private final DataManager dataManager;
    private final EconomyManager economyManager;
    
    // GUI标题
    private static final String MAIN_GUI_TITLE = "§6§l角色扮演职业系统";
    private static final String PROFESSION_GUI_TITLE = "§6§l职业选择";
    private static final String SKILL_GUI_TITLE = "§6§l技能升级";
    private static final String SHOP_GUI_TITLE = "§6§l职业商店";
    private static final String STATS_GUI_TITLE = "§6§l我的信息";
    private static final String QUEST_GUI_TITLE = "§6§l任务列表";
    
    // 正在查看GUI的玩家
    private final Set<UUID> viewingGUI;
    
    public GUIManager(Roleplayprofession plugin, DataManager dataManager, EconomyManager economyManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.economyManager = economyManager;
        this.viewingGUI = new HashSet<>();
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * 打开主菜单
     */
    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, MAIN_GUI_TITLE);
        
        // 边框
        setBorder(inv, Material.BLACK_STAINED_GLASS_PANE);
        
        // 职业选择按钮
        ItemStack professionBtn = createItem(Material.IRON_SWORD, "§6§l职业系统", 
            Arrays.asList("§7点击查看所有职业", "§7选择你的职业道路", "", "§e点击打开"));
        inv.setItem(20, professionBtn);
        
        // 技能升级按钮
        ItemStack skillBtn = createItem(Material.EXPERIENCE_BOTTLE, "§a§l技能升级", 
            Arrays.asList("§7提升你的职业技能", "§7获得更好的效果", "", "§e点击打开"));
        inv.setItem(22, skillBtn);
        
        // 职业商店按钮
        ItemStack shopBtn = createItem(Material.EMERALD, "§2§l职业商店", 
            Arrays.asList("§7购买职业装备和道具", "§7提升工作效率", "", "§e点击打开"));
        inv.setItem(24, shopBtn);
        
        // 任务系统按钮
        ItemStack questBtn = createItem(Material.BOOK, "§b§l任务系统", 
            Arrays.asList("§7查看可接任务", "§7完成任务获得奖励", "", "§e点击打开"));
        inv.setItem(30, questBtn);
        
        // 我的信息按钮
        ItemStack statsBtn = createItem(Material.PLAYER_HEAD, "§d§l我的信息", 
            Arrays.asList("§7查看个人信息", "§7职业等级和统计", "", "§e点击打开"));
        inv.setItem(32, statsBtn);
        
        // NPC交互按钮
        ItemStack npcBtn = createItem(Material.VILLAGER_SPAWN_EGG, "§5§lNPC交互", 
            Arrays.asList("§7与NPC路人互动", "§7接受NPC的任务", "", "§e点击打开"));
        inv.setItem(34, statsBtn);
        
        player.openInventory(inv);
        viewingGUI.add(player.getUniqueId());
    }
    
    /**
     * 打开职业选择GUI
     */
    public void openProfessionGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, PROFESSION_GUI_TITLE);
        
        // 边框
        setBorder(inv, Material.BLUE_STAINED_GLASS_PANE);
        
        // 职业按钮
        int[] professionSlots = {20, 21, 22, 23, 24, 29, 30, 31, 32, 33};
        ProfessionType[] professions = {
            ProfessionType.POLICE, ProfessionType.FIREFIGHTER, ProfessionType.MEDIC,
            ProfessionType.CUSTOMS, ProfessionType.CHEF, ProfessionType.WAITER,
            ProfessionType.FARMER, ProfessionType.NONE
        };
        
        PlayerData data = dataManager.getPlayerData(player.getUniqueId());
        ProfessionType currentProfession = data != null ? data.getProfession() : ProfessionType.NONE;
        
        for (int i = 0; i < Math.min(professions.length, professionSlots.length); i++) {
            ProfessionType profession = professions[i];
            Material icon = profession.getIcon();
            ChatColor color = profession.getColor();
            String name = profession.getColoredName() + "§r";
            
            List<String> lore = new ArrayList<>();
            lore.add("§7" + profession.getDescription());
            lore.add("");
            lore.add("§f解锁等级: §e" + profession.getUnlockLevel());
            lore.add("§f基础工资: " + economyManager.formatMoney(profession.getBaseSalary()) + "/小时");
            lore.add("");
            
            if (profession == currentProfession) {
                lore.add("§a✓ 当前职业");
                lore.add("§7等级: §e" + (data != null ? data.getProfessionLevel() : 1));
                lore.add("§7经验: §e" + (data != null ? data.getProfessionExp() : 0));
            } else if (data != null && data.getProfessionLevel() >= profession.getUnlockLevel()) {
                lore.add("§a✔ 已解锁");
                lore.add("§e点击选择");
            } else {
                lore.add("§c✗ 未解锁");
                lore.add("§7需要等级: §e" + profession.getUnlockLevel());
            }
            
            ItemStack item = createItem(icon, name, lore);
            inv.setItem(professionSlots[i], item);
        }
        
        // 返回按钮
        ItemStack backBtn = createItem(Material.ARROW, "§c返回主菜单", 
            Collections.singletonList("§7点击返回"));
        inv.setItem(49, backBtn);
        
        player.openInventory(inv);
        viewingGUI.add(player.getUniqueId());
    }
    
    /**
     * 打开技能升级GUI
     */
    public void openSkillGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, SKILL_GUI_TITLE);
        
        // 边框
        setBorder(inv, Material.GREEN_STAINED_GLASS_PANE);
        
        PlayerData data = dataManager.getPlayerData(player.getUniqueId());
        if (data == null) return;
        
        // 技能按钮
        int[] skillSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
        SkillType[] allSkills = SkillType.values();
        
        for (int i = 0; i < Math.min(allSkills.length, skillSlots.length); i++) {
            SkillType skill = allSkills[i];
            int currentLevel = data.getSkillLevel(skill);
            int maxLevel = skill.getMaxLevel();
            
            String name = skill.getColoredName() + " §rLv." + currentLevel;
            List<String> lore = new ArrayList<>();
            
            lore.add("§7" + skill.getDescription());
            lore.add("");
            lore.add("§f当前等级: §e" + currentLevel + " / " + maxLevel);
            lore.add("§f效果加成: §a+" + String.format("%.1f", (skill.getEffectMultiplier(currentLevel) - 1) * 100) + "%");
            lore.add("");
            
            if (currentLevel < maxLevel) {
                int requiredPoints = skill.getRequiredExp(currentLevel) / 100;
                lore.add("§f升级所需: §e" + requiredPoints + " 技能点");
                lore.add("§f当前拥有: §a" + data.getSkillPoints() + " 技能点");
                lore.add("");
                if (data.getSkillPoints() >= requiredPoints) {
                    lore.add("§a✔ 可升级");
                    lore.add("§e点击升级");
                } else {
                    lore.add("§c✗ 技能点不足");
                }
            } else {
                lore.add("§6★ 已满级");
            }
            
            Material icon = getSkillIcon(skill);
            ItemStack item = createItem(icon, name, lore);
            inv.setItem(skillSlots[i], item);
        }
        
        // 返回按钮
        ItemStack backBtn = createItem(Material.ARROW, "§c返回主菜单", 
            Collections.singletonList("§7点击返回"));
        inv.setItem(49, backBtn);
        
        player.openInventory(inv);
        viewingGUI.add(player.getUniqueId());
    }
    
    /**
     * 打开商店GUI
     */
    public void openShopGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, SHOP_GUI_TITLE);
        
        // 边框
        setBorder(inv, Material.YELLOW_STAINED_GLASS_PANE);
        
        PlayerData data = dataManager.getPlayerData(player.getUniqueId());
        if (data == null) return;
        
        // 商店物品
        Map<String, Double> shopItems = economyManager.getShopItems();
        int slot = 10;
        
        for (Map.Entry<String, Double> entry : shopItems.entrySet()) {
            if (slot >= 44) break; // 限制显示数量
            
            String itemId = entry.getKey();
            double price = entry.getValue();
            
            Material icon = getShopItemIcon(itemId);
            String name = getShopItemDisplayName(itemId);
            List<String> lore = new ArrayList<>();
            
            lore.add("§7" + getShopItemDescription(itemId));
            lore.add("");
            lore.add("§f价格: " + economyManager.formatMoney(price));
            lore.add("§f你的余额: " + economyManager.formatMoney(data.getMoney()));
            lore.add("");
            
            if (data.getMoney() >= price) {
                lore.add("§a✔ 可购买");
                lore.add("§e点击购买");
            } else {
                lore.add("§c✗ 余额不足");
            }
            
            ItemStack item = createItem(icon, "§f" + name, lore);
            inv.setItem(slot, item);
            
            slot++;
            if ((slot - 9) % 9 == 0) { // 跳过边框
                slot += 2;
            }
        }
        
        // 返回按钮
        ItemStack backBtn = createItem(Material.ARROW, "§c返回主菜单", 
            Collections.singletonList("§7点击返回"));
        inv.setItem(49, backBtn);
        
        player.openInventory(inv);
        viewingGUI.add(player.getUniqueId());
    }
    
    /**
     * 打开个人信息GUI
     */
    public void openStatsGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, STATS_GUI_TITLE);
        
        // 边框
        setBorder(inv, Material.PURPLE_STAINED_GLASS_PANE);
        
        PlayerData data = dataManager.getPlayerData(player.getUniqueId());
        if (data == null) return;
        
        // 玩家信息
        ItemStack playerInfo = createItem(Material.PLAYER_HEAD, "§6§l" + player.getName(),
            Arrays.asList(
                "§f职业: " + data.getProfession().getColoredName(),
                "§f等级: §e" + data.getProfessionLevel(),
                "§f经验: §e" + data.getProfessionExp() + " / " + data.getRequiredExpForNextLevel(),
                "§f技能点: §a" + data.getSkillPoints(),
                "§f余额: " + economyManager.formatMoney(data.getMoney())
            ));
        inv.setItem(13, playerInfo);
        
        // 统计信息
        ItemStack stats = createItem(Material.BOOK, "§b§l职业统计",
            Arrays.asList(
                "§f抓捕罪犯: §e" + data.getArrestsMade(),
                "§f救援次数: §e" + data.getRescuesMade(),
                "§f灭火次数: §e" + data.getFiresExtinguished(),
                "§f治疗病人: §e" + data.getPatientsTreated(),
                "§f检查次数: §e" + data.getInspectionsMade(),
                "§f烹饪餐点: §e" + data.getMealsCooked(),
                "§f服务订单: §e" + data.getOrdersServed(),
                "§f收获作物: §e" + data.getCropsHarvested(),
                "",
                "§f完成任务: §a" + data.getCompletedQuests(),
                "§f失败任务: §c" + data.getFailedQuests()
            ));
        inv.setItem(21, stats);
        
        // 技能信息
        ItemStack skills = createItem(Material.EXPERIENCE_BOTTLE, "§a§l技能等级",
            Arrays.asList(
                "§f抓捕技能: §eLv." + data.getSkillLevel(SkillType.ARREST),
                "§f消防技能: §eLv." + data.getSkillLevel(SkillType.FIREFIGHTING),
                "§f医疗技能: §eLv." + data.getSkillLevel(SkillType.MEDICAL),
                "§f检查技能: §eLv." + data.getSkillLevel(SkillType.INSPECTION),
                "§f烹饪技能: §eLv." + data.getSkillLevel(SkillType.COOKING),
                "§f服务技能: §eLv." + data.getSkillLevel(SkillType.SERVICE),
                "§f农业技能: §eLv." + data.getSkillLevel(SkillType.FARMING)
            ));
        inv.setItem(23, skills);
        
        // 状态信息
        ItemStack status = createItem(Material.CLOCK, "§e§l状态信息",
            Arrays.asList(
                "§f值班状态: " + (data.isOnDuty() ? "§a在岗" : "§c休息"),
                "§f游戏时间: §e" + formatTime(data.getPlayTime()),
                "§f上次登录: §e" + formatTimestamp(data.getLastLogin()),
                "",
                "§f下次工资: §e" + formatTimeRemaining(data.getLastSalaryTime())
            ));
        inv.setItem(31, status);
        
        // 返回按钮
        ItemStack backBtn = createItem(Material.ARROW, "§c返回主菜单", 
            Collections.singletonList("§7点击返回"));
        inv.setItem(49, backBtn);
        
        player.openInventory(inv);
        viewingGUI.add(player.getUniqueId());
    }
    
    /**
     * 打开任务GUI
     */
    public void openQuestGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, QUEST_GUI_TITLE);
        
        // 边框
        setBorder(inv, Material.CYAN_STAINED_GLASS_PANE);
        
        // TODO: 实现任务列表
        
        // 返回按钮
        ItemStack backBtn = createItem(Material.ARROW, "§c返回主菜单", 
            Collections.singletonList("§7点击返回"));
        inv.setItem(49, backBtn);
        
        player.openInventory(inv);
        viewingGUI.add(player.getUniqueId());
    }
    
    /**
     * 处理GUI点击事件
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        UUID playerId = player.getUniqueId();
        
        if (!viewingGUI.contains(playerId)) return;
        
        event.setCancelled(true);
        
        String title = event.getView().getTitle();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        // 根据GUI标题处理点击
        if (title.equals(MAIN_GUI_TITLE)) {
            handleMainMenuClick(player, event.getSlot());
        } else if (title.equals(PROFESSION_GUI_TITLE)) {
            handleProfessionMenuClick(player, event.getSlot());
        } else if (title.equals(SKILL_GUI_TITLE)) {
            handleSkillMenuClick(player, event.getSlot());
        } else if (title.equals(SHOP_GUI_TITLE)) {
            handleShopMenuClick(player, event.getSlot());
        } else if (title.equals(STATS_GUI_TITLE)) {
            handleStatsMenuClick(player, event.getSlot());
        } else if (title.equals(QUEST_GUI_TITLE)) {
            handleQuestMenuClick(player, event.getSlot());
        }
    }
    
    /**
     * 处理GUI关闭事件
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            viewingGUI.remove(event.getPlayer().getUniqueId());
        }
    }
    
    // 私有辅助方法
    private void handleMainMenuClick(Player player, int slot) {
        switch (slot) {
            case 20:
                openProfessionGUI(player);
                break;
            case 22:
                openSkillGUI(player);
                break;
            case 24:
                openShopGUI(player);
                break;
            case 30:
                openQuestGUI(player);
                break;
            case 32:
                openStatsGUI(player);
                break;
            case 34:
                // TODO: 打开NPC交互GUI
                player.sendMessage("§eNPC交互功能开发中...");
                break;
        }
    }
    
    private void handleProfessionMenuClick(Player player, int slot) {
        if (slot == 49) {
            openMainMenu(player);
            return;
        }
        
        // 职业槽位映射
        Map<Integer, ProfessionType> slotProfessionMap = new HashMap<>();
        slotProfessionMap.put(20, ProfessionType.POLICE);
        slotProfessionMap.put(21, ProfessionType.FIREFIGHTER);
        slotProfessionMap.put(22, ProfessionType.MEDIC);
        slotProfessionMap.put(23, ProfessionType.CUSTOMS);
        slotProfessionMap.put(24, ProfessionType.CHEF);
        slotProfessionMap.put(29, ProfessionType.WAITER);
        slotProfessionMap.put(30, ProfessionType.FARMER);
        slotProfessionMap.put(31, ProfessionType.NONE);
        
        ProfessionType selected = slotProfessionMap.get(slot);
        if (selected != null) {
            // TODO: 实现职业选择逻辑
            player.sendMessage("§e选择了职业: " + selected.getColoredName());
        }
    }
    
    private void handleSkillMenuClick(Player player, int slot) {
        if (slot == 49) {
            openMainMenu(player);
            return;
        }
        
        // TODO: 实现技能升级逻辑
        player.sendMessage("§e技能升级功能开发中...");
    }
    
    private void handleShopMenuClick(Player player, int slot) {
        if (slot == 49) {
            openMainMenu(player);
            return;
        }
        
        // TODO: 实现商店购买逻辑
        player.sendMessage("§e商店购买功能开发中...");
    }
    
    private void handleStatsMenuClick(Player player, int slot) {
        if (slot == 49) {
            openMainMenu(player);
        }
    }
    
    private void handleQuestMenuClick(Player player, int slot) {
        if (slot == 49) {
            openMainMenu(player);
        }
    }
    
    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private void setBorder(Inventory inv, Material borderMaterial) {
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, createItem(borderMaterial, " ", Collections.emptyList()));
            inv.setItem(i + 45, createItem(borderMaterial, " ", Collections.emptyList()));
        }
        
        for (int i = 0; i < 54; i += 9) {
            inv.setItem(i, createItem(borderMaterial, " ", Collections.emptyList()));
            inv.setItem(i + 8, createItem(borderMaterial, " ", Collections.emptyList()));
        }
    }
    
    private Material getSkillIcon(SkillType skill) {
        switch (skill) {
            case ARREST: return Material.IRON_SWORD;
            case FIREFIGHTING: return Material.WATER_BUCKET;
            case MEDICAL: return Material.POTION;
            case INSPECTION: return Material.PAPER;
            case COOKING: return Material.COOKED_BEEF;
            case SERVICE: return Material.CAKE;
            case FARMING: return Material.WHEAT;
            case LEADERSHIP: return Material.GOLDEN_HELMET;
            case COMMUNICATION: return Material.BOOK;
            case PHYSICAL: return Material.IRON_CHESTPLATE;
            default: return Material.EXPERIENCE_BOTTLE;
        }
    }
    
    private Material getShopItemIcon(String itemId) {
        switch (itemId) {
            case "police_baton": return Material.STICK;
            case "police_uniform": return Material.LEATHER_CHESTPLATE;
            case "handcuffs": return Material.IRON_NUGGET;
            case "fire_axe": return Material.IRON_AXE;
            case "fire_uniform": return Material.LEATHER_CHESTPLATE;
            case "fire_extinguisher": return Material.FIRE_CHARGE;
            case "first_aid_kit": return Material.PAPER;
            case "medic_uniform": return Material.LEATHER_CHESTPLATE;
            case "stretcher": return Material.OAK_PLANKS;
            case "chef_knife": return Material.IRON_SWORD;
            case "chef_hat": return Material.LEATHER_HELMET;
            case "cooking_pot": return Material.CAULDRON;
            case "farming_hoe": return Material.IRON_HOE;
            case "watering_can": return Material.WATER_BUCKET;
            case "seeds_pack": return Material.WHEAT_SEEDS;
            case "work_uniform": return Material.LEATHER_CHESTPLATE;
            case "communication_device": return Material.COMPASS;
            case "training_manual": return Material.BOOK;
            default: return Material.BARRIER;
        }
    }
    
    private String getShopItemDisplayName(String itemId) {
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
    
    private String getShopItemDescription(String itemId) {
        switch (itemId) {
            case "police_baton": return "警察专用装备，提高抓捕效率";
            case "police_uniform": return "警察制服，显示职业身份";
            case "handcuffs": return "用于抓捕罪犯的工具";
            case "fire_axe": return "消防员专用工具，用于破拆";
            case "fire_uniform": return "防火服，保护消防员安全";
            case "fire_extinguisher": return "便携式灭火设备";
            case "first_aid_kit": return "医疗急救用品";
            case "medic_uniform": return "医护人员制服";
            case "stretcher": return "运送伤员的工具";
            case "chef_knife": return "专业厨师刀具";
            case "chef_hat": return "厨师职业标志";
            case "cooking_pot": return "烹饪必备工具";
            case "farming_hoe": return "农民专用工具";
            case "watering_can": return "浇水工具";
            case "seeds_pack": return "各种作物种子";
            case "work_uniform": return "通用工作服";
            case "communication_device": return "团队通讯设备";
            case "training_manual": return "技能训练手册";
            default: return "未知物品";
        }
    }
    
    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        
        if (hours > 0) {
            return hours + "小时 " + minutes + "分钟";
        } else {
            return minutes + "分钟";
        }
    }
    
    private String formatTimestamp(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        if (diff < 60000) {
            return "刚刚";
        } else if (diff < 3600000) {
            return (diff / 60000) + "分钟前";
        } else if (diff < 86400000) {
            return (diff / 3600000) + "小时前";
        } else {
            return (diff / 86400000) + "天前";
        }
    }
    
    private String formatTimeRemaining(long lastSalaryTime) {
        long salaryInterval = plugin.getConfig().getLong("economy.salary-interval", 60) * 60 * 1000;
        long nextSalaryTime = lastSalaryTime + salaryInterval;
        long remaining = nextSalaryTime - System.currentTimeMillis();
        
        if (remaining <= 0) {
            return "即将发放";
        }
        
        long minutes = remaining / 60000;
        if (minutes < 60) {
            return minutes + "分钟后";
        } else {
            return (minutes / 60) + "小时后";
        }
    }
}