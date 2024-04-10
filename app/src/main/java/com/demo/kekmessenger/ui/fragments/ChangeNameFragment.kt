package com.demo.kekmessenger.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.demo.kekmessenger.R
import com.demo.kekmessenger.data.preferencesRepo.UserPreferencesRepository
import com.demo.kekmessenger.databinding.FragmentChangeNameBinding
import com.demo.kekmessenger.ui.activities.MainActivity
import com.demo.kekmessenger.ui.fragments.di.ChangeNameFragmentComponent
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChangeNameFragment : Fragment() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var binding: FragmentChangeNameBinding
    private val changeNameFragmentComponent: ChangeNameFragmentComponent by lazy {
        initializeChangeNameFragmentComponent()
    }

    private fun initializeChangeNameFragmentComponent() =
        (requireActivity() as MainActivity).mainActivityComponent.changeNameFragmentFactory()
            .create(this)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChangeNameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        changeNameFragmentComponent.inject(this)
        setNameListener()
        setConfirmButtonOnClickListener()
    }

    private fun setNameListener() {
        lifecycleScope.launch {
            userPreferencesRepository.userPreferencesFlow.collect {
                binding.currentNameTextView.text = it.name
            }
        }
    }

    private fun setConfirmButtonOnClickListener() {
        binding.confirmNameButton.setOnClickListener {
            lifecycleScope.launch {
                val result =
                    userPreferencesRepository.changeName(binding.changeNameEditText.text.toString())
                if (result.isSuccess) {
                    Toast.makeText(
                        requireContext(),
                        R.string.change_name_success,
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(
                        requireContext(),
                        R.string.change_name_failure,
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        }
    }
}