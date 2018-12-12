package com.innopage.biometric

import android.app.Activity
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat.requestPermissions
import android.support.v4.content.ContextCompat
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.support.v4.os.CancellationSignal
import java.util.concurrent.Executor

@RequiresApi(api = Build.VERSION_CODES.M)
@SuppressWarnings("deprecation")
sealed class BiometricPromptCompat(protected val activity: Activity) {

    protected var keyGenerated = false

    fun authenticate(
            cancel: CancellationSignal,
            executor: Executor,
            callback: BiometricAuthenticationCallback
    ) {
        val permission = if (BiometricPreconditions.isBiometricPromptEnabled()) {
            android.Manifest.permission.USE_BIOMETRIC
        } else {
            android.Manifest.permission.USE_FINGERPRINT
        }
        if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
            if (BiometricPreconditions.isFingerprintAvailable(activity)) {
                showPrompt(cancel, executor, callback)
            } else {
                if (BiometricPreconditions.isBiometricPromptEnabled()) {
                    activity.startFingerprintEnrollment()
                } else {
                    activity.startSecuritySettings()
                }
            }
        } else {
            requestPermissions(activity, arrayOf(permission), REQUEST_CODE_BIOMETRIC_PERMISSION)
        }
    }

    protected abstract fun showPrompt(
            cancel: CancellationSignal,
            executor: Executor,
            callback: BiometricAuthenticationCallback
    )

    private class PiePrompt constructor(
            activity: Activity,
            private val biometricPrompt: BiometricPrompt
    ) : BiometricPromptCompat(activity) {
        override fun showPrompt(
                cancel: CancellationSignal,
                executor: Executor,
                callback: BiometricAuthenticationCallback
        ) {
            if (!keyGenerated) {
                BiometricCryptoGenerator.generateKey()
                keyGenerated = true
            }
            val nativeCancellationSignal = android.os.CancellationSignal()
            cancel.setOnCancelListener { nativeCancellationSignal.cancel() }
            val wrappedCallback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errMsgId: Int, errString: CharSequence?) {
                    callback.onAuthenticationError(errMsgId, errString)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                    callback.onAuthenticationSucceeded(
                            result?.cryptoObject?.let {
                                BiometricAuthenticationResult(BiometricAuthenticationResult.CryptoObject(
                                        it.cipher, it.mac, it.signature
                                ))
                            })
                }

                override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence?) {
                    callback.onAuthenticationHelp(helpMsgId, helpString)
                }

                override fun onAuthenticationFailed() {
                    callback.onAuthenticationFailed()
                }
            }
            BiometricCryptoGenerator.initCipher().let { cipher ->
                if (cipher == null) {
                    biometricPrompt.authenticate(nativeCancellationSignal, executor, wrappedCallback)
                } else {
                    val crypto = BiometricPrompt.CryptoObject(cipher)
                    biometricPrompt.authenticate(crypto, nativeCancellationSignal, executor, wrappedCallback)
                }
            }
        }
    }

    private class MarshmallowPrompt constructor(
            activity: Activity,
            private val biometricDialogCompat: BiometricDialogCompat
    ) : BiometricPromptCompat(activity) {
        override fun showPrompt(
                cancel: CancellationSignal,
                executor: Executor,
                callback: BiometricAuthenticationCallback
        ) {
            if (!keyGenerated) {
                BiometricCryptoGenerator.generateKey()
                keyGenerated = true
            }
            biometricDialogCompat.show()
            val crypto = BiometricCryptoGenerator.initCipher()?.let { FingerprintManagerCompat.CryptoObject(it) }
            FingerprintManagerCompat.from(activity)
                    .authenticate(crypto, 0, cancel,
                            object : FingerprintManagerCompat.AuthenticationCallback() {
                                override fun onAuthenticationError(errMsgId: Int, errString: CharSequence?) {
                                    biometricDialogCompat.dismiss()
                                    // TODO: show error?
                                    cancel.cancel()
                                    callback.onAuthenticationError(errMsgId, errString)
                                }

                                override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult?) {
                                    biometricDialogCompat.dismiss()
                                    callback.onAuthenticationSucceeded(
                                            result?.cryptoObject?.let {
                                                BiometricAuthenticationResult(BiometricAuthenticationResult.CryptoObject(
                                                        it.cipher, it.mac, it.signature
                                                ))
                                            })
                                }

                                override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence?) {
                                    helpString?.let { biometricDialogCompat.updateStatus(it.toString()) }
                                    callback.onAuthenticationHelp(helpMsgId, helpString)
                                }

                                override fun onAuthenticationFailed() {
                                    callback.onAuthenticationFailed()
                                }
                            }, null)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    class Builder(private val activity: Activity) {
        private var title: CharSequence? = null
        private var subtitle: CharSequence? = null
        private var description: CharSequence? = null
        private var negativeButtonArgs: ButtonArgs? = null
        private var negativeButtonText: CharSequence? = null

        // Title is required
        // or BiometricPrompt will throw exception
        // put it builder to allow deferred set
        fun setTitle(title: CharSequence): Builder {
            this.title = title
            return this
        }

        fun setSubtitle(subtitle: CharSequence): Builder {
            this.subtitle = subtitle
            return this
        }

        fun setDescription(description: CharSequence): Builder {
            this.description = description
            return this
        }

        fun setNegativeButton(text: CharSequence, executor: Executor, listener: DialogInterface.OnClickListener): Builder {
            this.negativeButtonArgs = ButtonArgs(text, executor, listener)
            return this
        }

        fun setNegativeButtonText(text: CharSequence): Builder {
            this.negativeButtonText = text
            return this
        }

        fun build(): BiometricPromptCompat {
            return if (BiometricPreconditions.isBiometricPromptEnabled()) {
                PiePrompt(activity, BiometricPrompt.Builder(activity).apply {
                    title?.let { setTitle(it) }
                    subtitle?.let { setSubtitle(it) }
                    description?.let { setDescription(it) }
                    negativeButtonArgs?.let {
                        setNegativeButton(it.text, it.executor, it.listener)
                    } ?: run {
                        negativeButtonText?.let { setNegativeButton(it, {}, { _, _ -> }) }
                    }
                }.build())
            } else {
                MarshmallowPrompt(activity,
                        BiometricDialogCompat(activity).apply {
                            title?.let { setTitle(it.toString()) }
                            subtitle?.let { setSubtitle(it.toString()) }
                            description?.let { setDescription(it.toString()) }
                            negativeButtonText?.let { setButtonText(it.toString()) }
                        })
            }
        }

        data class ButtonArgs(
                val text: CharSequence,
                val executor: Executor,
                val listener: DialogInterface.OnClickListener
        )
    }

    companion object {
        private const val REQUEST_CODE_BIOMETRIC_PERMISSION = 900
    }
}