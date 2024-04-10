package com.demo.kekmessenger.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.demo.kekmessenger.data.imagesRepo.ImagesRepository
import com.demo.kekmessenger.databinding.FragmentOpenImageBinding
import com.demo.kekmessenger.ui.activities.MainActivity
import com.demo.kekmessenger.utils.UtilityFunctions.errorMessage
import com.demo.kekmessenger.utils.UtilityFunctions.mapToApplicationException
import kotlinx.coroutines.launch
import javax.inject.Inject

class OpenImageFragment : Fragment() {
    @Inject
    lateinit var imagesRepository: ImagesRepository
    private lateinit var binding: FragmentOpenImageBinding
    private val args: OpenImageFragmentArgs by navArgs()
    private val openImageFragmentComponent by lazy {
        initializeOpenImageFragmentComponent()
    }

    private fun initializeOpenImageFragmentComponent() =
        (requireActivity() as MainActivity).mainActivityComponent.openImageFragmentFactory()
            .create(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOpenImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        openImageFragmentComponent.inject(this)
        lifecycleScope.launch {
            val result = imagesRepository.getImage("img/${args.key}")
            binding.progressBar.visibility = View.GONE
            if (result.isSuccess) {
                binding.imgImageView.setImageDrawable(result.getOrThrow())
                binding.imgImageView.visibility = View.VISIBLE
            } else {
                Toast.makeText(
                    requireContext(),
                    result.exceptionOrNull()!!.mapToApplicationException()
                        ?.errorMessage(requireContext()),
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }
}