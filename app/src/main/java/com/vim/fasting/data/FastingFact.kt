package com.vim.fasting.data

/**
 * Science-backed educational snippets about what happens to the body
 * at different stages of intermittent fasting (16:8).
 *
 * Two sets: fasting-phase facts and eating-phase facts.
 * Each entry maps to a time range (in minutes from phase start).
 */
enum class FastingFact(
    val startMinute: Int,
    val emoji: String,
    val title: String,
    val body: String,
    val phase: FactPhase
) {
    // ── Fasting window (0 → 16h) ──
    START(
        startMinute = 0, emoji = "🔄", title = "代谢切换",
        body = "身体开始消耗储存的糖原。约12小时后进入酮症状态。",
        phase = FactPhase.FASTING
    ),
    ONE_HOUR(
        startMinute = 60, emoji = "📉", title = "血糖平稳",
        body = "胰岛素水平下降，血糖趋于稳定。饥饿感通常会在此后减弱。",
        phase = FactPhase.FASTING
    ),
    TWO_HOURS(
        startMinute = 120, emoji = "🧠", title = "头脑清晰",
        body = "酮体开始轻度上升，大脑获得更稳定的能量来源，注意力可能提升。",
        phase = FactPhase.FASTING
    ),
    FOUR_HOURS(
        startMinute = 240, emoji = "🔥", title = "脂肪燃烧",
        body = "糖原基本耗尽，身体正式进入脂肪代谢模式。此时是燃脂高峰期。",
        phase = FactPhase.FASTING
    ),
    EIGHT_HOURS(
        startMinute = 480, emoji = "🛡️", title = "细胞自噬",
        body = "细胞自噬（Autophagy）开始启动。身体开始清除受损细胞和老化蛋白。",
        phase = FactPhase.FASTING
    ),
    TWELVE_HOURS(
        startMinute = 720, emoji = "💪", title = "深度酮症",
        body = "进入深度酮症状态。生长激素水平上升，有助于保护肌肉和促进修复。",
        phase = FactPhase.FASTING
    ),
    SIXTEEN_HOURS(
        startMinute = 960, emoji = "🎉", title = "断食完成！",
        body = "16小时断食窗口结束。细胞修复最大化，准备进入进食窗口。",
        phase = FactPhase.FASTING
    ),

    // ── Eating window (0 → 8h) ──
    EAT_START(
        startMinute = 0, emoji = "🍽️", title = "进食窗口开启",
        body = "身体急需营养补充。优先摄入蛋白质和健康脂肪。",
        phase = FactPhase.EATING
    ),
    EAT_ONE_HOUR(
        startMinute = 60, emoji = "⚡", title = "能量补充",
        body = "进食开始。碳水化合物转化为糖原，肝脏和肌肉开始填充能量储备。",
        phase = FactPhase.EATING
    ),
    EAT_TWO_HOURS(
        startMinute = 120, emoji = "🧬", title = "胰岛素回升",
        body = "胰岛素水平正常上升，促进营养吸收。自噬过程逐渐暂停。",
        phase = FactPhase.EATING
    ),
    EAT_FOUR_HOURS(
        startMinute = 240, emoji = "🔋", title = "营养吸收高峰",
        body = "蛋白质合成达到峰值。此时摄入的营养素利用率最高。",
        phase = FactPhase.EATING
    ),
    EAT_SIX_HOURS(
        startMinute = 360, emoji = "🔄", title = "代谢回落",
        body = "消化基本完成。身体回到基础代谢状态，准备下一轮断食。",
        phase = FactPhase.EATING
    ),
    EAT_EIGHT_HOURS(
        startMinute = 480, emoji = "⏰", title = "进食窗口结束",
        body = "8小时进食窗口即将关闭。下一轮24小时断食周期准备开始。",
        phase = FactPhase.EATING
    );

    companion object {
        /**
         * Returns the best matching fact for the given elapsed minutes
         * in the specified phase.
         */
        fun forElapsedMinutes(minutes: Long, phase: FactPhase = FactPhase.FASTING): FastingFact {
            return entries
                .filter { it.phase == phase }
                .lastOrNull { minutes >= it.startMinute }
                ?: entries.first { it.phase == phase && it.startMinute == 0 }
        }
    }
}

enum class FactPhase { FASTING, EATING }
