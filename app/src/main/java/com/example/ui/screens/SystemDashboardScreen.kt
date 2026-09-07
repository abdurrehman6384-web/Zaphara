package com.example.ui.screens

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.io.File

@Composable
fun SystemDashboardScreen() {
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf("") }
    var commandLog by remember { mutableStateOf(listOf<String>()) }

    // System Status States
    val isAdminEnabled by remember { 
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        // Mutable state assuming this would be re-evaluated; for static demo we just read once
        mutableStateOf(false) // Replace with actual component check if AdminReceiver was implemented safely
    }
    
    val isAccessibilityEnabled by remember {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        mutableStateOf(enabledServices.isNotEmpty())
    }
    
    val isRooted by remember {
        mutableStateOf(checkRoot())
    }

    val speechRecognizer = remember {
        SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    isListening = false
                }
                override fun onError(error: Int) {
                    isListening = false
                    commandLog = commandLog + "Error: Speech recognition failed (Code: $error)"
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val command = matches[0]
                        recognizedText = command
                        commandLog = commandLog + "Command received: $command"
                        // Here you would normally pass the command to a safe command processor
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "SYSTEM DASHBOARD",
            color = RgsCyan,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Status Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusCard(
                title = "Device Admin",
                status = if (isAdminEnabled) "ACTIVE" else "INACTIVE",
                isActive = isAdminEnabled,
                modifier = Modifier.weight(1f)
            )
            StatusCard(
                title = "Accessibility",
                status = if (isAccessibilityEnabled) "ACTIVE" else "INACTIVE",
                isActive = isAccessibilityEnabled,
                modifier = Modifier.weight(1f)
            )
            StatusCard(
                title = "Root Access",
                status = if (isRooted) "GRANTED" else "DENIED",
                isActive = isRooted,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Permission Buttons
        Text(
            text = "PERMISSIONS & CONFIG",
            color = Color.Gray,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PermissionButton(
                text = "Admin",
                icon = Icons.Default.Security,
                onClick = { /* Launch Admin Intent */ }
            )
            PermissionButton(
                text = "Accessibility",
                icon = Icons.Default.Accessibility,
                onClick = { 
                    val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    context.startActivity(intent)
                }
            )
            PermissionButton(
                text = "Overlay",
                icon = Icons.Default.Layers,
                onClick = { /* Launch Overlay Intent */ }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Voice Command Interface
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E1E1E),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(if (isListening) Color(0xFFE53935) else Color(0xFF333333))
                        .border(
                            2.dp, 
                            if (isListening) Color(0xFFFFCDD2) else Color.Transparent, 
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            if (isListening) {
                                speechRecognizer.stopListening()
                                isListening = false
                            } else {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                                }
                                speechRecognizer.startListening(intent)
                                isListening = true
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Command",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = if (isListening) "Listening in Hindi/English..." else "Tap to Issue Command",
                    color = if (isListening) Color(0xFF00E676) else Color.Gray,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                
                if (recognizedText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "\"$recognizedText\"",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Command Log
        Text(
            text = "COMMAND LOG",
            color = Color.Gray,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1E1E1E),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (commandLog.isEmpty()) {
                    Text(
                        text = "> Waiting for commands...",
                        color = Color.DarkGray,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                } else {
                    commandLog.forEach { log ->
                        Text(
                            text = "> $log",
                            color = Color(0xFF00E676),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.StatusCard(title: String, status: String, isActive: Boolean, modifier: Modifier = Modifier) {
    val accentColor = if (isActive) Color(0xFF00E676) else Color(0xFFE53935)
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E1E1E),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = Color.Gray,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = status,
                color = accentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun RowScope.PermissionButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2A2A)),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        modifier = Modifier.weight(1f)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = text, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = text, color = Color.White, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

fun checkRoot(): Boolean {
    val paths = arrayOf(
        "/system/app/Superuser.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su",
        "/su/bin/su"
    )
    for (path in paths) {
        if (File(path).exists()) return true
    }
    return false
}
