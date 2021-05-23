package school.cactus.succulentshop.signUp.validation

import school.cactus.succulentshop.R
import school.cactus.succulentshop.validation.Validator

class MailValidator : Validator {
    override fun validate(field: String) = when {
        field.isEmpty() -> R.string.email_is_required
        //!field.contains(Regex("@")) -> R.string.email_is_invalid
        !(field.contains("@")) -> R.string.email_is_invalid
        field.length <= 5 -> R.string.email_is_invalid
        field.length >= 50 -> R.string.email_is_invalid
        else -> null
    }
}