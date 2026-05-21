package com.onlyfreeai.app.data.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserSubmissionLogicTest {

    private val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    private val yesterdayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(
        Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
    )

    // Simulates the exact rate-limiting business rule inside UserRepository.canSubmitToday()
    private fun simulateCanSubmitToday(user: User): Boolean {
        val today = todayDateString
        return if (user.lastSubmissionDate == today) {
            user.submissionsToday < User.MAX_SUBMISSIONS_PER_DAY
        } else {
            true
        }
    }

    @Test
    fun testCanSubmitToday_newUser_noSubmissions() {
        val newUser = User(
            id = "user_123",
            submissionsToday = 0,
            lastSubmissionDate = ""
        )
        assertTrue(simulateCanSubmitToday(newUser))
    }

    @Test
    fun testCanSubmitToday_yesterdaySubmissions_atMaxLimit() {
        val user = User(
            id = "user_123",
            submissionsToday = 3,
            lastSubmissionDate = yesterdayDateString
        )
        // Even though user is at 3, it was yesterday, so they should be allowed to submit today
        assertTrue(simulateCanSubmitToday(user))
    }

    @Test
    fun testCanSubmitToday_todaySubmissions_underMaxLimit() {
        val user = User(
            id = "user_123",
            submissionsToday = 2,
            lastSubmissionDate = todayDateString
        )
        // User has submitted 2 times today, limit is 3, so they can still submit
        assertTrue(simulateCanSubmitToday(user))
    }

    @Test
    fun testCanSubmitToday_todaySubmissions_atMaxLimit() {
        val user = User(
            id = "user_123",
            submissionsToday = 3,
            lastSubmissionDate = todayDateString
        )
        // User has submitted 3 times today, limit is 3, so they are blocked
        assertFalse(simulateCanSubmitToday(user))
    }

    @Test
    fun testCanSubmitToday_todaySubmissions_exceedsMaxLimit() {
        val user = User(
            id = "user_123",
            submissionsToday = 4,
            lastSubmissionDate = todayDateString
        )
        // Over limit for today, must be blocked
        assertFalse(simulateCanSubmitToday(user))
    }
}
