package com.onlyfreeai.app.ui.admin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.onlyfreeai.app.databinding.ActivityAdminBinding
import com.onlyfreeai.app.util.hide
import com.onlyfreeai.app.util.show
import com.onlyfreeai.app.util.toast

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var viewModel: AdminViewModel
    private lateinit var submissionAdapter: SubmissionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[AdminViewModel::class.java]

        setupToolbar()
        setupTabs()
        setupRecyclerView()
        observeData()

        verifyAdminAccess()
    }

    private fun verifyAdminAccess() {
        lifecycleScope.launch {
            try {
                val isAdmin = com.onlyfreeai.app.data.repository.UserRepository().isAdmin()
                if (!isAdmin) {
                    toast("Unauthorized access.")
                    finish()
                } else {
                    viewModel.loadPendingSubmissions()
                }
            } catch (e: Exception) {
                toast("Error verifying access.")
                finish()
            }
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> viewModel.loadPendingSubmissions()
                    1 -> viewModel.loadApprovedSubmissions()
                    2 -> viewModel.loadRejectedSubmissions()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerView() {
        submissionAdapter = SubmissionAdapter(
            onApprove = { submission ->
                viewModel.approveSubmission(submission)
                toast("${submission.name} approved!")
            },
            onReject = { submission ->
                showRejectDialog(submission.id, submission.name)
            }
        )

        binding.recyclerSubmissions.apply {
            layoutManager = LinearLayoutManager(this@AdminActivity)
            adapter = submissionAdapter
            setHasFixedSize(true)
        }
    }

    private fun showRejectDialog(submissionId: String, toolName: String) {
        val input = android.widget.EditText(this).apply {
            hint = "Reason for rejection..."
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Reject $toolName?")
            .setView(input)
            .setPositiveButton("Reject") { _, _ ->
                val reason = input.text.toString().ifBlank { "Does not meet criteria" }
                viewModel.rejectSubmission(submissionId, reason)
                toast("$toolName rejected")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeData() {
        viewModel.submissions.observe(this) { submissions ->
            submissionAdapter.submitList(submissions)

            if (submissions.isEmpty()) {
                binding.emptyState.show()
                binding.recyclerSubmissions.hide()
            } else {
                binding.emptyState.hide()
                binding.recyclerSubmissions.show()
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) binding.progressBar.show() else binding.progressBar.hide()
        }

        viewModel.error.observe(this) { errorMsg ->
            if (errorMsg.isNotBlank()) {
                toast(errorMsg)
            }
        }
    }
}
