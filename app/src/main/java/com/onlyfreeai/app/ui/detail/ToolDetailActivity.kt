package com.onlyfreeai.app.ui.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.onlyfreeai.app.databinding.ActivityToolDetailBinding
import com.onlyfreeai.app.util.*

class ToolDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityToolDetailBinding
    private lateinit var viewModel: ToolDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityToolDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ToolDetailViewModel::class.java]

        val toolId = intent.getStringExtra(Constants.EXTRA_TOOL_ID) ?: run {
            finish()
            return
        }

        setupToolbar()
        observeData()
        viewModel.loadTool(toolId)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun observeData() {
        viewModel.tool.observe(this) { tool ->
            if (tool == null) return@observe

            binding.apply {
                imgToolLogo.loadUrl(tool.logoUrl)
                tvToolName.text = tool.name
                tvToolDescription.text = tool.description
                tvCategory.text = tool.category

                if (tool.isVerified) badgeVerified.show() else badgeVerified.hide()

                // What's Free
                tvWhatsFree.text = tool.whatsFree.joinToString("\n") { "✓ $it" }

                // What's NOT Free
                tvWhatsNotFree.text = tool.whatsNotFree.joinToString("\n") { "✗ $it" }

                // Best For
                tvBestFor.text = tool.bestFor.joinToString(" • ")

                // Date Added
                tool.dateAdded?.let { tvDateAdded.text = "Added ${it.toFormattedDate()}" }

                // Save count
                tvSaves.text = "${tool.saves} saves"

                // Visit Tool button
                btnVisitTool.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tool.websiteUrl))
                    startActivity(intent)
                }

                // Save / Unsave
                btnSaveTool.setOnClickListener {
                    viewModel.toggleSave(tool.id)
                }

                // Flag as Gone Paid
                btnGonePaid.setOnClickListener {
                    viewModel.flagAsPaid(tool.id)
                }
            }
        }

        viewModel.isSaved.observe(this) { isSaved ->
            binding.btnSaveTool.text = if (isSaved) "★ Saved" else "☆ Save to Stack"
        }

        viewModel.error.observe(this) { errorMsg ->
            if (errorMsg.isNotBlank()) {
                toast(errorMsg)
            }
        }

        viewModel.message.observe(this) { msg ->
            if (msg.isNotBlank()) {
                toast(msg)
            }
        }
    }
}
