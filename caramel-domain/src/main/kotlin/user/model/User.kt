package user.model

import com.whatever.domain.base.BaseEntity
import com.whatever.domain.couple.model.Couple
import com.whatever.domain.user.exception.UserExceptionCode.INVALID_BIRTH_DATE
import com.whatever.domain.user.exception.UserExceptionCode.INVALID_USER_STATUS_FOR_COUPLING
import com.whatever.domain.user.exception.UserIllegalArgumentException
import com.whatever.domain.user.exception.UserIllegalStateException
import user.model.UserStatus.COUPLED
import user.model.UserStatus.SINGLE
import com.whatever.global.exception.ErrorUi
import com.whatever.util.DateTimeUtil
import com.whatever.util.DateTimeUtil.KST_ZONE_ID
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Converter
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.validator.constraints.CodePointLength
import org.springframework.security.crypto.encrypt.TextEncryptor
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.ZoneId

private val logger = KotlinLogging.logger { }

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(
            name = "user_unique_idx_platform_user_id_when_not_deleted",
            columnNames = ["platform_user_id"]
        )
    ],
)
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Convert(converter = UserEmailConverter::class)
    var email: String? = null,

    var birthDate: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val platform: LoginPlatform,

    @Column(nullable = false)
    val platformUserId: String,

    @Column(length = 8)
    @field:CodePointLength(min = MIN_NICKNAME_LENGTH, max = MAX_NICKNAME_LENGTH)
    var nickname: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(length = MAX_GENDER_LENGTH)
    var gender: UserGender? = null,

    @Enumerated(EnumType.STRING)
    @Column(length = MAX_STATUS_LENGTH, nullable = false)
    var userStatus: UserStatus = UserStatus.NEW,

//    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
//    val contents: MutableList<Content> = mutableListOf(),
) : BaseEntity() {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_id", referencedColumnName = "id")
    protected var _couple: Couple? = null
    val couple: Couple? get() = _couple

    fun setCouple(couple: Couple) {
        if (userStatus != SINGLE) {
            logger.error { "Current user status is '${userStatus}'. To be coupled, the user status must be '${SINGLE}'." }
            throw UserIllegalStateException(
                errorCode = INVALID_USER_STATUS_FOR_COUPLING,
                errorUi = ErrorUi.Toast("커플이 될 수 없는 사용자가 있어요."),
            )
        }
        _couple = couple
        updateUserStatus(COUPLED)
    }

    fun leaveFromCouple() {
        this._couple = null
        updateUserStatus(SINGLE)
    }

    fun updateUserStatus(newStatus: UserStatus) {
        userStatus = newStatus
    }

    fun updateBirthDate(
        newBirthDate: LocalDate,
        userZoneId: ZoneId = KST_ZONE_ID,
    ) {
        val todayInUserZone = DateTimeUtil.zonedNow(userZoneId).toLocalDate()
        if (newBirthDate.isAfter(todayInUserZone)) {
            throw UserIllegalArgumentException(
                errorCode = INVALID_BIRTH_DATE,
                errorUi = ErrorUi.Toast("미래의 날짜를 생일로 설정할 수 없어요.")
            )
        }

        birthDate = newBirthDate
    }

    fun register(
        nickname: String,
        birthday: LocalDate,
        gender: UserGender,
        userTimeZone: ZoneId = KST_ZONE_ID,
    ) {
        this.nickname = nickname
        updateBirthDate(birthday, userTimeZone)
        this.gender = gender
        this.userStatus = SINGLE
    }

    companion object {
        const val MAX_GENDER_LENGTH = 50
        const val MAX_STATUS_LENGTH = 50
        const val MIN_NICKNAME_LENGTH = 1
        const val MAX_NICKNAME_LENGTH = 8
    }
}

@Component
@Converter
class UserEmailConverter(
    private val textEncryptor: TextEncryptor,
) : AttributeConverter<String?, String?> {
    override fun convertToDatabaseColumn(email: String?): String? {
        return email?.let { textEncryptor.encrypt(email) }
    }

    override fun convertToEntityAttribute(encryptedEmail: String?): String? {
        return encryptedEmail?.let { textEncryptor.decrypt(encryptedEmail) }
    }
}
