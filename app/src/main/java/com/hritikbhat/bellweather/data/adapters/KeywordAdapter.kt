package com.hritikbhat.bellweather.data.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.hritikbhat.bellweather.R
import com.hritikbhat.bellweather.data.db.tables.KeywordTable
import com.hritikbhat.bellweather.databinding.KeywordItemBinding


class KeywordAdapter(private var dataSet: ArrayList<KeywordTable>) : RecyclerView.Adapter<KeywordAdapter.ViewHolder>() {
    private var onItemClickListener: OnKeywordItemClickListener? = null


    interface OnKeywordItemClickListener {
        fun onAppItemClick(keyword: KeywordTable)
    }

    fun setOnItemClickListener(listener: OnKeywordItemClickListener) {
        onItemClickListener = listener
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<KeywordItemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.keyword_item,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    inner class ViewHolder(private val binding: KeywordItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(keyword: KeywordTable) {
            // Bind your data to the layout using data binding
            binding.keywordName.text = keyword.kName
            binding.appConstraintLayout.setOnClickListener {
                onItemClickListener?.onAppItemClick(keyword)
            }

        }
    }

    fun updateInsertedList(keywordArraylist:ArrayList<KeywordTable>){
        this.dataSet = keywordArraylist
        notifyItemInserted(dataSet.size)
    }

    fun updateDeletedItemList(position: Int){
        notifyItemRemoved(position)
    }

    fun getItems() = dataSet

}