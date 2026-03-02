package io.Sriptirc_wp_1198.roleplayprofession.command;

import io.Sriptirc_wp_1198.roleplayprofession.Roleplayprofession;
import io.Sriptirc_wp_1198.roleplayprofession.data.DataManager;
import io.Sriptirc_wp_1198.roleplayprofession.data.PlayerData;
import io.Sriptirc_wp_1198.roleplayprofession.economy.EconomyManager;
import io.Sriptirc_wp_1198.roleplayprofession.gui.GUIManager;
import io.Sriptirc_wp_1198.roleplayprofession.profession.ProfessionType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * 命令处理器
 */
public class CommandHandler implements CommandExecutor, TabCompleter {
    private final Roleplayprofession plugin;
    private final DataManager dataManager;
    private final EconomyManager economyManager;
    private final GUIManager guiManager;
    
    public CommandHandler(Roleplayprofession plugin, DataManager dataManager, 
                         EconomyManager economyManager, GUIManager guiManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.economyManager = economyManager;
        this.guiManager = guiManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();
        
        switch (cmd) {
            case "roleplay":
            case "rp":
                return handleRoleplayCommand(sender, args);
            case "rpjob":
            case "job":
                return handleJobCommand(sender, args);
            case "rpeconomy":
            case "money":
            case "eco":
                return handleEconomyCommand(sender, args);
            case "rpnpc":
            case "npc":
                return handleNPCCommand(sender, args);
            default:
                sender.sendMessage("§c未知命令");
                return false;
        }
    }
    
    /**
     * 处理主命令 /roleplay
     */
    private boolean handleRoleplayCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家才能使用此命令！");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            // 打开主菜单
            guiManager.openMainMenu(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "help":
                sendHelp(player);
                break;
            case "reload":
                if (player.hasPermission("roleplay.admin")) {
                    plugin.reloadPluginConfig();
                    player.sendMessage("§a配置文件已重载！");
                } else {
                    player.sendMessage("§c你没有权限使用此命令！");
                }
                break;
            case "stats":
                guiManager.openStatsGUI(player);
                break;
            case "shop":
                guiManager.openShopGUI(player);
                break;
            case "skills":
                guiManager.openSkillGUI(player);
                break;
            case "quests":
                guiManager.openQuestGUI(player);
                break;
            default:
                player.sendMessage("§c未知子命令！使用 /rp help 查看帮助");
                break;
        }
        
