package com.hritikbhat.bellweather.data.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.hritikbhat.bellweather.R
import com.hritikbhat.bellweather.data.db.tables.AppTable
import com.hritikbhat.bellweather.databinding.AppItemBinding


class AppAdapter(private val dataSet: List<AppTable>) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {
    private var onItemClickListener: OnAppItemClickListener? = null


    interface OnAppItemClickListener {
        fun onAppItemClick(app: AppTable)
    }

    fun setOnItemClickListener(listener: OnAppItemClickListener) {
        onItemClickListener = listener
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<AppItemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.app_item,
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

    inner class ViewHolder(private val binding: AppItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(appDetails: AppTable) {
            // Bind your data to the layout using data binding
            binding.appNameText.text = appDetails.aName
            binding.appImgIV.setImageBitmap(appDetails.appImg)
            binding.appConstraintLayout.setOnClickListener {
                onItemClickListener?.onAppItemClick(appDetails)
            }

        }
    }

}
