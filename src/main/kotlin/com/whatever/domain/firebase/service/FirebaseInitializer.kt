package com.whatever.domain.firebase.service

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.whatever.config.properties.FirebaseProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

private val logger = KotlinLogging.logger { }

@Component
class FirebaseInitializer(
    private val firebaseProperties: FirebaseProperties,
) {

    @PostConstruct
    private fun initFirebaseApp() {
        if (firebaseProperties.credentialFilePath.isBlank()) {
            logger.error { "Firebase credential file path is not configured in properties." }
            throw IllegalStateException("Firebase credential file path is not configured")
        }

        val options = getFirebaseOptions(firebaseProperties.credentialFilePath)

        if (FirebaseApp.getApps().isEmpty()) {
            runCatching {
                FirebaseApp.initializeApp(options)
            }.onFailure { e ->
                logger.error(e) { "FirebaseApp.initializeApp(options) failed" }
            }.getOrThrow()

            logger.info { "Firebase app initialized successfully" }
        } else {
            logger.info { "Firebase app already initialized" }
        }

        if (!firebaseProperties.fcmEnabled) {
            logger.warn { "[FCM IS DISABLED] Skipping notification in this application." }
        }
    }

    private fun getFirebaseOptions(credentialFilePath: String): FirebaseOptions {
        return runCatching {
            File(credentialFilePath).inputStream().use { inputStream ->
                FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(inputStream))
                    .build()
            }
        }.onFailure { e ->
            val errorMessage = when (e) {
                is FileNotFoundException -> "Credential file not found at path: '$credentialFilePath'."
                is IOException -> "I/O error reading credential file at path: '$credentialFilePath'."
                else -> "Failed to build FirebaseOptions from '$credentialFilePath' due to an unexpected error."
            }
            logger.error(e) { errorMessage }

        }.getOrThrow()
    }
}
