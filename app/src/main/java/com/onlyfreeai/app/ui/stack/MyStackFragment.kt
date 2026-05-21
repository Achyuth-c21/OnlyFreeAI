package com.onlyfreeai.app.ui.stack

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.onlyfreeai.app.databinding.FragmentMyStackBinding
import com.onlyfreeai.app.ui.detail.ToolDetailActivity
import com.onlyfreeai.app.ui.home.ToolAdapter
import com.onlyfreeai.app.util.Constants
import com.onlyfreeai.app.util.hide
import com.onlyfreeai.app.util.show
import com.onlyfreeai.app.util.animateEntrance

class MyStackFragment : Fragment() {

    private var _binding: FragmentMyStackBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MyStackViewModel
    private lateinit var toolAdapter: ToolAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyStackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[MyStackViewModel::class.java]

        setupRecyclerView()
        observeData()

        binding.tvTitle.animateEntrance()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadSavedTools()
    }

    private fun setupRecyclerView() {
        toolAdapter = ToolAdapter { tool ->
            val intent = Intent(requireContext(), ToolDetailActivity::class.java)
            intent.putExtra(Constants.EXTRA_TOOL_ID, tool.id)
            startActivity(intent)
        }

        binding.recyclerSavedTools.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = toolAdapter
            setHasFixedSize(true)
        }
    }

    private fun observeData() {
        viewModel.savedTools.observe(viewLifecycleOwner) { tools ->
            toolAdapter.submitList(tools)

            if (tools.isEmpty()) {
                binding.emptyState.show()
                binding.recyclerSavedTools.hide()
            } else {
                binding.emptyState.hide()
                binding.recyclerSavedTools.show()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) binding.progressBar.show() else binding.progressBar.hide()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
