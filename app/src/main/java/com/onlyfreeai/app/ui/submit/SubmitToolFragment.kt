package com.onlyfreeai.app.ui.submit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.onlyfreeai.app.databinding.FragmentSubmitToolBinding
import com.onlyfreeai.app.util.*

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

        // Apply premium tactile feedback
        binding.btnFetchUrl.scalePress()
        binding.btnSubmit.scalePress()

        // Staggered entrance animation for form elements
        val viewsToAnimate = listOf(
            binding.etUrl,
            binding.btnFetchUrl,
            binding.etName,
            binding.etDescription,
            binding.spinnerCategory,
            binding.etWhatsFree,
            binding.btnSubmit
        )
        viewsToAnimate.forEachIndexed { index, v ->
            v.animateEntrance(index * 50L)
        }
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

            if (url.isBlank() || !android.util.Patterns.WEB_URL.matcher(url).matches()) {
                requireContext().toast("Please enter a valid URL")
                return@setOnClickListener
            }
            if (name.isBlank() || name.length > Constants.MAX_TOOL_NAME_LENGTH) {
                requireContext().toast("Name must be 1-${Constants.MAX_TOOL_NAME_LENGTH} characters")
                return@setOnClickListener
            }
            if (description.isBlank() || description.length > Constants.MAX_DESCRIPTION_LENGTH) {
                requireContext().toast("Description must be 1-${Constants.MAX_DESCRIPTION_LENGTH} characters")
                return@setOnClickListener
            }
            if (category == "All" || category.isBlank()) {
                requireContext().toast("Please select a valid category")
                return@setOnClickListener
            }
            if (whatsFree.isBlank() || whatsFree.length > Constants.MAX_FREE_ITEM_LENGTH) {
                requireContext().toast("What's free info must be 1-${Constants.MAX_FREE_ITEM_LENGTH} characters")
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
        binding.etUrl.setText("")
        binding.etName.setText("")
        binding.etDescription.setText("")
        binding.etWhatsFree.setText("")
        binding.spinnerCategory.setSelection(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
