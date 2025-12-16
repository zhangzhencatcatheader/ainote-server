
package top.zztech.ainote.error

import org.babyfish.jimmer.error.ErrorFamily


@ErrorFamily
enum class TemplateError {
    TEMPLATE_NOT_FOUND,
    INVALID_TEMPLATE_ID,
    TEMPLATE_NAME_EMPTY,
    TEMPLATE_ALREADY_EXISTS,
    TEMPLATE_DISABLED,
    TEMPLATE_CREATE_FAILED,
    TEMPLATE_UPDATE_FAILED,
    TEMPLATE_DELETE_FAILED
}