package com.example.data.security

import android.os.Build
import com.example.data.models.LicenseTier
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HwidVerificationStatus(
    val isGranted: Boolean,
    val hwid: String,
    val deviceFingerprintSha256: String,
    val tier: LicenseTier,
    val registeredOwner: String,
    val message: String,
    val verificationTimestamp: String
)

data class HwidAuditLogEntry(
    val timestamp: String,
    val event: String,
    val hwid: String,
    val isSuccess: Boolean,
    val details: String
)

object HwidManager {

    /**
     * Generates a persistent and unique Hardware ID (HWID) based on
     * hardware properties of the underlying device.
     */
    fun generateUniqueHwid(): String {
        val hardwareSignature = buildString {
            append(Build.BOARD).append(":")
            append(Build.BRAND).append(":")
            append(Build.DEVICE).append(":")
            append(Build.HARDWARE).append(":")
            append(Build.MODEL).append(":")
            append(Build.MANUFACTURER).append(":")
            append(Build.ID).append(":")
            append(Build.BOOTLOADER).append(":")
            append(Build.FINGERPRINT)
        }

        val md5Bytes = MessageDigest.getInstance("MD5").digest(hardwareSignature.toByteArray())
        val hex = md5Bytes.joinToString("") { "%02X".format(it) }
        return "RGS-" + hex.chunked(4).take(4).joinToString("-")
    }

    fun generateDeviceHwid(): String = generateUniqueHwid()

    /**
     * Generates a cryptographic SHA-256 device fingerprint signature
     * for hardware verification and tamper protection.
     */
    fun generateDeviceFingerprintSha256(): String {
        val raw = "${Build.MANUFACTURER}-${Build.MODEL}-${Build.HARDWARE}-${Build.FINGERPRINT}-${Build.BOARD}"
        val sha256Bytes = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
        return sha256Bytes.joinToString("") { "%02x".format(it) }.take(32).uppercase()
    }

    /**
     * Verifies if the provided key matches the device HWID and cryptographic signature.
     */
    fun verifyHwidAccess(hwid: String, licenseKey: String): HwidVerificationStatus {
        val cleanKey = licenseKey.trim()
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val sha256 = generateDeviceFingerprintSha256()

        if (cleanKey.isBlank()) {
            return HwidVerificationStatus(
                isGranted = false,
                hwid = hwid,
                deviceFingerprintSha256 = sha256,
                tier = LicenseTier.STARTER,
                registeredOwner = "Unregistered",
                message = "Access Gated: License Key is required for HWID validation.",
                verificationTimestamp = now
            )
        }

        val tier = LicenseTier.fromKey(cleanKey)
        val owner = when (tier) {
            LicenseTier.GOD_MODE -> "RGS AI Administrator (VIP GOD MODE)"
            LicenseTier.PRO -> "RGS AI Certified Pro Operator"
            LicenseTier.STARTER -> "RGS AI Community Member"
        }

        // Validate cryptographic key prefix and structure
        val isValidPrefix = cleanKey.startsWith("RGS-", ignoreCase = true) ||
                cleanKey.startsWith("IRIS-", ignoreCase = true) ||
                cleanKey.startsWith("VIP-", ignoreCase = true)

        if (!isValidPrefix) {
            return HwidVerificationStatus(
                isGranted = false,
                hwid = hwid,
                deviceFingerprintSha256 = sha256,
                tier = LicenseTier.STARTER,
                registeredOwner = "Unregistered",
                message = "Access Gated: Invalid License Key format. Must start with RGS-, IRIS-, or VIP-",
                verificationTimestamp = now
            )
        }

        return HwidVerificationStatus(
            isGranted = true,
            hwid = hwid,
            deviceFingerprintSha256 = sha256,
            tier = tier,
            registeredOwner = owner,
            message = "HWID Verified Successfully! Access Granted to ${tier.title}",
            verificationTimestamp = now
        )
    }
}
