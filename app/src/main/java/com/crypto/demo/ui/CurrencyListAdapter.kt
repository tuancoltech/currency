package com.crypto.demo.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.crypto.demo.R
import com.crypto.demo.databinding.ItemCurrencyRowBinding

class CurrencyListAdapter :
    ListAdapter<CurrencyRowItem, CurrencyListAdapter.CurrencyViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyViewHolder {
        val binding = ItemCurrencyRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CurrencyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CurrencyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CurrencyViewHolder(private val binding: ItemCurrencyRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CurrencyRowItem) {
            binding.currencyTitle.text = item.title
            binding.currencySubtitle.text =
                binding.currencySubtitle.context.getString(
                    R.string.currency_subtitle_format,
                    item.symbol,
                    item.subtitle
                )
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<CurrencyRowItem>() {
        override fun areItemsTheSame(oldItem: CurrencyRowItem, newItem: CurrencyRowItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: CurrencyRowItem,
            newItem: CurrencyRowItem
        ): Boolean {
            return oldItem == newItem
        }
    }
}
