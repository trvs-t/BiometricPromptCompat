package com.innopage.biometric

// Wrapper interface since Google provides 2 identical interface instance
interface BiometricAuthenticationCallback {
    fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?)
    fun onAuthenticationFailed()
    fun onAuthenticationError(errorCode: Int, errString: CharSequence?)
    fun onAuthenticationSucceeded(result: BiometricAuthenticationResult?)
}