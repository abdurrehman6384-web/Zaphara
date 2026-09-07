package com.example

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rgs.companion.chat.ChatScreen
import com.rgs.companion.chat.ChatViewModel
import com.rgs.companion.overlay.FloatingCompanionService
import com.rgs.companion.ui.screens.HomeScreen
import com.rgs.companion.ui.theme.CompanionTheme

/**
 * Main activity hosting the Cyber Obsidian procedural companion interface.
 */
class MainActivity : ComponentActivity() {

    private val chatViewModel: ChatViewModel by viewModels()

    private val overlayPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (FloatingCompanionService.canDrawOverlays(this)) {
                startOverlayService()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestPermissionsIfNeeded()

        FloatingCompanionService.onAvatarTap = {
            val bringToFrontIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(bringToFrontIntent)
        }

        setContent {
            CompanionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val state by chatViewModel.state.collectAsStateWithLifecycle()
                    var showChat by remember { mutableStateOf(false) }

                    if (showChat) {
                        ChatScreen(
                            viewModel = chatViewModel,
                            companionName = chatViewModel.personaName,
                            onBack = { showChat = false },
                        )
                    } else {
                        HomeScreen(
                            state = state,
                            companionName = chatViewModel.personaName,
                            speech = chatViewModel.speech,
                            memoryStore = chatViewModel.memoryStore,
                            onAvatarTap = { chatViewModel.pokeAvatar() },
                            onOpenChat = { showChat = true },
                            onOpenAccessibility = { openAccessibilitySettings() },
                            onToggleOverlay = { enableFloatingCompanion() },
                        )
                    }
                }
            }
        }
    }

    private fun enableFloatingCompanion() {
        if (!FloatingCompanionService.canDrawOverlays(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                overlayPermissionLauncher.launch(intent)
            }
        } else {
            startOverlayService()
        }
    }

    private fun startOverlayService() {
        val intent = Intent(this, FloatingCompanionService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    private fun requestPermissionsIfNeeded() {
        val permissions = mutableListOf<String>()
        permissions.add(android.Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        requestPermissions(permissions.toTypedArray(), 101)
    }
}
