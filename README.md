# BlueDream-Lottery 🎁

![Java](https://img.shields.io/badge/Java-8%2B-orange)
![Minecraft](https://img.shields.io/badge/Minecraft-1.7.10%2B-green)
![License](https://img.shields.io/badge/License-MIT-blue)

BlueDream-Lottery 是一款功能强大、视觉效果震撼的 Minecraft 服务器抽奖插件。内置 14+ 种炫酷动画，支持全 GUI 操作，让你的服务器抽奖体验提升到一个新的高度！

## ✨ 核心特性

- **🎬 视觉盛宴**：内置 14+ 种不同风格的抽奖动画（如：老虎机、维度裂隙、雷霆一击、时空穿梭、3D悬浮展示等），支持为每个奖池独立配置。
- **💎 抽奖模式**：支持 **左键单抽** 与 **右键十连抽**，自动计算消耗，体验如丝般顺滑。
- **⚖️ 保底系统**：支持设置“超级大奖”和保底次数。当玩家运气不佳时，保底机制将确保奖励按时降临。
- **🔗 指令奖励**：奖池物品不仅可以发放道具，还支持配置多条 **后台指令**（如发放金币、点券、权限组等），支持 PlaceholderAPI 变量。
- **💰 多种支付**：完美兼容 Vault (金币)、PlayerPoints (点券) 以及自定义钥匙物品。
- **🌍 物理入口**：支持 **物理抽奖方块**！可将地图上任意方块设为抽奖入口，支持全方位防破坏与防爆炸保护。
- **🛠️ 全 GUI 编辑**：无需频繁修改配置文件！游戏内即可通过界面完成所有奖池设置、概率调整和动画切换。
- **🚀 极速性能**：采用玩家独立数据文件 + 异步快照保存机制，彻底告别保存数据时的主线程卡顿。
- **🛡️ 贴心防护**：背包满了？别担心！会自动将奖品掉落在玩家脚下并发出温馨提示。
- **📦 便携宝箱**：管理员可发放特定奖池的“宝箱”物品，玩家右键即可直接开启抽奖。
- **🎵 沉浸音效**：所有抽奖动画均有音效反馈，让每一次抽奖都充满仪式感。
- **🌟 炫酷粒子**：新增 5 种炫酷环绕粒子特效（涡流、光环、星尘等），支持管理员 GUI 一键切换并自动保存。
- **🌍 国际化支持**：支持 zh_cn / en_us 语言动态切换，自定义消息提示。
- **🚀 全版本支持**：通过底层代码重构，实现了从 1.7.10 到 1.21.x 的适配。

## 📝 命令与权限

| 命令 | 描述 | 权限 |
| --- | --- | --- |
| `/lt help` | 查看完整帮助 | - |
| `/lt create <奖池名>` | 创建新奖池 | `bluedream.lottery.admin` |
| `/lt remove <奖池名>` | 删除指定奖池 | `bluedream.lottery.admin` |
| `/lt rename <旧名> <新名>` | 重命名奖池 | `bluedream.lottery.admin` |
| `/lt edit <奖池名>` | 打开图形化编辑器 | `bluedream.lottery.admin` |
| `/lt setblock <奖池名>` | 将准星方块设置为奖池入口 | `bluedream.lottery.admin` |
| `/lt removeblock` | 移除准星方块的奖池设置 | `bluedream.lottery.admin` |
| `/lt play <奖池名>` | 打开抽奖界面 | `bluedream.lottery.use` |
| `/lt give <奖池> <数量> [玩家]` | 给予抽奖箱 | `bluedream.lottery.admin` |
| `/lt reload` | 重载配置文件 | `bluedream.lottery.admin` |
| `/lt lang <zh_cn|en_us>` | 切换语言 | `bluedream.lottery.admin` |

## 📦 安装与依赖

1. 下载 `BlueDream-Lottery.jar` 并放入 `plugins` 文件夹。
2. 重启服务器。
3. （可选）安装以下前置插件以获得完整体验：
   - **Vault** (金币支持)
   - **PlayerPoints** (点券支持)
   - **PlaceholderAPI** (变量支持)

## 🏗️ 构建项目

本项目使用 Maven 构建。

```bash
git clone https://github.com/ciqian666/BlueDream-Lottery.git
cd BlueDream-Lottery
mvn clean package
```

构建完成后，插件 jar 包位于 `target` 目录下。

## 📄 开源协议

本项目采用 MIT 协议开源。
