# CircleFast 🎯

168 间歇性断食计时 App — 专为 OPPO Watch x3（ColorOS Watch）设计。

圆形 AMOLED 表盘，一屏搞定断食计时。

## 功能

- ⭕ 圆形 Canvas 倒计时圆环，适配圆屏
- 🔔 16h 断食结束振动+通知提醒
- 🟢 进食窗口 8h 结束提醒
- 🌑 纯黑深色主题，AMOLED 省电
- ⚡ 冷启动 < 500ms，APK < 2MB

## 技术栈

- Kotlin + Jetpack Compose
- 原生 Android APK（非 Wear OS）
- ADB 侧载安装

## 开发环境

- Android Studio Ladybug+
- JDK 17+
- Gradle 8.x + Kotlin DSL

## 安装

```bash
# 手表开启 ADB 调试
adb connect <手表IP>
adb install app-debug.apk
```

## 许可证

MIT
