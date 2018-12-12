package com.innopage.biometric

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.support.annotation.RequiresApi

@RequiresApi(api = Build.VERSION_CODES.P)
fun Activity.startFingerprintEnrollment() {
    startActivityForResult(
            Intent(Settings.ACTION_FINGERPRINT_ENROLL),
            REQUEST_CODE_FINGERPRINT
    )
}

fun Activity.startSecuritySettings() {
    startActivityForResult(
            Intent(Settings.ACTION_SECURITY_SETTINGS),
            REQUEST_CODE_SECURITY
    )
}

const val REQUEST_CODE_FINGERPRINT = 901
const val REQUEST_CODE_SECURITY = 902