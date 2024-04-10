package com.demo.kekmessenger.ui.recyclerView

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.demo.kekmessenger.R
import com.demo.kekmessenger.data.messagesRepo.MessageType
import com.demo.kekmessenger.viewModels.Message
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate

class TextMessageDelegate :
    AbsListItemAdapterDelegate<Message, Message, TextMessageDelegate.TextMessageViewHolder>() {
    override fun isForViewType(item: Message, items: MutableList<Message>, position: Int): Boolean =
        item.metaData.type == MessageType.TEXT

    override fun onCreateViewHolder(parent: ViewGroup): TextMessageViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_text, parent, false)
        return TextMessageViewHolder(view)
    }

    override fun onBindViewHolder(
        item: Message,
        viewHolder: TextMessageViewHolder,
        payloads: MutableList<Any>
    ): Unit =
        viewHolder.bind(item)


    inner class TextMessageViewHolder(itemView: View) : BaseMessageViewHolder(itemView) {
        private val textMessageTextView: TextView =
            itemView.findViewById(R.id.textMessageTextView)

        fun bind(message: Message) {
            super.metadataBind(message)
            textMessageTextView.text = message.data
        }
    }

}