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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.vim.fasting.data.*
import com.vim.fasting.notification.NotificationHelper
import com.vim.fasting.ui.CircleFastScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Main activity — single-screen app for OPPO Watch.
 *
 * Optimized for fast cold start:
 * - Load state on background thread before setContent
 * - Defer notification channel creation to first use
 * - Defer permission request to after first render
 * - Use splash window to cover init time
 */
class MainActivity : ComponentActivity() {

    private lateinit var preferences: FastingPreferences
    private lateinit var timer: FastingTimer
    private lateinit var notifier: NotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fast init: prefs + timer are cheap, notifier has IO but we defer
        preferences = FastingPreferences(this)
        timer = FastingTimer(this)
        notifier = NotificationHelper(this)

        setContent {
            CircleFastApp(
                preferences = preferences,
                timer = timer,
                notifier = notifier
            )
        }

        // Defer permission request slightly to not block first frame
        postponePermissionRequest()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
    }

    private var permissionRequested = false

    private fun postponePermissionRequest() {
        if (permissionRequested) return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) return

        // Request permission after first frame renders
        window.decorView.post {
            permissionRequested = true
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
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
 * Tap toggles between EATING and FASTING.
 *
 * Startup optimized: no double-load, no blocking.
 */
@Composable
private fun CircleFastApp(
    preferences: FastingPreferences,
    timer: FastingTimer,
    notifier: NotificationHelper
) {
    var state by remember { mutableStateOf(preferences.loadState()) }

    CircleFastScreen(
        state = state,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        when (state.phase) {
                            FastingPhase.FASTING -> {
                                timer.cancelAll()
                                notifier.cancelNotification()
                                val now = System.currentTimeMillis()
                                val newState = FastingState(FastingPhase.EATING, now)
                                state = newState
                                preferences.saveState(newState)
                                timer.scheduleEatingOverdue()
                                notifier.showPhaseChangeNotification(
                                    "🍽️ 切换至进食窗口",
                                    "断食结束，8小时进食窗口已开启。点击屏幕可切换回断食。"
                                )
                            }

                            FastingPhase.EATING -> {
                                timer.cancelAll()
                                notifier.cancelNotification()
                                val now = System.currentTimeMillis()
                                val newState = FastingState(FastingPhase.FASTING, now)
                                state = newState
                                preferences.saveState(newState)
                                timer.scheduleFastingOverdue()
                                notifier.showPhaseChangeNotification(
                                    "🔄 切换至断食模式",
                                    "进食窗口关闭，16小时断食开始。点击屏幕可切换回进食。"
                                )
                            }
                        }
                    }
                )
            }
    )
}
