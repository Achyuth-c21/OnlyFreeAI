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
        .transform(com.bumptech.glide.load.resource.bitmap.RoundedCorners(radius))
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
        .replace(Regex("<script[^>]*>[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), "") // Strip scripts
        .replace(Regex("<style[^>]*>[\\s\\S]*?</style>", RegexOption.IGNORE_CASE), "")   // Strip styles
        .replace(Regex("on\\w+\\s*=\\s*\"[^\"]*\"", RegexOption.IGNORE_CASE), "")         // Strip event handlers
        .replace(Regex("on\\w+\\s*=\\s*'[^']*'", RegexOption.IGNORE_CASE), "")            // Strip single-quote handlers
        .replace(Regex("<[^>]*>"), "")                                                     // Strip all remaining HTML tags
        .replace(Regex("javascript:", RegexOption.IGNORE_CASE), "")                        // Strip javascript: URIs
        .replace(Regex("data:", RegexOption.IGNORE_CASE), "")                              // Strip data: URIs
        .trim()
        .take(Constants.MAX_DESCRIPTION_LENGTH)
}

/** SECURITY: Only HTTPS URLs are considered valid */
fun String.isValidUrl(): Boolean {
    return this.startsWith("https://")
}

// ─── Animation Extensions ───────────────────────────────────
fun View.fadeIn(duration: Long = 300) {
    this.show()
    this.alpha = 0f
    this.animate()
        .alpha(1f)
        .setDuration(duration)
        .setListener(null)
        .start()
}

fun View.fadeOut(duration: Long = 200, onEnd: (() -> Unit)? = null) {
    this.animate()
        .alpha(0f)
        .setDuration(duration)
        .setListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                this@fadeOut.hide()
                onEnd?.invoke()
            }
        })
        .start()
}

fun View.slideUp(duration: Long = 400) {
    this.show()
    this.translationY = 100f
    this.alpha = 0f
    this.animate()
        .translationY(0f)
        .alpha(1f)
        .setDuration(duration)
        .setInterpolator(android.view.animation.DecelerateInterpolator())
        .start()
}

fun View.slideDown(duration: Long = 300, onEnd: (() -> Unit)? = null) {
    this.animate()
        .translationY(100f)
        .alpha(0f)
        .setDuration(duration)
        .setInterpolator(android.view.animation.AccelerateInterpolator())
        .setListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                this@slideDown.hide()
                onEnd?.invoke()
            }
        })
        .start()
}

@android.annotation.SuppressLint("ClickableViewAccessibility")
fun View.scalePress() {
    this.setOnTouchListener { v, event ->
        when (event.action) {
            android.view.MotionEvent.ACTION_DOWN -> {
                v.animate()
                    .scaleX(0.97f)
                    .scaleY(0.97f)
                    .setDuration(100)
                    .start()
            }
            android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                v.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(150)
                    .setInterpolator(android.view.animation.OvershootInterpolator())
                    .start()
            }
        }
        false
    }
}

fun View.animateEntrance(delay: Long = 0) {
    this.alpha = 0f
    this.translationY = 80f
    this.animate()
        .alpha(1f)
        .translationY(0f)
        .setDuration(400)
        .setStartDelay(delay)
        .setInterpolator(android.view.animation.DecelerateInterpolator())
        .start()
}
