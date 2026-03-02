# RolePlayProfession - 角色扮演职业系统

一个全面的Minecraft角色扮演职业系统插件，支持警察、消防、医护、海关、厨师、服务员、农民等多种职业，包含NPC路人增强、经济系统、任务系统和技能升级系统。

## 功能特性

### 🎭 职业系统
- **警察**: 维护治安，抓捕罪犯玩家
- **消防员**: 灭火救援，保护安全
- **医护人员**: 医疗急救，拯救生命
- **海关人员**: 检查物品，维护边境
- **厨师**: 烹饪美食，服务顾客
- **服务员**: 点餐送餐，服务周到
- **农民**: 种植收获，提供食材

### 👥 NPC路人增强
- NPC有日常行为（走路、交谈、工作、休息）
- NPC会报警/求救
- NPC有职业身份
- NPC会生病/受伤
- NPC会去餐厅/商店
- NPC有家庭关系
- NPC会犯罪/违法

### 💰 经济系统
- 独立的插件内货币系统
- 职业工资发放
- 每日登录奖励
- 任务奖励
- 商店系统（购买职业装备）
- 转账功能
- 财富排行榜

### 📊 技能系统
- 职业专属技能
- 技能升级系统
- 技能效果加成
- 技能点获取

### 🎯 任务系统
- 日常任务
- 紧急任务
- 职业专属任务
- 任务奖励

### 🎮 玩家互动
- 警察抓捕罪犯玩家
- 医护救援受伤玩家/NPC
- 消防灭火救人
- 海关检查物品
- 厨师服务玩家/NPC
- 服务员点餐送餐
- 农民种植收获

## 安装方法

1. 将插件放入 `plugins/ScriptIrc/scripts/src/` 目录
2. 重启服务器或执行 `/scriptirc compiler RolePlayProfession`
3. 插件将自动编译并启用

## 命令列表

### 主命令
- `/rp` 或 `/roleplay` - 打开主菜单
- `/rp help` - 查看帮助
- `/rp stats` - 查看个人信息
- `/rp shop` - 打开职业商店
- `/rp skills` - 打开技能界面
- `/rp quests` - 查看任务列表
- `/rp reload` - 重载配置（需要权限）

### 职业命令
- `/job` 或 `/rpjob` - 打开职业选择
- `/job join <职业>` - 加入职业
- `/job leave` - 离开职业
- `/job info` - 查看职业信息
- `/job duty` - 切换值班状态
- `/job list` - 列出所有职业

### 经济命令
- `/money` 或 `/rpeconomy` - 查看余额
- `/money pay <玩家> <金额>` - 转账
- `/money top` - 查看财富榜
- `/money give <玩家> <金额>` - 给予金钱（管理员）
- `/money take <玩家> <金额>` - 扣除金钱（管理员）

### NPC命令
- `/npc` 或 `/rpnpc` - NPC管理（需要权限）
- `/npc spawn <类型>` - 生成NPC
- `/npc remove <ID>` - 移除NPC
- `/npc list` - 列出所有NPC
- `/npc reload` - 重载NPC配置

## 权限节点

- `roleplay.use` - 使用角色扮演系统
- `roleplay.admin` - 管理员权限
- `roleplay.police` - 警察职业权限
- `roleplay.fire` - 消防员职业权限
- `roleplay.medic` - 医护人员权限
- `roleplay.customs` - 海关人员权限
- `roleplay.chef` - 厨师职业权限
- `roleplay.waiter` - 服务员职业权限
- `roleplay.farmer` - 农民职业权限
- `roleplay.npc.manage` - 管理NPC权限

## 配置文件

插件配置文件位于 `plugins/RolePlayProfession/config.yml`，包含以下主要配置：

### 经济系统
```yaml
economy:
  currency-name: "职业币"  # 货币名称
  currency-symbol: "§6Ⓟ§f"  # 货币符号
  start-money: 1000  # 初始资金
  daily-login-bonus: 100  # 每日登录奖励
  salary-interval: 60  # 工资发放间隔（分钟）
```

### 职业系统
```yaml
professions:
  unlock-levels:  # 职业解锁等级
    police: 5
    firefighter: 3
    medic: 4
    customs: 6
    chef: 2
    waiter: 1
    farmer: 1
  
  base-salary:  # 基础工资（每小时）
    police: 500
    firefighter: 450
    medic: 480
    customs: 520
    chef: 300
    waiter: 250
    farmer: 280
```

### NPC系统
```yaml
npc:
  spawn-density: 0.1  # NPC生成密度（每区块）
  max-npcs: 100  # NPC最大数量
  activity-range: 16  # NPC活动范围（区块）
  
  behavior-probability:  # NPC行为概率
    walk: 40
    talk: 20
    work: 30
    rest: 10
```

## 游戏玩法

### 开始游戏
1. 加入服务器后，使用 `/rp` 打开主菜单
2. 选择适合你的职业
3. 开始执行职业任务赚取金钱
4. 升级技能提升工作效率
5. 购买职业装备增强能力

### 职业互动
- **警察**: 攻击罪犯玩家进行抓捕
- **消防员**: 靠近火焰自动灭火
- **医护人员**: 攻击受伤玩家进行治疗
- **厨师**: 烹饪食物获得奖励
- **农民**: 收获作物获得奖励
- **服务员**: 服务NPC获得奖励

### 经济系统
- 每小时自动发放工资
- 完成任务获得额外奖励
- 每日登录获得奖励
- 可以在商店购买职业装备
- 支持玩家间转账

## 开发计划

### 已完成功能
- [x] 基础职业系统
- [x] 经济系统
- [x] 技能系统
- [x] GUI界面
- [x] 玩家数据存储
- [x] 基础玩家互动

### 计划功能
- [ ] NPC系统实现
- [ ] 任务系统完善
- [ ] 更多职业互动
- [ ] 职业专属建筑
- [ ] 团队系统
- [ ] 成就系统

## 技术细节

### 数据存储
- 玩家数据使用YAML文件存储
- 支持自动保存
- 数据备份机制

### 性能优化
- 异步数据保存
- 事件监听优化
- 内存管理

### 兼容性
- 支持 Minecraft 1.20.x
- 基于 Bukkit/Spigot API
- 无外部依赖

## 问题反馈

如果在使用过程中遇到任何问题，请：
1. 检查控制台错误日志
2. 确认配置文件是否正确
3. 检查权限设置
4. 联系插件开发者

## 更新日志

### v1.0.0
- 初始版本发布
- 基础职业系统
- 经济系统
- 技能系统
- GUI界面
- 玩家数据管理

## 版权信息

本插件由 ScriptIrc Engine 生成，遵循 MIT 开源协议。

## 支持与捐赠

如果你喜欢这个插件，请考虑支持开发者：
- 报告bug和改进建议
- 分享给其他玩家
- 提供功能建议