package com.arria.ping.util

import android.util.Base64
import android.util.Log
import java.security.Key
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec

object EncryptionAndDecryptionUtil {

    private val SALT_BYTES = 8
    private val PBK_ITERATIONS = 1000
    private val ENCRYPTION_ALGORITHM = "AES/CBC/PKCS5Padding"
    private val PBE_ALGORITHM = "PBEwithSHA256and128BITAES-CBC-BC"


    fun encrypt(text: String) : String?{
        val encryptedData = passwordEncryption(text)
        var data: String? = null
        if (encryptedData != null) {
            data = byteArrayToString(encryptedData.encryptedData)
            StorePrefData.encryptedPassword = Base64.encodeToString(encryptedData.encryptedData, Base64.DEFAULT)
            StorePrefData.encryptedIV = Base64.encodeToString(encryptedData.iv, Base64.DEFAULT)
            StorePrefData.encryptedSalt = Base64.encodeToString(encryptedData.salt, Base64.DEFAULT)
        }
        return data
    }

    fun decrypt(text: String): String {
        return decryptionOfEncryptedPassword(stringToByteArray(text))
    }

    private fun passwordEncryption(password: String): EncryptedData? {
        try {
            val data = password.toByteArray(charset("UTF-8"))

            val encData = EncryptedData()
            val rnd = SecureRandom()
            encData.salt = ByteArray(SALT_BYTES)
            encData.iv = ByteArray(16)
            rnd.nextBytes(encData.salt)
            rnd.nextBytes(encData.iv)
            val keySpec = PBEKeySpec(IpConstants.KEY_NAME.toCharArray(), encData.salt, PBK_ITERATIONS)
            val secretKeyFactory: SecretKeyFactory = SecretKeyFactory.getInstance(PBE_ALGORITHM)
            val key: Key = secretKeyFactory.generateSecret(keySpec)
            val cipher: Cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM)
            val ivSpec = IvParameterSpec(encData.iv)
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)
            encData.encryptedData = cipher.doFinal(data)
            return encData
        } catch (e: Exception) {
            Log.e("Exception", "failed to encrypt ${e.printStackTrace()}")
            return null
        }

    }

    private fun decryptionOfEncryptedPassword(
            encryptedData: ByteArray
    ): String {
        var decryptedText: ByteArray? = null
        try {
            val salt = stringToByteArray(StorePrefData.encryptedSalt)
            val iv = stringToByteArray(StorePrefData.encryptedIV)
            val keySpec = PBEKeySpec(IpConstants.KEY_NAME.toCharArray(), salt, PBK_ITERATIONS)
            val secretKeyFactory: SecretKeyFactory = SecretKeyFactory.getInstance(PBE_ALGORITHM)
            val key: Key = secretKeyFactory.generateSecret(keySpec)
            val cipher: Cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM)
            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
            decryptedText = cipher.doFinal(encryptedData)
        } catch (e: Exception) {
            Log.e("Exception", "failed to encrypt ${e.printStackTrace()}")
        }
        return String(decryptedText!!)
    }

    private fun stringToByteArray(text: String): ByteArray {
        return Base64.decode(text, Base64.DEFAULT)
    }

    private fun byteArrayToString(byteArray: ByteArray): String {
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

}

private class EncryptedData {
    lateinit var salt: ByteArray
    lateinit var iv: ByteArray
    lateinit var encryptedData: ByteArray
}