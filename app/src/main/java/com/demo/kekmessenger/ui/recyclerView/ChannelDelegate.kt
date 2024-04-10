package com.demo.kekmessenger.ui.recyclerView

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.demo.kekmessenger.R
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate

class ChannelDelegate(private val onItemClick: (String) -> Unit) :
    AbsListItemAdapterDelegate<String, String, ChannelDelegate.ChannelViewHolder>() {


    override fun isForViewType(item: String, items: MutableList<String>, position: Int): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): ChannelViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_channel, parent, false)
        return ChannelViewHolder(view)
    }

    override fun onBindViewHolder(
        item: String,
        viewHolder: ChannelViewHolder,
        payloads: MutableList<Any>
    ): Unit = viewHolder.bind(item)

    inner class ChannelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val channelNameTextView: TextView =
            itemView.findViewById(R.id.channelNameTextView)

        fun bind(channel: String) {
            channelNameTextView.text = channel
            itemView.setOnClickListener {
                onItemClick(channel)
            }
        }
    }
}
