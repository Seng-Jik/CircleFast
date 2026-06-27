# 开发日志 — CircleFast

## v1.0.0 — 2026-06-27

### 项目诞生

- 兴逸提出需求：为 OPPO Watch x3 开发 168 间歇性断食计时 App
- 核心要求：计时、通知提醒、圆屏适配、深色省电、无后台驻留
- 项目定名 **CircleFast**，GitHub 仓库 `Seng-Jik/CircleFast`
- AI 辅助：DeepSeek V4 Pro + Thinking 模式，总费用 ≈ ¥2

### 技术选型

- **Kotlin + Jetpack Compose**（标准版，非 Wear OS）
- **AlarmManager** 后台计时（无前台 Service，续航几乎无影响）
- **SharedPreferences** 本地存储
- **Target API 33 / Min API 26**
- **APK Debug 682KB / Release 876KB**

### 开发里程碑

| 时间 | 事件 |
|------|------|
| 19:13 | 需求确认，省电方案定稿（AlarmManager 自毁模式） |
| 19:17 | 项目定名 CircleFast，GitHub 仓库创建 |
| 19:20 | README + .gitignore + 首次 git push |
| 19:48 | 完整项目骨架搭建完成（subagent 后台开发） |
| 20:15 | 首次编译通过，APK 682KB |
| 20:55 | 断食科普功能（FastingFact）—— 分时段显示身体变化 |
| 20:56 | 进食窗口 8 小时倒计时 + 科普 |
| 20:57 | 图标设计（Vector Drawable 餐叉方案，后改为用户 AI 生图） |
| 21:05 | 通知逻辑重构：仅阶段切换发通知，新消息覆盖旧消息，无常驻 |
| 21:29 | 用户确认省电方案 A（设置不受限制） |
| 21:51 | 用户 AI 生成图标导入项目，READMA 更新 + Vim 署名 |
| 21:54 | 图标显示问题排查（mipmap-anydpi-v26 XML 冲突） |
| 22:01 | Release APK 构建 + ADB 安装到手表成功 |

### 核心设计决策

1. **省电优先**：AlarmManager 设置定时器后 Service 自毁，不常驻后台
2. **通知精简**：仅在断食开始/结束/进食结束时发单条通知，同 ID 覆盖
3. **圆屏适配**：Canvas 手动绘制圆环 + 刻度，不依赖 Wear OS 库
4. **断食科普**：不同时间段显示对应的身体状态知识（7 个断食里程碑 + 6 个进食里程碑）
5. **颜色方案**：断食橙红 #FF5722 / 进食翠绿 #4CAF50 / 纯黑背景

### 项目结构

```
CircleFast/
├── app/src/main/java/com/vim/fasting/
│   ├── MainActivity.kt              # 单入口
│   ├── data/
│   │   ├── FastingState.kt          # 状态模型
│   │   ├── FastingPreferences.kt    # 本地存储
│   │   ├── FastingTimer.kt          # AlarmManager 定时
│   │   └── FastingFact.kt          # 科普数据（13 个里程碑）
│   ├── notification/
│   │   └── NotificationHelper.kt    # 通知 + 振动
│   ├── receiver/
│   │   ├── FastingAlarmReceiver.kt  # 到点回调
│   │   └── BootReceiver.kt         # 重启恢复
│   └── ui/
│       ├── CircleFastScreen.kt      # 主界面
│       └── CircularCountdown.kt     # Canvas 圆环
├── assets/
│   └── icon.png                     # App 图标（用户 AI 生成）
├── README.md                        # 项目主页
├── USAGE.md                         # 使用说明书
└── CHANGELOG.md                     # 本文件
```

### 已知事项

- keystore 密码 `circlefast`（开发签名，非正式发布）
- 手表需在省电策略中设为「不受限制」以确保通知准时
- 如果 OPPO 未来锁 ADB，需走 OPPO 开发者通道申请签名
- 科普内容基于常见生理反应概括，非医学建议
