package properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "firebase")
data class FirebaseProperties(
    val credentialFilePath: String,
    val fcmEnabled: Boolean,
)
