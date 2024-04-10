package com.demo.kekmessenger.ui.recyclerView

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.NavController
import com.demo.kekmessenger.R
import com.demo.kekmessenger.data.messagesRepo.MessageType
import com.demo.kekmessenger.ui.fragments.ChatFragmentDirections
import com.demo.kekmessenger.viewModels.ChatViewModel
import com.demo.kekmessenger.viewModels.Message
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

class ImageMessageDelegate(
    private val viewModel: ChatViewModel,
    private val navController: NavController
) :
    AbsListItemAdapterDelegate<Message, Message, ImageMessageDelegate.ImageMessageViewHolder>() {
    override fun isForViewType(item: Message, items: MutableList<Message>, position: Int): Boolean =
        item.metaData.type == MessageType.IMAGE

    override fun onCreateViewHolder(parent: ViewGroup): ImageMessageViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_image, parent, false)
        return ImageMessageViewHolder(view, viewModel)
    }

    override fun onBindViewHolder(
        item: Message,
        viewHolder: ImageMessageViewHolder,
        payloads: MutableList<Any>
    ): Unit = viewHolder.bind(item)

    inner class ImageMessageViewHolder(itemView: View, private val viewModel: ChatViewModel) :
        BaseMessageViewHolder(itemView) {
        private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        private val imageView: ImageView = itemView.findViewById(R.id.imageMessageImageView)
        private val progressBar: CircularProgressIndicator =
            itemView.findViewById(R.id.progressBar)
        private var job: Job? = null

        fun bind(message: Message) {
            super.metadataBind(message)
            job?.cancel()
            imageView.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
            job = coroutineScope.launch {
                val result = viewModel.load(message.data)
                ensureActive()
                if (result.isSuccess) {
                    imageView.setImageDrawable(result.getOrThrow())
                    imageView.setOnClickListener {
                        val action =
                            ChatFragmentDirections.actionChatFragmentToOpenImageFragment(message.data)
                        navController.navigate(action)
                    }
                } else {
                    Log.d("ImageMessageViewHolder", result.exceptionOrNull()!!.message.toString())
                    imageView.setImageResource(R.drawable.image_placeholder)
                }
                progressBar.visibility = View.GONE
                imageView.visibility = View.VISIBLE
            }

        }
    }
}