
package top.zztech.ainote.error

import org.babyfish.jimmer.error.ErrorFamily


@ErrorFamily
enum class AccountError {
    USERNAME_ALREADY_EXISTS,
    USERNAME_DOES_NOT_EXIST,
    PHONE_DOES_NOT_EXIST,
    UNAUTHORIZED,
    PASSWORD_IS_ERROR,
    CAPTCHA_IS_ERROR,
    SMS_SEND_TOO_FREQUENT,
    SMS_CODE_EXPIRED,
    SMS_CODE_IS_ERROR,
    USER_IS_THIS_COMPANY_ADMIN
}