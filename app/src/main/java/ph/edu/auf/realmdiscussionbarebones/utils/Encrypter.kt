package ph.edu.auf.realmdiscussionbarebones.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.random.Random

object Encrypter {
    private const val KEY_ALIAS = "REALM_KEY"

    private fun generateKey(): SecretKey{
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        val keygen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")

        val keySpecs = KeyGenParameterSpec.Builder(
            KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
        keygen.init(keySpecs)
        return keygen.generateKey()
    }

    private fun encrypt(seed: ByteArray){
        val secretKey = generateKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val eData = cipher.doFinal(seed)

        //TODO: SAVE TO SHARED PREF

        val iv = cipher.iv

        val ivData = Base64.encodeToString(iv, Base64.NO_PADDING)
        val sData = Base64.encodeToString(eData, Base64.NO_PADDING)

        SharedPrefManager.putString("REALMKEY", sData)
        SharedPrefManager.putString("REALMKEYIV", ivData)

    }
    private fun getKey(): KeyStore.SecretKeyEntry{
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        return keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry

    }

    fun getEncryptedData(): ByteArray{
        val eData = SharedPrefManager.getString("REALMKEY", "")
        val ivData = SharedPrefManager.getString("REALMKEYIV", "")

        return if(eData.isEmpty()){
            val nKey = getRandomKey()
            encrypt(nKey)
            nKey
        }
        else{
            val secretKey = getKey()

            val iv = Base64.decode(ivData, Base64.NO_PADDING)
            val algoParamSpec : AlgorithmParameterSpec = GCMParameterSpec(128, iv)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey.secretKey, algoParamSpec)

            val sData = Base64.decode(eData, Base64.NO_PADDING)
            cipher.doFinal(sData)

        }
    }

    fun getRandomKey(seed: Long? = null): ByteArray {
        // generate a new 64-byte encryption key
        val key = ByteArray(64)
        if (seed != null) {
            // If there is a seed provided, create a random number with that seed
            // and fill the byte array with random bytes
            Random(seed).nextBytes(key)
        } else {
            // fill the byte array with random bytes
            Random.nextBytes(key)
        }
        return key
    }

}

