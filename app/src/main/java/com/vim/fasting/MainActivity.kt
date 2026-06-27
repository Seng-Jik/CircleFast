package com.vim.fasting

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.vim.fasting.data.*
import com.vim.fasting.notification.NotificationHelper
import com.vim.fasting.ui.CircleFastScreen

/**
 * Main activity — single-screen app for OPPO Watch.
 *
 * Shows a dark AMOLED-friendly circular countdown on a round display.
 * Tap the screen to start/stop fasting.
 */
class MainActivity : ComponentActivity() {

    private lateinit var preferences: FastingPreferences
    private lateinit var timer: FastingTimer
    private lateinit var notifier: NotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferences = FastingPreferences(this)
        timer = FastingTimer(this)
        notifier = NotificationHelper(this)

        // Request notification permission on Android 13+
        requestNotificationPermission()

        setContent {
            CircleFastApp(
                preferences = preferences,
                timer = timer,
                notifier = notifier
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Activity re-launched (e.g., via notification tap) — recompose handles state
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(this, "通知权限未授予，提醒功能将不可用", Toast.LENGTH_LONG).show()
        }
    }
}

/**
 * Root composable for CircleFast.
 */
@Composable
private fun CircleFastApp(
    preferences: FastingPreferences,
    timer: FastingTimer,
    notifier: NotificationHelper
) {
    var state by remember { mutableStateOf(preferences.loadState()) }

    val ctx = LocalContext.current

    // Reload state from preferences when activity is first composed
    LaunchedEffect(Unit) {
        state = preferences.loadState()
    }

    CircleFastScreen(
        state = state,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        when (state.phase) {
                            FastingPhase.IDLE -> {
                                val now = System.currentTimeMillis()
                                val newState = FastingState(FastingPhase.FASTING, now)
                                state = newState
                                preferences.saveState(newState)
                                timer.startFasting()
                                notifier.showPhaseChangeNotification(
                                    "🔄 断食开始",
                                    "16小时断食计时已启动。身体即将进入代谢切换模式。"
                                )
                            }

                            FastingPhase.FASTING, FastingPhase.EATING -> {
                                timer.cancelAll()
                                notifier.cancelNotification()
                                preferences.clearState()
                                state = FastingState(FastingPhase.IDLE, 0L)
                                Toast.makeText(ctx, "断食已手动结束", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }
    )
}
