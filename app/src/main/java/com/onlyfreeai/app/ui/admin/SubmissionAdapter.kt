package com.onlyfreeai.app.ui.admin

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.onlyfreeai.app.data.model.Submission
import com.onlyfreeai.app.databinding.ItemSubmissionBinding
import com.onlyfreeai.app.util.hide
import com.onlyfreeai.app.util.loadUrl
import com.onlyfreeai.app.util.show
import com.onlyfreeai.app.util.toFormattedDate
import com.onlyfreeai.app.util.scalePress
import com.onlyfreeai.app.util.animateEntrance

class SubmissionAdapter(
    private val onApprove: (Submission) -> Unit,
    private val onReject: (Submission) -> Unit
) : ListAdapter<Submission, SubmissionAdapter.SubmissionViewHolder>(SubmissionDiffCallback()) {

    private var lastPosition = -1

    inner class SubmissionViewHolder(
        private val binding: ItemSubmissionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(submission: Submission) {
            binding.apply {
                tvName.text = submission.name
                tvDescription.text = submission.description
                tvCategory.text = submission.category
                tvUrl.text = submission.websiteUrl
                imgLogo.loadUrl(submission.logoUrl)

                submission.dateSubmitted?.let {
                    tvDate.text = it.toFormattedDate()
                }

                // Show/hide action buttons based on status
                when (submission.status) {
                    Submission.STATUS_PENDING -> {
                        btnApprove.show()
                        btnReject.show()
                        tvStatus.hide()
                    }
                    else -> {
                        btnApprove.hide()
                        btnReject.hide()
                        tvStatus.show()
                        tvStatus.text = submission.status.uppercase()
                    }
                }

                root.scalePress()
                btnApprove.scalePress()
                btnReject.scalePress()

                btnApprove.setOnClickListener { onApprove(submission) }
                btnReject.setOnClickListener { onReject(submission) }

                tvUrl.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(submission.websiteUrl))
                    itemView.context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmissionViewHolder {
        val binding = ItemSubmissionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SubmissionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubmissionViewHolder, position: Int) {
        holder.bind(getItem(position))
        
        // Staggered entrance animation
        if (position > lastPosition) {
            holder.itemView.animateEntrance((position * 40L).coerceAtMost(300L))
            lastPosition = position
        }
    }

    class SubmissionDiffCallback : DiffUtil.ItemCallback<Submission>() {
        override fun areItemsTheSame(old: Submission, new: Submission) = old.id == new.id
        override fun areContentsTheSame(old: Submission, new: Submission) = old == new
    }
}
