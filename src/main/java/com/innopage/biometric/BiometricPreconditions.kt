package com.innopage.biometric

import android.content.Context
import android.os.Build
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat

object BiometricPreconditions {
    fun isBiometricPromptEnabled(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
    }

    fun isSdkVersionSupported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    fun isHardwareSupported(context: Context): Boolean {
        val fingerprintManager = FingerprintManagerCompat.from(context)
        return fingerprintManager.isHardwareDetected
    }

    fun isFingerprintAvailable(context: Context): Boolean {
        val fingerprintManager = FingerprintManagerCompat.from(context)
        return fingerprintManager.hasEnrolledFingerprints()
    }
}