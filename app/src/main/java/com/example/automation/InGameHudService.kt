package com.example.automation

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.skills.FreeFireStateBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InGameHudService : Service(), LifecycleOwner, SavedStateRegistryOwner {

    companion object {
        var isRunning = false
            private set

        fun start(context: Context) {
            val intent = Intent(context, InGameHudService::class.java)
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, InGameHudService::class.java)
            context.stopService(intent)
        }
    }

    private lateinit var windowManager: WindowManager
    private var floatingView: ComposeView? = null
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        setupFloatingHud()
        isRunning = true
    }

    private fun setupFloatingHud() {
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 50
            y = 200
        }

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@InGameHudService)
            setViewTreeSavedStateRegistryOwner(this@InGameHudService)
            setContent {
                var isExpanded by remember { mutableStateOf(false) }
                var isFabMenuExpanded by remember { mutableStateOf(false) }
                val sessionState by FreeFireStateBus.state.collectAsState()

                Box(
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                params.x += dragAmount.x.toInt()
                                params.y += dragAmount.y.toInt()
                                try {
                                    windowManager.updateViewLayout(floatingView, params)
                                } catch (ignored: Exception) {}
                            }
                        }
                ) {
                    if (!isExpanded) {
                        // FAB Menu Column
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AnimatedVisibility(visible = isFabMenuExpanded) {
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Emergency Stop
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Emergency Stop", color = Color.White, fontSize = 12.sp, modifier = Modifier.background(Color.Black.copy(alpha=0.6f), RoundedCornerShape(4.dp)).padding(6.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier.size(42.dp).clip(CircleShape).background(Color(0xFFDC2626)).clickable { 
                                                FreeFireStateBus.update { it.copy(isRunning = false, statusSummary = "EMERGENCY STOP") }
                                                isFabMenuExpanded = false 
                                            },
                                            contentAlignment = Alignment.Center
                                        ) { Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.White, modifier = Modifier.size(20.dp)) }
                                    }
                                    
                                    // Toggle Assist
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(if (sessionState.isRunning) "Pause Assist" else "Resume Assist", color = Color.White, fontSize = 12.sp, modifier = Modifier.background(Color.Black.copy(alpha=0.6f), RoundedCornerShape(4.dp)).padding(6.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier.size(42.dp).clip(CircleShape).background(Color(0xFF3B82F6)).clickable { 
                                                FreeFireStateBus.update { it.copy(isRunning = !it.isRunning, statusSummary = "Assist toggled") }
                                                isFabMenuExpanded = false 
                                            },
                                            contentAlignment = Alignment.Center
                                        ) { Icon(if (sessionState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = "Toggle", tint = Color.White, modifier = Modifier.size(20.dp)) }
                                    }

                                    // Full HUD
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Full HUD", color = Color.White, fontSize = 12.sp, modifier = Modifier.background(Color.Black.copy(alpha=0.6f), RoundedCornerShape(4.dp)).padding(6.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier.size(42.dp).clip(CircleShape).background(Color(0xFF10B981)).clickable { 
                                                isExpanded = true
                                                isFabMenuExpanded = false 
                                            },
                                            contentAlignment = Alignment.Center
                                        ) { Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.White, modifier = Modifier.size(20.dp)) }
                                    }
                                }
                            }

                            // Main FAB
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                if (sessionState.isRunning) Color(0xFF10B981) else Color(0xFF6366F1),
                                                Color(0xFF09090B)
                                            )
                                        )
                                    )
                                    .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                                    .clickable { isFabMenuExpanded = !isFabMenuExpanded },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isFabMenuExpanded) Icons.Default.Close else Icons.Default.Shield,
                                    contentDescription = "HUD",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    } else {
                        // Expanded Tactical HUD Card
                        Column(
                            modifier = Modifier
                                .width(220.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF111116).copy(alpha = 0.95f))
                                .border(1.dp, Color(0xFF818CF8).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (sessionState.isRunning) Color(0xFF10B981) else Color(0xFFE53935))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ZAIPHRA HUD",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Collapse",
                                    tint = Color.Gray,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { isExpanded = false }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Mode: ${sessionState.mode}",
                                color = Color(0xFFA5B4FC),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Threat: ${sessionState.threatLevel}% | APM: ${sessionState.actionsPerMinute}",
                                color = Color.LightGray,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Action: ${sessionState.lastAction}",
                                color = Color(0xFF34D399),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (sessionState.isRunning) Color(0xFFDC2626) else Color(0xFF10B981))
                                        .clickable {
                                            FreeFireStateBus.update {
                                                it.copy(
                                                    isRunning = !it.isRunning,
                                                    statusSummary = if (!it.isRunning) "Manual HUD Start" else "Manual HUD Stop"
                                                )
                                            }
                                        }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (sessionState.isRunning) "STOP" else "START",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF27272A))
                                        .clickable {
                                            FreeFireStateBus.update {
                                                it.copy(mode = "Clutch", statusSummary = "Clutch activated from HUD")
                                            }
                                        }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "CLUTCH",
                                        color = Color(0xFFFBBF24),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        floatingView = composeView
        try {
            windowManager.addView(floatingView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        floatingView?.let {
            try {
                windowManager.removeView(it)
            } catch (ignored: Exception) {}
        }
        floatingView = null
        isRunning = false
    }
}
