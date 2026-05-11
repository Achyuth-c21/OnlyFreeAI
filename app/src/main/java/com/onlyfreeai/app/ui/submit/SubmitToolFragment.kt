package com.onlyfreeai.app.ui.submit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.onlyfreeai.app.databinding.FragmentSubmitToolBinding
import com.onlyfreeai.app.util.Constants
import com.onlyfreeai.app.util.hide
import com.onlyfreeai.app.util.show
import com.onlyfreeai.app.util.toast

class SubmitToolFragment : Fragment() {

    private var _binding: FragmentSubmitToolBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SubmitToolViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSubmitToolBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[SubmitToolViewModel::class.java]

        setupCategorySpinner()
        setupClickListeners()
        observeData()
    }

    private fun setupCategorySpinner() {
        val categories = Constants.CATEGORIES.filter { it != "All" }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupClickListeners() {
        // Auto-fetch metadata from URL
        binding.btnFetchUrl.setOnClickListener {
            val url = binding.etUrl.text.toString().trim()
            if (url.isBlank()) {
                requireContext().toast("Please enter a URL")
                return@setOnClickListener
            }
            viewModel.fetchUrlMetadata(url)
        }

        // Submit tool
        binding.btnSubmit.setOnClickListener {
            val url = binding.etUrl.text.toString().trim()
            val name = binding.etName.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            val category = binding.spinnerCategory.selectedItem.toString()
            val whatsFree = binding.etWhatsFree.text.toString().trim()

            if (url.isBlank() || name.isBlank() || description.isBlank() || whatsFree.isBlank()) {
                requireContext().toast("Please fill in all fields")
                return@setOnClickListener
            }

            viewModel.submitTool(
                url = url,
                name = name,
                description = description,
                category = category,
                whatsFree = whatsFree.split("\n").filter { it.isNotBlank() }
            )
        }
    }

    private fun observeData() {
        viewModel.urlMetadata.observe(viewLifecycleOwner) { metadata ->
            metadata?.let {
                binding.etName.setText(it.title)
                binding.etDescription.setText(it.description)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBar.show()
                binding.btnSubmit.isEnabled = false
            } else {
                binding.progressBar.hide()
                binding.btnSubmit.isEnabled = true
            }
        }

        viewModel.submitResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    requireContext().toast("Tool submitted! It will be reviewed soon.")
                    clearForm()
                } else {
                    requireContext().toast(it.exceptionOrNull()?.message ?: "Submission failed")
                }
            }
        }

        viewModel.canSubmit.observe(viewLifecycleOwner) { canSubmit ->
            if (!canSubmit) {
                binding.btnSubmit.isEnabled = false
                requireContext().toast("You've reached the daily submission limit (3/day)")
            }
        }
    }

    private fun clearForm() {
        binding.etUrl.text?.clear()
        binding.etName.text?.clear()
        binding.etDescription.text?.clear()
        binding.etWhatsFree.text?.clear()
        binding.spinnerCategory.setSelection(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
