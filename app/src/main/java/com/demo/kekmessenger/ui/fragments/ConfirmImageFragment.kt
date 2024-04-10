package com.demo.kekmessenger.ui.fragments

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.demo.kekmessenger.databinding.FragmentConfirmImageBinding

class ConfirmImageFragment : Fragment() {

    private lateinit var binding: FragmentConfirmImageBinding
    private val args: ConfirmImageFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentConfirmImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setImage()
        setButtonListener()
    }

    private fun setButtonListener() {
        binding.confirmButton.setOnClickListener {
            Log.d("ConfirmButton", "Pressed")
            findNavController().previousBackStackEntry!!.savedStateHandle["Confirm Result"] = true
            findNavController().popBackStack()
        }
    }

    private fun setImage() {
        requireContext().contentResolver.openInputStream(args.ImageUri).use {
            val bitmap = BitmapFactory.decodeStream(it)
            binding.imageview.setImageBitmap(bitmap)
        }
    }
}