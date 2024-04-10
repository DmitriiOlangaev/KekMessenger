package com.demo.kekmessenger.ui.recyclerView

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.demo.kekmessenger.R
import com.demo.kekmessenger.viewModels.Message
import java.text.SimpleDateFormat
import java.util.Date

open class BaseMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val senderNameTextView: TextView = itemView.findViewById(R.id.senderNameTextView)
    private val dateOfMessageTextView: TextView = itemView.findViewById(R.id.dateOfMessageTextView)
    private val messageIdTextView: TextView = itemView.findViewById(R.id.messageIdTextView)

    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")
    protected fun metadataBind(message: Message) {
        senderNameTextView.text = message.metaData.sender
        dateOfMessageTextView.text = sdf.format(Date(message.metaData.time))
        messageIdTextView.text = message.metaData.id.toString()
    }
}