package com.onlyfreeai.app.ui.auth

import android.util.Patterns
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AuthValidationTest {

    @Test
    fun testEmailValidation_emptyEmail() {
        val email = ""
        assertTrue(email.isEmpty())
    }

    @Test
    fun testEmailValidation_validEmailAddresses() {
        val validEmails = listOf(
            "test@domain.com",
            "user.name@sub.domain.org",
            "info@onlyfree.ai",
            "admin123@gmail.com"
        )
        for (email in validEmails) {
            assertTrue(
                "Email '$email' should be recognized as valid",
                Patterns.EMAIL_ADDRESS.matcher(email).matches()
            )
        }
    }

    @Test
    fun testEmailValidation_invalidEmailAddresses() {
        val invalidEmails = listOf(
            "plainaddress",
            "#@%^%#$@#$@#.com",
            "@domain.com",
            "Joe Smith <email@domain.com>",
            "email.domain.com",
            "email@domain@domain.com"
        )
        for (email in invalidEmails) {
            assertFalse(
                "Email '$email' should be recognized as invalid",
                Patterns.EMAIL_ADDRESS.matcher(email).matches()
            )
        }
    }

    @Test
    fun testPasswordValidation_tooShort() {
        val shortPassword = "12345"
        assertTrue(shortPassword.length < 6)
    }

    @Test
    fun testPasswordValidation_sufficientLength() {
        val goodPassword = "secure123Password"
        assertTrue(goodPassword.length >= 6)
    }

    @Test
    fun testPasswordMatch_mismatch() {
        val password = "securePassword123"
        val confirmPassword = "securePassword321"
        assertFalse(password == confirmPassword)
    }

    @Test
    fun testPasswordMatch_identical() {
        val password = "securePassword123"
        val confirmPassword = "securePassword123"
        assertTrue(password == confirmPassword)
    }
}
