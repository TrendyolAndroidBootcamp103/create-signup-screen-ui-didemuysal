package school.cactus.succulentshop.signUp.validation

import school.cactus.succulentshop.R
import school.cactus.succulentshop.validation.Validator

class UsernameValidator : Validator {
    override fun validate(field: String) = when {
        field.isEmpty() -> R.string.username_is_required
        field.length <= 2 -> R.string.username_is_too_short
        field.length >= 20 -> R.string.username_is_too_long
        field.contains(Regex("[^a-z0-9_ ]")) -> R.string.username_character_settings
        else -> null
    }
}