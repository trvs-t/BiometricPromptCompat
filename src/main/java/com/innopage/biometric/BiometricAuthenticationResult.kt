package com.innopage.biometric

import java.security.Signature
import javax.crypto.Cipher
import javax.crypto.Mac

data class BiometricAuthenticationResult(
        val cryptoObject: CryptoObject
) {
    data class CryptoObject(
            val cipher: Cipher?,
            val mac: Mac?,
            val signature: Signature?
    )
}