        return true;
    }
    
    /**
     * 处理职业命令 /rpjob
     */
    private boolean handleJobCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家才能使用此命令！");
            return true;
        }
        
        Player player = (Player) sender;
        PlayerData data = dataManager.getPlayerData(player.getUniqueId());
        
        if (data == null) {
            player.sendMessage("§c数据加载失败！");
            return true;
        }
        
        if (args.length == 0) {
            // 打开职业选择GUI
            guiManager.openProfessionGUI(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "join":
                if (args.length < 2) {
                    player.sendMessage("§c用法: /job join <职业>");
                    player.sendMessage("§7可用职业: police, fire, medic, customs, chef, waiter, farmer");
                    return true;
                }
                
                ProfessionType profession = ProfessionType.fromString(args[1]);
                if (profession == ProfessionType.NONE) {
                    player.sendMessage("§c无效的职业！");
                    return true;
                }
                
                // 检查权限
                if (!player.hasPermission(profession.getPermission())) {
                    player.sendMessage("§c你没有权限选择这个职业！");
                    return true;
                }
                
                // 检查等级
                if (data.getProfessionLevel() < profession.getUnlockLevel()) {
                    player.sendMessage("§c需要等级 " + profession.getUnlockLevel() + " 才能选择此职业！");
                    return true;
                }
                
                // 检查冷却
                if (!data.canChangeProfession()) {
                    player.sendMessage("§c更换职业需要等待24小时冷却！");
                    return true;
                }
                
                // 更换职业
                data.setProfession(profession);
                data.resetProfessionChangeCooldown();
                player.sendMessage("§a成功选择职业: " + profession.getColoredName());
                break;
                
            case "leave":
                if (data.getProfession() == ProfessionType.NONE) {
                    player.sendMessage("§c你还没有职业！");
                    return true;
                }
                
                data.setProfession(ProfessionType.NONE);
                player.sendMessage("§a已离开当前职业");
                break;
                
            case "info":
                player.sendMessage("§6=== 职业信息 ===");
                player.sendMessage("§f当前职业: " + data.getProfession().getColoredName());
                player.sendMessage("§f职业等级: §e" + data.getProfessionLevel());
                player.sendMessage("§f职业经验: §e" + data.getProfessionExp() + " / " + data.getRequiredExpForNextLevel());
                player.sendMessage("§f基础工资: " + economyManager.formatMoney(data.getSalary()) + "/小时");
                player.sendMessage("§f下次工资: §e" + formatTimeRemaining(data.getLastSalaryTime()));
                break;
                
            case "duty":
                boolean newDutyState = !data.isOnDuty();
                data.setOnDuty(newDutyState);
                player.sendMessage(newDutyState ? "§a已开始值班" : "§c已结束值班");
                break;
                
            case "list":
                player.sendMessage("§6=== 所有职业 ===");
                for (ProfessionType type : ProfessionType.values()) {
                    if (type != ProfessionType.NONE) {
                        String status = data.getProfessionLevel() >= type.getUnlockLevel() ? "§a✔" : "§c✗";
                        player.sendMessage(status + " " + type.getColoredName() + 
                                          " §7- 需要等级: §e" + type.getUnlockLevel());
                    }
                }
                break;
                
            default:
                player.sendMessage("§c未知子命令！使用 /job help 查看帮助");
                break;
        }
        
        return true;
    }
    
    /**
     * 处理经济命令 /rpeconomy
     */
    private boolean handleEconomyCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§c用法: /money <balance|pay|top>");
                return true;
            }
            
            Player player = (Player) sender;
            PlayerData data = dataManager.getPlayerData(player.getUniqueId());
            if (data != null) {
                player.sendMessage("§6=== 经济信息 ===");
                player.sendMessage("§f余额: " + economyManager.formatMoney(data.getMoney()));
                player.sendMessage("§f技能点: §a" + data.getSkillPoints());
                player.sendMessage("§f当前工资: " + economyManager.formatMoney(data.getSalary()) + "/小时");
            }
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "balance":
            case "bal":
                if (args.length > 1) {
                    // 查看他人余额（需要权限）
                    if (!sender.hasPermission("roleplay.admin")) {
                        sender.sendMessage("§c你没有权限查看他人余额！");
                        return true;
                    }
                    
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        sender.sendMessage("§c玩家不在线或不存在！");
                        return true;
                    }
                    
                    PlayerData targetData = dataManager.getPlayerData(target.getUniqueId());
                    if (targetData != null) {
                        sender.sendMessage("§6" + target.getName() + " 的余额: " + 
                                          economyManager.formatMoney(targetData.getMoney()));
                    }
                } else {
                    // 查看自己余额
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("§c只有玩家才能使用此命令！");
                        return true;
                    }
                    
                    Player player = (Player) sender;
                    PlayerData data = dataManager.getPlayerData(player.getUniqueId());
                    if (data != null) {
                        player.sendMessage("§6你的余额: " + economyManager.formatMoney(data.getMoney()));
                    }
                }
                break;
                
            case "pay":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§c只有玩家才能使用此命令！");
                    return true;
                }
                
                if (args.length < 3) {
                    sender.sendMessage("§c用法: /money pay <玩家> <金额>");
                    return true;
                }
                
                Player player = (Player) sender;
                Player target = Bukkit.getPlayer(args[1]);
                
                if (target == null) {
                    player.sendMessage("§c玩家不在线或不存在！");
                    return true;
                }
                
                if (target == player) {
                    player.sendMessage("§c不能给自己转账！");
                    return true;
                }
                
                try {
                    double amount = Double.parseDouble(args[2]);
                    if (amount <= 0) {
                        player.sendMessage("§c金额必须大于0！");
                        return true;
                    }
                    
                    if (economyManager.transferMoney(player.getUniqueId(), target.getUniqueId(), amount)) {
                        player.sendMessage("§a成功转账给 " + target.getName());
                    } else {
                        player.sendMessage("§c转账失败！余额不足或出现错误");
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage("§c无效的金额！");
                }
                break;
                
            case "top":
                // 显示财富排行榜
                showMoneyTop(sender);
                break;
                
            case "give":
                if (!sender.hasPermission("roleplay.admin")) {
                    sender.sendMessage("§c你没有权限使用此命令！");
                    return true;
                }
                
                if (args.length < 3) {
                    sender.sendMessage("§c用法: /money give <玩家> <金额>");
                    return true;
                }
                
                Player targetPlayer = Bukkit.getPlayer(args[1]);
                if (targetPlayer == null) {
                    sender.sendMessage("§c玩家不在线或不存在！");
                    return true;
                }
                
                try {
                    double amount = Double.parseDouble(args[2]);
                    if (economyManager.addMoney(targetPlayer.getUniqueId(), amount)) {
                        sender.sendMessage("§a成功给予 " + targetPlayer.getName() + " " + 
                                          economyManager.formatMoney(amount));
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage("§c无效的金额！");
                }
                break;
                
            case "take":
                if (!sender.hasPermission("roleplay.admin")) {
                    sender.sendMessage("§c你没有权限使用此命令！");
                    return true;
                }
                
                if (args.length < 3) {
                    sender.sendMessage("§c用法: /money take <玩家> <金额>");
                    return true;
                }
                
                Player takeTarget = Bukkit.getPlayer(args[1]);
                if (takeTarget == null) {
                    sender.sendMessage("§c玩家不在线或不存在！");
                    return true;
                }
                
                try {
                    double amount = Double.parseDouble(args[2]);
                    if (economyManager.removeMoney(takeTarget.getUniqueId(), amount)) {
                        sender.sendMessage("§a成功从 " + takeTarget.getName() + " 扣除 " + 
                                          economyManager.formatMoney(amount));
                    } else {
                        sender.sendMessage("§c扣除失败！余额不足");
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage("§c无效的金额！");
                }
                break;
                
            default:
                sender.sendMessage("§c未知子命令！使用 /money help 查看帮助");
                break;
        }
        
        return true;
    }
    
    /**
     * 处理NPC命令 /rpnpc
     */
    private boolean handleNPCCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("roleplay.npc.manage")) {
            sender.sendMessage("§c你没有权限使用此命令！");
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage("§6=== NPC管理命令 ===");
            sender.sendMessage("§f/npc spawn <类型> - 生成NPC");
            sender.sendMessage("§f/npc remove <ID> - 移除NPC");
            sender.sendMessage("§f/npc list - 列出所有NPC");
            sender.sendMessage("§f/npc reload - 重载NPC配置");
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "spawn":
                sender.sendMessage("§eNPC生成功能开发中...");
                break;
                
            case "remove":
                sender.sendMessage("§eNPC移除功能开发中...");
                break;
                
            case "list":
                sender.sendMessage("§eNPC列表功能开发中...");
                break;
                
            case "reload":
                sender.sendMessage("§aNPC配置已重载！");
                break;
                
            default:
                sender.sendMessage("§c未知子命令！");
                break;
        }
        
        return true;
    }
    
    /**
     * 显示财富排行榜
     */
    private void showMoneyTop(CommandSender sender) {
        Map<UUID, PlayerData> allData = dataManager.getOnlinePlayersData();
        List<Map.Entry<UUID, PlayerData>> sorted = new ArrayList<>(allData.entrySet());
        
        sorted.sort((a, b) -> Double.compare(b.getValue().getMoney(), a.getValue().getMoney()));
        
        sender.sendMessage("§6=== 财富排行榜 ===");
        int limit = Math.min(10, sorted.size());
        for (int i = 0; i < limit; i++) {
            Map.Entry<UUID, PlayerData> entry = sorted.get(i);
            String name = entry.getValue().getPlayerName();
            double money = entry.getValue().getMoney();
            
            String rankColor;
            switch (i) {
                case 0: rankColor = "§6"; break; // 金色
                case 1: rankColor = "§7"; break; // 银色
                case 2: rankColor = "§c"; break; // 铜色
                default: rankColor = "§f"; break; // 白色
            }
            
            sender.sendMessage(rankColor + (i + 1) + ". §f" + name + " §7- " + 
                              economyManager.formatMoney(money));
        }
    }
    
    /**
     * 发送帮助信息
     */
    private void sendHelp(Player player) {
        player.sendMessage("§6=== 角色扮演职业系统帮助 ===");
        player.sendMessage("§f/rp - 打开主菜单");
        player.sendMessage("§f/rp stats - 查看个人信息");
        player.sendMessage("§f/rp shop - 打开职业商店");
        player.sendMessage("§f/rp skills - 打开技能界面");
        player.sendMessage("§f/rp quests - 查看任务列表");
        player.sendMessage("");
        player.sendMessage("§f/job - 打开职业选择");
        player.sendMessage("§f/job join <职业> - 加入职业");
        player.sendMessage("§f/job leave - 离开职业");
        player.sendMessage("§f/job info - 查看职业信息");
        player.sendMessage("§f/job duty - 切换值班状态");
        player.sendMessage("");
        player.sendMessage("§f/money - 查看余额");
        player.sendMessage("§f/money pay <玩家> <金额> - 转账");
        player.sendMessage("§f/money top - 查看财富榜");
    }
    
    /**
     * 格式化剩余时间
     */
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
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        String cmd = command.getName().toLowerCase();
        
        switch (cmd) {
            case "roleplay":
            case "rp":
                if (args.length == 1) {
                    completions.addAll(Arrays.asList("help", "stats", "shop", "skills", "quests", "reload"));
                }
                break;
                
            case "rpjob":
            case "job":
                if (args.length == 1) {
                    completions.addAll(Arrays.asList("join", "leave", "info", "duty", "list", "help"));
                } else if (args.length == 2 && args[0].equalsIgnoreCase("join")) {
                    for (ProfessionType type : ProfessionType.values()) {
                        if (type != ProfessionType.NONE) {
                            completions.add(type.name().toLowerCase());
                        }
                    }
                }
                break;
                
            case "rpeconomy":
            case "money":
            case "eco":
                if (args.length == 1) {
                    completions.addAll(Arrays.asList("balance", "pay", "top", "give", "take"));
                } else if (args.length == 2) {
                    if (args[0].equalsIgnoreCase("balance") || 
                        args[0].equalsIgnoreCase("pay") || 
                        args[0].equalsIgnoreCase("give") || 
                        args[0].equalsIgnoreCase("take")) {
                        // 玩家名补全
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            completions.add(player.getName());
                        }
                    }
                }
                break;
                
            case "rpnpc":
            case "npc":
                if (args.length == 1) {
                    completions.addAll(Arrays.asList("spawn", "remove", "list", "reload"));
                }
                break;
        }
        
        // 过滤匹配的补全
        if (args.length > 0) {
            String lastArg = args[args.length - 1].toLowerCase();
            completions.removeIf(s -> !s.toLowerCase().startsWith(lastArg));
        }
        
        return completions;
    }
}