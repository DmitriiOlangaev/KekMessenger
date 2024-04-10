package com.demo.kekmessenger.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.demo.kekmessenger.databinding.FragmentChatsBinding
import com.demo.kekmessenger.ui.activities.MainActivity
import com.demo.kekmessenger.ui.fragments.di.ChatsFragmentComponent
import com.demo.kekmessenger.ui.recyclerView.ChannelDelegate
import com.demo.kekmessenger.ui.recyclerView.RecyclerViewHolder
import com.demo.kekmessenger.utils.UtilityFunctions.errorMessage
import com.demo.kekmessenger.viewModels.ChatsViewModel
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChatsFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<ChatsViewModel> { viewModelFactory }
    private lateinit var binding: FragmentChatsBinding
    private val chatsFragmentComponent: ChatsFragmentComponent by lazy {
        initializeChatsFragmentComponent()
    }

    private lateinit var recyclerViewHolder: RecyclerViewHolder<String>

    private fun initializeChatsFragmentComponent() =
        (requireActivity() as MainActivity).mainActivityComponent.chatsFragmentFactory()
            .create(this)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatsFragmentComponent.inject(this)
        setUpViewModelErrorCollector()
        setUpChatsRecyclerView()
    }


    private fun setUpViewModelErrorCollector() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collect {
                    when (it) {
                        null -> {}
                        else -> {
                            Toast.makeText(
                                requireContext(),
                                it.errorMessage(requireContext()),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }


    private fun setUpChatsRecyclerView(): Unit {
        val layoutManager = LinearLayoutManager(requireContext())
        val adapter = chatsAdapter()
        recyclerViewHolder = RecyclerViewHolder(
            binding.chatsRecyclerView, layoutManager, adapter
        )
        binding.chatsRecyclerView.layoutManager = layoutManager
        binding.chatsRecyclerView.adapter = adapter
        recyclerViewHolder.recyclerView.addItemDecoration(
            DividerItemDecoration(
                recyclerViewHolder.recyclerView.context,
                layoutManager.orientation
            )
        )
        setUpViewModelDataCollector()
    }

    private fun setUpViewModelDataCollector(): Unit {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.channels.collect {
                    recyclerViewHolder.adapter.items =
                        it
                }
            }
        }
    }

    private fun chatsAdapter(): AsyncListDifferDelegationAdapter<String> {
        val callBack = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem
        }
        return object : AsyncListDifferDelegationAdapter<String>(callBack) {
            init {
                delegatesManager.addDelegate(ChannelDelegate { channel ->
                    val action = ChatsFragmentDirections.actionChatsFragmentToChatFragment(channel)
                    this@ChatsFragment.findNavController().navigate(action)
                })
                items = listOf()
            }
        }
    }

}