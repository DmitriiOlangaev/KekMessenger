package com.demo.kekmessenger.ui.fragments

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demo.kekmessenger.R
import com.demo.kekmessenger.databinding.FragmentChatBinding
import com.demo.kekmessenger.ui.activities.MainActivity
import com.demo.kekmessenger.ui.fragments.di.ChatFragmentComponent
import com.demo.kekmessenger.ui.recyclerView.ImageMessageDelegate
import com.demo.kekmessenger.ui.recyclerView.RecyclerViewHolder
import com.demo.kekmessenger.ui.recyclerView.TextMessageDelegate
import com.demo.kekmessenger.utils.UtilityFunctions.errorMessage
import com.demo.kekmessenger.viewModels.ChatViewModel
import com.demo.kekmessenger.viewModels.Message
import com.demo.kekmessenger.viewModels.di.ChatViewModelFactory
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

class ChatFragment : Fragment() {


    @Inject
    lateinit var viewModelFactory: ChatViewModelFactory
    private val args: ChatFragmentArgs by navArgs()
    private val viewModel by viewModels<ChatViewModel> {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                viewModelFactory.create(args.channel) as T
        }
    }
    private lateinit var binding: FragmentChatBinding
    private val chatFragmentComponent: ChatFragmentComponent by lazy {
        initializeChatFragmentComponent()
    }

    private lateinit var recyclerViewHolder: RecyclerViewHolder<Message>

    private fun initializeChatFragmentComponent() =
        (requireActivity() as MainActivity).mainActivityComponent.chatFragmentFactory().create(this)

    private lateinit var photosDir: File

    private lateinit var latestImageUri: Uri
    private var scrollDirection: ScrollDirection = ScrollDirection.NONE
    private lateinit var scrollType: ScrollType
    private var scrollButtonHidingJob: Job? = null

    private enum class ScrollDirection {
        UP, DOWN, NONE
    }

    private enum class ScrollType {
        SMOOTH, FAST
    }

    private val selectPhotoLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { selectedImage ->
            if (selectedImage != null) {
                latestImageUri = selectedImage
                startConfirmImageFragment()
            } else {
                Toast.makeText(requireContext(), "Image wasn't picked", Toast.LENGTH_SHORT).show()
            }
        }
    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { result ->
            if (result) {
                startConfirmImageFragment()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Error while taking photo occurred",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatFragmentComponent.inject(this)
        setChannelNameTextView()
        setCurrentNameCollector()
        setUpMessagesRecyclerView()
        setMessagesCollector()
        addRecyclerViewTouchListener()
        setScrollButtonListener()
        setLoadingStatusCollector()
        setRefreshButtonListener()
        setGetImageButtonListener()
        setSendMessageButtonListener()
        setConfirmResultListener()
        setErrorCollector()
        photosDir = File(requireContext().applicationContext.cacheDir, "photos").apply { mkdir() }
    }

    private fun setConfirmResultListener() {
        findNavController().currentBackStackEntry!!.savedStateHandle.getLiveData<Boolean>("Confirm Result")
            .observe(viewLifecycleOwner) { confirm ->
                Log.d("ChatFragment", "Confirm = $confirm")
                if (confirm) {
                    val contentResolver = requireContext().applicationContext.contentResolver
                    contentResolver.openInputStream(latestImageUri).use { stream ->
                        val bitmap = BitmapFactory.decodeStream(stream)
                        if (bitmap == null) {
                            Toast.makeText(
                                requireContext(),
                                "Image wasn't send due forbidden storage access",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Log.d("ChatFragment", "images was sent")
                            viewModel.send(bitmap)
                        }
                    }
                    findNavController().currentBackStackEntry!!.savedStateHandle["Confirm Result"] =
                        false
                }
            }
    }

    private fun setCollector(f: suspend CoroutineScope.() -> Unit) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED, f)
        }
    }

    private fun setCurrentNameCollector() {
        setCollector {
            viewModel.name.collect {
                binding.currentNameTextView.text = it
            }
        }
    }

    private fun setLoadingStatusCollector() {
        setCollector {
            viewModel.isLoadingMessages.collect {
                when (it) {
                    true -> binding.messagesLoadingProgressBar.visibility = View.VISIBLE
                    false -> binding.messagesLoadingProgressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun setErrorCollector() {
        setCollector {
            viewModel.error.collect {
                when (it) {
                    null -> {}
                    else -> {
                        Toast.makeText(
                            requireContext(),
                            it.errorMessage(requireContext()),
                            Toast.LENGTH_SHORT
                        ).show()
                        viewModel.errorHandled()
                    }
                }
            }
        }
    }

    private fun startConfirmImageFragment() {
        val action =
            ChatFragmentDirections.actionChatFragmentToConfirmImageFragment2(latestImageUri)
        findNavController().navigate(action)
    }

    private fun makeScrollButtonVisible(button1: ImageButton, button2: ImageButton, id: Int) {
        scrollButtonHidingJob?.cancel()
        scrollDirection = ScrollDirection.NONE
        scrollType = ScrollType.SMOOTH
        button1.visibility = View.GONE
        button2.setImageDrawable(
            ResourcesCompat.getDrawable(
                resources,
                id,
                null
            )
        )
        button2.visibility = View.VISIBLE
        scrollButtonHidingJob = lifecycleScope.launch {
            delay(5000)
            button2.visibility = View.GONE
        }
    }

    private fun addRecyclerViewTouchListener() {
    }

    private fun setChannelNameTextView() {
        binding.channelNameTextView.text = args.channel
    }

    private fun setRefreshButtonListener() {
        binding.refreshButton.setOnClickListener {
            viewModel.refresh()
        }
    }

    private fun setSendMessageButtonListener() {
        binding.sendMessageButton.setOnClickListener {
            val message = binding.messageEditText.text.toString()
            binding.messageEditText.text?.clear()
            viewModel.send(message)
        }
    }

    private fun showImageSourceDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose source")
            .setItems(arrayOf<CharSequence>("Gallery", "Camera")) { _, which ->
                when (which) {
                    0 -> {
                        getImages()
                    }

                    1 -> {
                        takePhoto()
                    }

                    else -> {
                        throw IllegalStateException()
                    }
                }
            }
        builder.show()
    }

    private fun getImages(): Unit {
        selectPhotoLauncher.launch("image/*")
    }

    private fun takePhoto(): Unit {
        val name =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", resources.configuration.locales[0]).format(
                Date(System.currentTimeMillis())
            ) + "photo.jpg"
        val photoFile = File.createTempFile(name, null, photosDir)
        val photoUri = FileProvider.getUriForFile(
            requireContext().applicationContext,
            requireContext().applicationContext.packageName + ".provider",
            photoFile
        )
        latestImageUri = photoUri
        takePhotoLauncher.launch(photoUri)
    }

    private fun setGetImageButtonListener() {
        binding.getImageButton.setOnClickListener {
            showImageSourceDialog()
        }
    }

    private fun scroll(sd: ScrollDirection) {
        scrollButtonHidingJob?.cancel()
        val (pos: Int, button: ImageButton, id: Int) = getScrollData(sd)
        when (scrollDirection) {
            sd -> {
                scrollType = ScrollType.FAST
                recyclerViewHolder.recyclerView.scrollToPosition(pos)
            }

            ScrollDirection.NONE -> {
                scrollDirection = sd
                scrollType = ScrollType.SMOOTH
                recyclerViewHolder.recyclerView.smoothScrollToPosition(pos)
                button.setImageDrawable(ResourcesCompat.getDrawable(resources, id, null))
                viewModel.loadAll(sd == ScrollDirection.UP)
            }

            else -> {
                throw IllegalStateException()
            }
        }
        lifecycleScope.launch {
            delay(5000)
            button.visibility = View.GONE
        }
    }

    private fun getScrollData(sd: ScrollDirection): Triple<Int, ImageButton, Int> {
        val pos: Int
        val button: ImageButton
        val id: Int
        when (sd) {
            ScrollDirection.UP -> {
                pos = 0
                button = binding.scrollToUpButton
                id = R.drawable.double_up_arrow
            }

            ScrollDirection.DOWN -> {
                pos = Integer.max(
                    0,
                    recyclerViewHolder.adapter.itemCount - 1
                )
                button = binding.scrollToDownButton
                id = R.drawable.double_down_arrow
            }

            ScrollDirection.NONE -> throw IllegalArgumentException()
        }
        return Triple(pos, button, id)
    }

    private fun setScrollButtonListener() {
        binding.scrollToDownButton.setOnClickListener {
            scroll(ScrollDirection.DOWN)
        }
        binding.scrollToUpButton.setOnClickListener {
            scroll(ScrollDirection.UP)
        }
    }

    private fun setMessagesCollector() {
        setCollector {
            viewModel.messages.collect {
                recyclerViewHolder.adapter.items =
                    it
                when (scrollDirection) {
                    ScrollDirection.NONE -> {}
                    else -> {
                        val pos = getScrollData(scrollDirection).first
                        if (scrollType == ScrollType.SMOOTH) {
                            recyclerViewHolder.recyclerView.smoothScrollToPosition(pos)
                        } else {
                            recyclerViewHolder.recyclerView.scrollToPosition(pos)
                        }
                    }
                }
            }
        }
    }

    private fun setUpMessagesRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())
        val adapter = messagesAdapter()
        recyclerViewHolder =
            RecyclerViewHolder(binding.messagesRecyclerView, layoutManager, adapter)
        binding.messagesRecyclerView.layoutManager = layoutManager
        binding.messagesRecyclerView.adapter = adapter
        recyclerViewHolder.recyclerView.addItemDecoration(
            DividerItemDecoration(
                recyclerViewHolder.recyclerView.context,
                layoutManager.orientation
            )
        )
        setOnScrollListener()
    }

    private fun setOnScrollListener() {
        recyclerViewHolder.recyclerView.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_SETTLING || recyclerView.scrollState != RecyclerView.SCROLL_STATE_DRAGGING) return
                val requiredRemainToStartLoad = 10
                if (dy > 0) {
                    //DOWN
                    makeScrollButtonVisible(
                        binding.scrollToUpButton,
                        binding.scrollToDownButton,
                        R.drawable.down_arrow
                    )
                    if (recyclerViewHolder.layoutManager.findLastVisibleItemPosition() + requiredRemainToStartLoad >= recyclerViewHolder.layoutManager.itemCount) {
                        viewModel.load(false)
                    }
                } else if (dy < 0) {
                    //UP
                    makeScrollButtonVisible(
                        binding.scrollToDownButton,
                        binding.scrollToUpButton,
                        R.drawable.up_arrow
                    )
                    if (recyclerViewHolder.layoutManager.findFirstVisibleItemPosition() - requiredRemainToStartLoad <= 0) {
                        viewModel.load(true)
                    }
                }
            }
        })
    }

    private fun messagesAdapter(): AsyncListDifferDelegationAdapter<Message> {
        val callBack = object : DiffUtil.ItemCallback<Message>() {
            override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean =
                oldItem.metaData.id == newItem.metaData.id

            override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean =
                oldItem.metaData.id == newItem.metaData.id
        }
        return object : AsyncListDifferDelegationAdapter<Message>(callBack) {
            init {
                delegatesManager.addDelegate(ImageMessageDelegate(viewModel, findNavController()))
                delegatesManager.addDelegate(TextMessageDelegate())
                items = viewModel.messages.value
            }
        }
    }

    override fun onDestroy() {
        photosDir.deleteRecursively()
        super.onDestroy()
    }
}