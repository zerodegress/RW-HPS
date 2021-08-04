package com.github.dr.rwserver.util.encryption

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec

/**
 * @author Dr
 */
object Aes {
    /**
     * AES加密
     * @param content 待加密的内容
     * @param encryptKey 加密密钥
     * @return 加密后的byte[]
     * @throws Exception Exception
     */
    @JvmStatic
    @Throws(Exception::class)
    fun aesEncryptToBytes(content: ByteArray, encryptKey: String): ByteArray {
        val keyGenerator = KeyGenerator.getInstance("AES")
        val random = SecureRandom.getInstance("SHA1PRNG")
        random.setSeed(encryptKey.toByteArray())
        keyGenerator.init(128, random)
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(keyGenerator.generateKey().encoded, "AES"))
        return cipher.doFinal(content)
    }

    /**
     * AES解密
     * @param encryptBytes 待解密的byte[]
     * @param decryptKey 解密密钥
     * @return 解密后的String
     * @throws Exception Exception
     */
    @JvmStatic
    @Throws(Exception::class)
    fun aesDecryptByBytes(encryptBytes: ByteArray, decryptKey: String): ByteArray {
        val keyGenerator = KeyGenerator.getInstance("AES")
        val random = SecureRandom.getInstance("SHA1PRNG")
        random.setSeed(decryptKey.toByteArray())
        keyGenerator.init(128, random)
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(keyGenerator.generateKey().encoded, "AES"))
        return cipher.doFinal(encryptBytes)
    }
}