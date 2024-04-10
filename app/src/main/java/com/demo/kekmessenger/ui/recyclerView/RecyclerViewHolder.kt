package com.demo.kekmessenger.ui.recyclerView

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter

data class RecyclerViewHolder<T>(
    val recyclerView: RecyclerView,
    val layoutManager: LinearLayoutManager,
    val adapter: AsyncListDifferDelegationAdapter<T>
)
