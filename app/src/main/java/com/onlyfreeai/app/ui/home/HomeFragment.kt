package com.onlyfreeai.app.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.onlyfreeai.app.databinding.FragmentHomeBinding
import com.onlyfreeai.app.R
import com.onlyfreeai.app.ui.detail.ToolDetailActivity
import com.onlyfreeai.app.util.Constants
import com.onlyfreeai.app.util.hide
import com.onlyfreeai.app.util.show

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel
    private lateinit var toolAdapter: ToolAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        setupRecyclerView()
        setupSearch()
        setupCategoryFilter()
        observeData()
        setupAdminButton()

        viewModel.loadTools()
    }

    private fun setupAdminButton() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val isAdmin = com.onlyfreeai.app.data.repository.UserRepository().isAdmin()
                if (_binding == null) return@launch
                if (isAdmin) {
                    binding.btnAdmin.show()
                    binding.btnAdmin.setOnClickListener {
                        startActivity(Intent(requireContext(), com.onlyfreeai.app.ui.admin.AdminActivity::class.java))
                    }
                } else {
                    binding.btnAdmin.hide()
                }
            } catch (e: Exception) {
                // Silently handle — admin button just stays hidden
                if (_binding != null) binding.btnAdmin.hide()
            }
        }
    }

    private fun setupRecyclerView() {
        toolAdapter = ToolAdapter { tool ->
            val intent = Intent(requireContext(), ToolDetailActivity::class.java)
            intent.putExtra(Constants.EXTRA_TOOL_ID, tool.id)
            startActivity(intent)
        }

        binding.recyclerTools.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = toolAdapter
            setHasFixedSize(true)
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadTools()
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.searchTools(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    viewModel.loadTools()
                }
                return true
            }
        })
    }

    private fun setupCategoryFilter() {
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            Constants.CATEGORIES
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = spinnerAdapter

        // Dynamically build the beautiful chips
        binding.chipGroupCategories.removeAllViews()
        Constants.CATEGORIES.forEachIndexed { index, category ->
            val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                text = category
                isClickable = true
                isCheckable = true
                isCheckedIconVisible = false
                
                // Styling corresponding to Midnight Luxe
                setTextColor(androidx.core.content.ContextCompat.getColorStateList(requireContext(), R.color.chip_text_color))
                background = androidx.core.content.ContextCompat.getDrawable(requireContext(), R.drawable.selector_category_chip)
                chipBackgroundColor = null
                chipStrokeColor = null
                chipStrokeWidth = 0f
                rippleColor = null
                
                val horizontalPadding = (20 * resources.displayMetrics.density).toInt()
                val verticalPadding = (10 * resources.displayMetrics.density).toInt()
                setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
                
                id = index
                isChecked = (index == 0)
                isSelected = (index == 0)
            }

            chip.setOnClickListener {
                // Sync spinner selection in the background
                binding.spinnerCategory.setSelection(index)

                // Select chip and uncheck others
                for (i in 0 until binding.chipGroupCategories.childCount) {
                    val child = binding.chipGroupCategories.getChildAt(i) as? com.google.android.material.chip.Chip
                    val isCurrent = (child?.id == index)
                    child?.isChecked = isCurrent
                    child?.isSelected = isCurrent
                }

                // Reset position tracker in adapter so stagger entrance fires again
                (binding.recyclerTools.adapter as? ToolAdapter)?.apply {
                    // We can't access private property lastPosition directly, but submitting a new list will reset automatically or we can ignore
                }

                // Load appropriate category
                if (category == "All") {
                    viewModel.loadTools()
                } else {
                    viewModel.filterByCategory(category)
                }
            }
            binding.chipGroupCategories.addView(chip)
        }
    }

    private fun observeData() {
        viewModel.tools.observe(viewLifecycleOwner) { tools ->
            toolAdapter.submitList(tools)
            binding.swipeRefresh.isRefreshing = false

            if (tools.isEmpty() && viewModel.isLoading.value != true) {
                binding.emptyState.show()
                binding.recyclerTools.hide()
            } else {
                binding.emptyState.hide()
                if (viewModel.isLoading.value != true) {
                    binding.recyclerTools.show()
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading && !binding.swipeRefresh.isRefreshing) {
                binding.shimmerViewContainer.show()
                binding.shimmerViewContainer.startShimmer()
                binding.recyclerTools.hide()
                binding.emptyState.hide()
            } else {
                binding.shimmerViewContainer.stopShimmer()
                binding.shimmerViewContainer.hide()
                if (viewModel.tools.value?.isNotEmpty() == true) {
                    binding.recyclerTools.show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
