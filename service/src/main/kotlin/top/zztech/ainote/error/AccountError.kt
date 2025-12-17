
package top.zztech.ainote.error

import org.babyfish.jimmer.error.ErrorFamily


@ErrorFamily
enum class AccountError {
    USERNAME_ALREADY_EXISTS,
    USERNAME_DOES_NOT_EXIST,
    UNAUTHORIZED,
    PASSWORD_IS_ERROR,
    CAPTCHA_IS_ERROR
}