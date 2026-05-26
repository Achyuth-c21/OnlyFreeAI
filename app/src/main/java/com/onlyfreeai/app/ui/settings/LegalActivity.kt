package com.onlyfreeai.app.ui.settings

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import com.onlyfreeai.app.R

class LegalActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_LEGAL_TYPE = "legal_type"
        const val TYPE_PRIVACY = "privacy"
        const val TYPE_TERMS = "terms"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val type = intent.getStringExtra(EXTRA_LEGAL_TYPE) ?: TYPE_PRIVACY

        val scrollView = ScrollView(this).apply {
            setBackgroundColor(getColor(R.color.background_primary))
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(resources.getDimensionPixelSize(R.dimen.legal_padding))
        }

        // Back button
        val backButton = ImageView(this).apply {
            setImageResource(R.drawable.ic_back_arrow)
            setColorFilter(getColor(R.color.text_primary))
            val size = resources.getDimensionPixelSize(R.dimen.legal_back_size)
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                bottomMargin = resources.getDimensionPixelSize(R.dimen.legal_back_margin)
            }
            setOnClickListener { finish() }
            contentDescription = "Back"
        }
        container.addView(backButton)

        // Title
        val title = TextView(this).apply {
            text = if (type == TYPE_PRIVACY) getString(R.string.settings_privacy_policy) else getString(R.string.settings_terms)
            setTextColor(getColor(R.color.text_primary))
            textSize = 26f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            letterSpacing = -0.02f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 8
            }
        }
        container.addView(title)

        // Effective date
        val date = TextView(this).apply {
            text = "Effective Date: May 12, 2026"
            setTextColor(getColor(R.color.text_tertiary))
            textSize = 13f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 32
            }
        }
        container.addView(date)

        // Content
        val content = TextView(this).apply {
            text = Html.fromHtml(
                if (type == TYPE_PRIVACY) getPrivacyPolicyHtml() else getTermsHtml(),
                Html.FROM_HTML_MODE_COMPACT
            )
            setTextColor(getColor(R.color.text_secondary))
            textSize = 15f
            setLineSpacing(6f, 1.1f)
            movementMethod = LinkMovementMethod.getInstance()
        }
        container.addView(content)

        scrollView.addView(container)
        setContentView(scrollView)
    }

    private fun getPrivacyPolicyHtml(): String = """
        <h3>1. Introduction</h3>
        <p>Welcome to OnlyFreeAI. We respect your privacy and are committed to protecting your personal data. This Privacy Policy explains how we collect, use, and safeguard your information when you use our mobile application.</p>

        <h3>2. Information We Collect</h3>
        <p>OnlyFreeAI uses Firebase Authentication (Google Sign-In) to manage user accounts. When you log in, we collect:</p>
        <p>• <b>Profile Information:</b> Your name, email address, and profile picture associated with your Google account.</p>
        <p>• <b>Usage Data:</b> Information about how you use the app, including the tools you save to "My Stack" and the tools you submit to the directory.</p>
        <p>• <b>Device Information:</b> Basic device information and crash reports (via Firebase Crashlytics) to help us identify bugs and improve app stability.</p>

        <h3>3. How We Use Your Information</h3>
        <p>We use the collected information for the following purposes:</p>
        <p>• To create and manage your personal account.</p>
        <p>• To allow you to save your favorite tools to "My Stack" and sync them across your devices.</p>
        <p>• To process and review AI tools that you submit to the directory.</p>
        <p>• To analyze app usage and crashes to improve the user experience.</p>

        <h3>4. Third-Party Services</h3>
        <p>OnlyFreeAI uses third-party services that may collect information used to identify you:</p>
        <p>• Google Play Services</p>
        <p>• Firebase Authentication</p>
        <p>• Cloud Firestore</p>
        <p>• Firebase Crashlytics &amp; Analytics</p>
        <p>These services have their own privacy policies regarding how they handle your data.</p>

        <h3>5. Data Security</h3>
        <p>We value your trust in providing us your personal information, and we use commercially acceptable means of protecting it. Your data is securely stored using Google's Firebase infrastructure. However, please remember that no method of transmission over the internet or method of electronic storage is 100% secure.</p>

        <h3>6. User Rights &amp; Data Deletion</h3>
        <p>You have the right to access, update, or delete your personal information. If you wish to delete your account and all associated data (such as your saved tools), you can do so directly within the app's Settings menu or by contacting us at <b>nampallyachyuth111@gmail.com</b>.</p>

        <h3>7. Children's Privacy</h3>
        <p>OnlyFreeAI does not knowingly collect personal information from children under the age of 13. If we discover that a child under 13 has provided us with personal information, we will immediately delete it. If you are a parent or guardian and you are aware that your child has provided us with personal information, please contact us.</p>

        <h3>8. Changes to This Privacy Policy</h3>
        <p>We may update our Privacy Policy from time to time. We will notify you of any changes by posting the new Privacy Policy within the app. You are advised to review this page periodically for any changes.</p>

        <h3>9. Contact Us</h3>
        <p>If you have any questions or suggestions about our Privacy Policy, do not hesitate to contact us at <b>nampallyachyuth111@gmail.com</b>.</p>
    """.trimIndent()

    private fun getTermsHtml(): String = """
        <h3>1. Acceptance of Terms</h3>
        <p>By downloading, installing, or using the OnlyFreeAI application ("App"), you agree to be bound by these Terms of Service ("Terms"). If you do not agree to these Terms, please do not use the App.</p>

        <h3>2. Description of Service</h3>
        <p>OnlyFreeAI is a directory of verified free AI tools. The App allows users to browse, search, save, and submit AI tools. All tools listed on the platform have been verified to be genuinely free at the time of listing.</p>

        <h3>3. User Accounts</h3>
        <p>• You must sign in with a valid Google account to use the App.</p>
        <p>• You are responsible for maintaining the security of your account.</p>
        <p>• You must not share your account credentials with others.</p>
        <p>• You must provide accurate information when creating your account.</p>

        <h3>4. User Conduct</h3>
        <p>When using OnlyFreeAI, you agree NOT to:</p>
        <p>• Submit false, misleading, or spam tool listings.</p>
        <p>• Attempt to manipulate the tool directory or ratings.</p>
        <p>• Use the App for any unlawful or unauthorized purpose.</p>
        <p>• Interfere with or disrupt the integrity or performance of the App.</p>
        <p>• Attempt to gain unauthorized access to any portion of the App.</p>
        <p>• Use automated scripts to access or interact with the App.</p>

        <h3>5. Tool Submissions</h3>
        <p>• Users may submit AI tools for review. All submissions are subject to admin approval.</p>
        <p>• Submissions are limited to a maximum of 3 per user per day.</p>
        <p>• Submitted tools must be genuinely free — no trials, no credit card requirements, no hidden paywalls.</p>
        <p>• We reserve the right to approve, reject, or remove any submitted tool at our sole discretion.</p>
        <p>• By submitting a tool, you confirm that the information provided is accurate to the best of your knowledge.</p>

        <h3>6. Intellectual Property</h3>
        <p>• The OnlyFreeAI app, including its design, logo, and content, is owned by OnlyFreeAI.</p>
        <p>• Tool names, logos, and descriptions belong to their respective owners.</p>
        <p>• The "Verified Free" badge is a trademark of OnlyFreeAI.</p>

        <h3>7. Disclaimer of Warranties</h3>
        <p>• The App is provided "as is" without warranties of any kind.</p>
        <p>• We do not guarantee that listed tools will remain free indefinitely. Tools may change their pricing at any time.</p>
        <p>• We are not responsible for the content, functionality, or safety of third-party tools listed on the platform.</p>
        <p>• We make our best effort to verify free tier claims but cannot guarantee 100% accuracy at all times.</p>

        <h3>8. Limitation of Liability</h3>
        <p>OnlyFreeAI shall not be liable for any indirect, incidental, special, consequential, or punitive damages arising out of or relating to your use of the App or any tools discovered through the App.</p>

        <h3>9. Account Termination</h3>
        <p>We reserve the right to suspend or terminate your account if you violate these Terms or engage in behavior that we determine to be harmful to the App or its users.</p>

        <h3>10. Changes to Terms</h3>
        <p>We may modify these Terms at any time. Continued use of the App after changes constitutes your acceptance of the new Terms. We will notify you of significant changes through the App.</p>

        <h3>11. Governing Law</h3>
        <p>These Terms shall be governed by and construed in accordance with the laws of India, without regard to conflict of law principles.</p>

        <h3>12. Contact Us</h3>
        <p>If you have any questions about these Terms of Service, please contact us at <b>nampallyachyuth111@gmail.com</b>.</p>
    """.trimIndent()
}
