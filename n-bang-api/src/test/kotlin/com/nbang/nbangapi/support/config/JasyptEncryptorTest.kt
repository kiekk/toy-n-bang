package com.nbang.nbangapi.support.config

import org.jasypt.encryption.pbe.PooledPBEStringEncryptor
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class JasyptEncryptorTest {

    private fun createEncryptor(password: String): PooledPBEStringEncryptor {
        val encryptor = PooledPBEStringEncryptor()
        val config = SimpleStringPBEConfig()

        config.password = password
        config.algorithm = "PBEWITHHMACSHA512ANDAES_256"
        config.setKeyObtentionIterations("1000")
        config.setPoolSize("1")
        config.providerName = "SunJCE"
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator")
        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator")
        config.stringOutputType = "base64"

        encryptor.setConfig(config)
        return encryptor
    }

    @Test
    @Disabled("암호화가 필요할 때만 활성화")
    fun encrypt() {
        val password = ""
        val encryptor = createEncryptor(password)

        val plainTexts = listOf(
            "암호화할값",
        )

        println("=== Jasypt Encryption Results ===")
        println("Password: $password")
        println()

        plainTexts.forEach { plainText ->
            val encrypted = encryptor.encrypt(plainText)
            println("Plain: $plainText")
            println("Encrypted: ENC($encrypted)")
            println()
        }
    }

    @Test
    @Disabled("복호화 테스트용")
    fun decrypt() {
        val password = ""
        val encryptor = createEncryptor(password)

        val encryptedText = "암호화된값"
        val decrypted = encryptor.decrypt(encryptedText)

        println("Encrypted: $encryptedText")
        println("Decrypted: $decrypted")
    }
}
