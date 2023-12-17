package com.bangkit.crabify.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bangkit.crabify.data.model.SensorDataValue
import com.bangkit.crabify.databinding.SensorItemBinding
import com.bumptech.glide.Glide

class SensorListAdapter : RecyclerView.Adapter<SensorListAdapter.ViewHolder>() {

    private var list: MutableList<SensorDataValue> = arrayListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = SensorItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
    }

    fun updateList(list: MutableList<SensorDataValue>) {
        this.list = list
    }

    inner class ViewHolder(val binding: SensorItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SensorDataValue) {
            Glide.with(itemView.context)
                .load(item.image)
                .error(android.R.drawable.stat_notify_error)
                .into(binding.ivSensor)
            binding.tvSensorName.text = item.name
            binding.tvInUnit.text = item.unit
            binding.tvInUnitValue.text = item.value
        }
    }
}