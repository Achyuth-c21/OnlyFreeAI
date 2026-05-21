package com.onlyfreeai.app.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.onlyfreeai.app.data.model.Tool
import com.onlyfreeai.app.databinding.ItemToolCardBinding
import com.onlyfreeai.app.util.loadUrl
import com.onlyfreeai.app.util.show
import com.onlyfreeai.app.util.hide
import com.onlyfreeai.app.util.scalePress
import com.onlyfreeai.app.util.animateEntrance

class ToolAdapter(
    private val onToolClick: (Tool) -> Unit
) : ListAdapter<Tool, ToolAdapter.ToolViewHolder>(ToolDiffCallback()) {

    private var lastPosition = -1

    inner class ToolViewHolder(
        private val binding: ItemToolCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(tool: Tool) {
            binding.apply {
                tvToolName.text = tool.name
                tvToolDescription.text = tool.description
                tvCategory.text = tool.category
                imgToolLogo.loadUrl(tool.logoUrl)

                if (tool.isVerified) {
                    badgeVerified.show()
                } else {
                    badgeVerified.hide()
                }

                tvSaves.text = "${tool.saves} saves"

                root.scalePress()
                root.setOnClickListener { onToolClick(tool) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolViewHolder {
        val binding = ItemToolCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ToolViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ToolViewHolder, position: Int) {
        holder.bind(getItem(position))
        
        // Staggered entrance animation for new items
        if (position > lastPosition) {
            holder.itemView.animateEntrance((position * 40L).coerceAtMost(300L))
            lastPosition = position
        }
    }

    class ToolDiffCallback : DiffUtil.ItemCallback<Tool>() {
        override fun areItemsTheSame(oldItem: Tool, newItem: Tool) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Tool, newItem: Tool) = oldItem == newItem
    }
}
