package com.whatever.caramel.domain.content.model.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.jasypt.encryption.StringEncryptor
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Converter
@Component
class JpaStringEncryptConverter(
    @Qualifier("jasyptStringEncryptor")
    private val stringEncryptor: StringEncryptor,
) : AttributeConverter<String?, String?> {

    override fun convertToDatabaseColumn(attribute: String?): String? {
        return attribute?.let { stringEncryptor.encrypt(it) }
    }

    override fun convertToEntityAttribute(dbData: String?): String? {
        return dbData?.let { stringEncryptor.decrypt(it) }
    }
}

