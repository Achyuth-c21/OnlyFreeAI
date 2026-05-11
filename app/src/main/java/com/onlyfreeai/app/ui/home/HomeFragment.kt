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
import com.onlyfreeai.app.ui.detail.ToolDetailActivity
import com.onlyfreeai.app.util.Constants
import com.onlyfreeai.app.util.hide
import com.onlyfreeai.app.util.show

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
        androidx.lifecycle.lifecycleScope.launchWhenStarted {
            val isAdmin = com.onlyfreeai.app.data.repository.UserRepository().isAdmin()
            if (isAdmin) {
                binding.btnAdmin.show()
                binding.btnAdmin.setOnClickListener {
                    startActivity(Intent(requireContext(), com.onlyfreeai.app.ui.admin.AdminActivity::class.java))
                }
            } else {
                binding.btnAdmin.hide()
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
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            Constants.CATEGORIES
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter

        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val category = Constants.CATEGORIES[position]
                if (category == "All") {
                    viewModel.loadTools()
                } else {
                    viewModel.filterByCategory(category)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun observeData() {
        viewModel.tools.observe(viewLifecycleOwner) { tools ->
            toolAdapter.submitList(tools)
            binding.swipeRefresh.isRefreshing = false

            if (tools.isEmpty()) {
                binding.emptyState.show()
                binding.recyclerTools.hide()
            } else {
                binding.emptyState.hide()
                binding.recyclerTools.show()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading && !binding.swipeRefresh.isRefreshing) {
                binding.progressBar.show()
            } else {
                binding.progressBar.hide()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
