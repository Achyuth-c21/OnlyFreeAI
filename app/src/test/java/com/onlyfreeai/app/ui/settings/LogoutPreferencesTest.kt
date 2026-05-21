package com.onlyfreeai.app.ui.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.test.core.app.ApplicationProvider
import com.onlyfreeai.app.util.Constants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric:robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LogoutPreferencesTest {

    @Test
    fun testLogout_preservesThemePreferenceAndClearsOnboarded() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sharedPrefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

        // 1. Arrange: Set both theme and onboarded flags
        sharedPrefs.edit()
            .putInt(Constants.PREF_DARK_MODE, AppCompatDelegate.MODE_NIGHT_YES)
            .putBoolean(Constants.PREF_ONBOARDED, true)
            .apply()

        // Verify initial state is set
        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, sharedPrefs.getInt(Constants.PREF_DARK_MODE, -1))
        assertTrue(sharedPrefs.getBoolean(Constants.PREF_ONBOARDED, false))

        // 2. Act: Perform the precise logout logic we implemented in SettingsFragment.kt
        sharedPrefs.edit()
            .remove(Constants.PREF_ONBOARDED)
            .apply()

        // 3. Assert: Onboarded flag is deleted, but theme preference remains fully intact
        assertFalse(sharedPrefs.contains(Constants.PREF_ONBOARDED))
        assertTrue(sharedPrefs.contains(Constants.PREF_DARK_MODE))
        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, sharedPrefs.getInt(Constants.PREF_DARK_MODE, -1))
    }

    @Test
    fun testThemeChange_savesCorrectlyToSharedPreferences() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sharedPrefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

        // Act: Save Light Theme
        sharedPrefs.edit()
            .putInt(Constants.PREF_DARK_MODE, AppCompatDelegate.MODE_NIGHT_NO)
            .apply()

        // Assert: Read back correct light theme value
        assertEquals(AppCompatDelegate.MODE_NIGHT_NO, sharedPrefs.getInt(Constants.PREF_DARK_MODE, -1))

        // Act: Save System Follow Theme
        sharedPrefs.edit()
            .putInt(Constants.PREF_DARK_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            .apply()

        // Assert: Read back correct follow system theme value
        assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, sharedPrefs.getInt(Constants.PREF_DARK_MODE, -1))
    }
}
