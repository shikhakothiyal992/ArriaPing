package com.arria.ping.util

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import com.arria.ping.util.IpConstants.ANDROID_KEYSTORE
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

object CryptoManagerUtil {

    private val KEY_SIZE = 256

    @RequiresApi(Build.VERSION_CODES.M)
    private val ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES

    @RequiresApi(Build.VERSION_CODES.M)
    private val ENCRYPTION_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM

    @RequiresApi(Build.VERSION_CODES.M)
    private val ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE

    @RequiresApi(Build.VERSION_CODES.M)
    fun getInitializedCipherForEncryption(keyName: String): Cipher {
        val cipher = getCipher()
        val secretKey = getOrCreateSecretKey(keyName)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return cipher
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getOrCreateSecretKey(keyName: String): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        keyStore.getKey(keyName, null)
                ?.let {return it as SecretKey}

        val paramsBuilder = KeyGenParameterSpec.Builder(
                keyName,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
        paramsBuilder.apply {
            setBlockModes(ENCRYPTION_BLOCK_MODE)
            setEncryptionPaddings(ENCRYPTION_PADDING)
            setKeySize(KEY_SIZE)
            setUserAuthenticationRequired(true)
        }

        val keyGenParams = paramsBuilder.build()
        val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
        )
        keyGenerator.init(keyGenParams)
        return keyGenerator.generateKey()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getCipher(): Cipher {
        val transformation = "$ENCRYPTION_ALGORITHM/$ENCRYPTION_BLOCK_MODE/$ENCRYPTION_PADDING"
        return Cipher.getInstance(transformation)
    }

}