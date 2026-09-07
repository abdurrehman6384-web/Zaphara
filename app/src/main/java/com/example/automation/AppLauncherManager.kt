package com.example.automation

import android.content.Context
import android.content.Intent
import android.util.Log

class AppLauncherManager(private val context: Context) {
    
    fun launchApp(appName: String): Boolean {
        val packageName = getPackageNameForApp(appName)
        if (packageName != null) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                Log.d("Zaiphra", "Launched $appName ($packageName)")
                return true
            }
        }
        Log.e("Zaiphra", "Could not find or launch app: $appName")
        return false
    }

    private fun getPackageNameForApp(appName: String): String? {
        val normalized = appName.lowercase()
        return when {
            normalized.contains("instagram") -> "com.instagram.android"
            normalized.contains("youtube") -> "com.google.android.youtube"
            normalized.contains("tiktok") -> "com.zhiliaoapp.musically"
            normalized.contains("free fire") -> "com.dts.freefireth"
            normalized.contains("minecraft") -> "com.mojang.minecraftpe"
            normalized.contains("pubg") -> "com.tencent.ig"
            normalized.contains("genshin") -> "com.miHoYo.GenshinImpact"
            else -> null
        }
    }
}
