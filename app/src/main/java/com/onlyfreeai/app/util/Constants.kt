package com.onlyfreeai.app.util

object Constants {

    // Categories for AI tools
    val CATEGORIES = listOf(
        "All",
        "Writing",
        "Image Generation",
        "Video",
        "Audio",
        "Code",
        "Chatbot",
        "Design",
        "Productivity",
        "Research",
        "Education",
        "Marketing",
        "Data Analysis",
        "Translation",
        "Other"
    )

    // Firestore field names
    const val FIELD_STATUS = "status"
    const val FIELD_DATE_ADDED = "dateAdded"
    const val FIELD_CATEGORY = "category"
    const val FIELD_SAVES = "saves"

    // Character limits
    const val MAX_TOOL_NAME_LENGTH = 50
    const val MAX_DESCRIPTION_LENGTH = 300
    const val MAX_FREE_ITEM_LENGTH = 150

    // Rate limiting
    const val MAX_SUBMISSIONS_PER_DAY = 3

    // Intent extras
    const val EXTRA_TOOL_ID = "extra_tool_id"

    // Shared Preferences
    const val PREFS_NAME = "onlyfreeai_prefs"
    const val PREF_DARK_MODE = "dark_mode"
    const val PREF_ONBOARDED = "onboarded"
}
