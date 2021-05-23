package school.cactus.succulentshop.signUp.validation

import school.cactus.succulentshop.R
import school.cactus.succulentshop.validation.Validator

class PasswordValidator : Validator {
    override fun validate(field: String) = when {
        field.isEmpty() -> R.string.password_is_required
        field.length <= 7 -> R.string.password_is_too_short
        field.length >= 40 -> R.string.password_is_too_long
        !field.contains(Regex("[0-9]")) -> R.string.password_must_contain_one_digit
        !field.contains(Regex("[a-z]")) -> R.string.password_must_contain_one_lowercase_letter
        !field.contains(Regex("[A-Z]")) -> R.string.password_must_contain_one_uppercase_letter
        !field.contains(Regex("[^a-zA-Z0-9]")) -> R.string.password_must_contain_at_least_one_special_character
        else -> null
    }
}