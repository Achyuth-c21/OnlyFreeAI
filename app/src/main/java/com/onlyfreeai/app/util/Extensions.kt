package com.onlyfreeai.app.util

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

// ─── View Extensions ────────────────────────────────────────
fun View.show() { visibility = View.VISIBLE }
fun View.hide() { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }

fun View.toggleVisibility() {
    visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
}

// ─── Context Extensions ─────────────────────────────────────
fun Context.toast(message: String, long: Boolean = false) {
    Toast.makeText(this, message, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
}

// ─── ImageView Extensions ───────────────────────────────────
fun ImageView.loadUrl(url: String?) {
    if (url.isNullOrBlank()) return
    Glide.with(this.context)
        .load(url)
        .transition(DrawableTransitionOptions.withCrossFade(300))
        .centerCrop()
        .into(this)
}

fun ImageView.loadUrlRounded(url: String?, radius: Int = 16) {
    if (url.isNullOrBlank()) return
    Glide.with(this.context)
        .load(url)
        .transition(DrawableTransitionOptions.withCrossFade(300))
        .circleCrop()
        .into(this)
}

// ─── Timestamp Extensions ───────────────────────────────────
fun Timestamp.toFormattedDate(): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    return sdf.format(this.toDate())
}

fun Timestamp.toRelativeTime(): String {
    val now = System.currentTimeMillis()
    val diff = now - this.toDate().time
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 30 -> toFormattedDate()
        days > 0 -> "${days}d ago"
        hours > 0 -> "${hours}h ago"
        minutes > 0 -> "${minutes}m ago"
        else -> "Just now"
    }
}

// ─── String Extensions ──────────────────────────────────────
fun String.sanitize(): String {
    return this
        .replace(Regex("<[^>]*>"), "") // Strip HTML tags
        .replace(Regex("<script[^>]*>[\\s\\S]*?</script>"), "") // Strip scripts
        .trim()
        .take(Constants.MAX_DESCRIPTION_LENGTH)
}

fun String.isValidUrl(): Boolean {
    return this.startsWith("http://") || this.startsWith("https://")
}
