package com.reelia.app.ui.auth

/** Hard floor enforced at sign-up — short of this, [onSubmit][LoginViewModel] refuses to create
 * the account. Firebase's own minimum is 6; 8 matches common baseline password guidance. */
const val MIN_SIGNUP_PASSWORD_LENGTH = 8

enum class PasswordStrength { TOO_SHORT, WEAK, MEDIUM, STRONG }

/** Length below [MIN_SIGNUP_PASSWORD_LENGTH] is always [PasswordStrength.TOO_SHORT] regardless of
 * character variety — everything at or above that floor is scored on variety (digits, casing,
 * symbols) and extra length, purely as feedback rather than a second hard gate. */
fun evaluatePasswordStrength(password: String): PasswordStrength {
    if (password.length < MIN_SIGNUP_PASSWORD_LENGTH) return PasswordStrength.TOO_SHORT
    var score = 0
    if (password.length >= 12) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { it.isUpperCase() } && password.any { it.isLowerCase() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++
    return when (score) {
        0 -> PasswordStrength.WEAK
        1, 2 -> PasswordStrength.MEDIUM
        else -> PasswordStrength.STRONG
    }
}
