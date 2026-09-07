package com.example.engine

sealed class Intent {
    data class LaunchApp(val appName: String) : Intent()
    data class SocialMediaScroll(val platform: String, val durationMinutes: Int = -1) : Intent()
    data class GameMasterMode(val game: String, val mode: String, val customStrategy: String? = null) : Intent()
    object StopAction : Intent()
    object YouTubeControl : Intent()
    object DeviceInfo : Intent()
    object Chat : Intent()
    object Unknown : Intent()
}

class CommandRouter {
    fun routeCommand(intentString: String): Intent {
        val lower = intentString.lowercase()
        return when {
            lower.contains("stop") || lower.contains("ruk jao") || lower.contains("bas") || lower.contains("pause") -> Intent.StopAction
            
            lower.contains("instagram reels scroll") || lower.contains("scroll") -> {
                val platform = if (lower.contains("youtube")) "YouTube Shorts" 
                               else if (lower.contains("tiktok")) "TikTok" 
                               else "Instagram"
                Intent.SocialMediaScroll(platform)
            }
            
            lower.contains("free fire") || lower.contains("khelo") || lower.contains("minecraft") || lower.contains("ranked") || lower.contains("hotdrop") -> {
                val game = if (lower.contains("minecraft")) "Minecraft" else "Free Fire"
                val mode = when {
                    lower.contains("clutch") -> "Clutch"
                    lower.contains("ranked push") -> "Ranked Push"
                    lower.contains("safe ranked") -> "Safe Ranked"
                    lower.contains("hotdrop") || lower.contains("aggressive") -> "Aggressive Hotdrop"
                    lower.contains("adaptive") -> "Adaptive"
                    lower.contains("farming") -> "Farming"
                    else -> "Full Power"
                }
                
                val customStrategy = if (lower.contains("custom strategy:")) {
                    lower.substringAfter("custom strategy:").trim()
                } else null
                
                Intent.GameMasterMode(game, mode, customStrategy)
            }
            
            lower.contains("launch") || lower.contains("open") || lower.contains("start") -> {
                // Dummy extraction for demo, real implementation uses Gemini parsing
                Intent.LaunchApp("TargetApp")
            }
            
            lower.contains("youtube") -> Intent.YouTubeControl
            lower.contains("device") -> Intent.DeviceInfo
            else -> Intent.Chat
        }
    }
}
