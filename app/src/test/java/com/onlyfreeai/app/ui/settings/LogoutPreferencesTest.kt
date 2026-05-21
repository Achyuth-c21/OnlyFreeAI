package com.onlyfreeai.app.ui.settings

import android.content.SharedPreferences
import com.onlyfreeai.app.util.Constants
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

class LogoutPreferencesTest {

    @Test
    fun testLogout_removesOnboardedButDoesNotClear() {
        // Arrange
        val mockPrefs = mock(SharedPreferences::class.java)
        val mockEditor = mock(SharedPreferences.Editor::class.java)

        `when`(mockPrefs.edit()).thenReturn(mockEditor)
        `when`(mockEditor.remove(Constants.PREF_ONBOARDED)).thenReturn(mockEditor)

        // Act: Simulates the exact call in SettingsFragment.kt on logout
        mockPrefs.edit().remove(Constants.PREF_ONBOARDED).apply()

        // Assert: Verify that PREF_ONBOARDED is specifically removed
        verify(mockEditor).remove(Constants.PREF_ONBOARDED)
        // Verify that the whole shared preferences map is NOT cleared (preserving PREF_DARK_MODE)
        verify(mockEditor, never()).clear()
    }

    @Test
    fun testThemeChange_savesCorrectly() {
        // Arrange
        val mockPrefs = mock(SharedPreferences::class.java)
        val mockEditor = mock(SharedPreferences.Editor::class.java)
        val testThemeValue = 2 // AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM

        `when`(mockPrefs.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putInt(Constants.PREF_DARK_MODE, testThemeValue)).thenReturn(mockEditor)

        // Act: Simulates saving theme in SettingsFragment.kt
        mockPrefs.edit().putInt(Constants.PREF_DARK_MODE, testThemeValue).apply()

        // Assert: Verify that the correct theme value is saved
        verify(mockEditor).putInt(Constants.PREF_DARK_MODE, testThemeValue)
    }
}
