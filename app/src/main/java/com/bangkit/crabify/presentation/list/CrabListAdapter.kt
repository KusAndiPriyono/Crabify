package com.bangkit.crabify.presentation.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bangkit.crabify.data.model.Crab
import com.bangkit.crabify.databinding.ItemCrabLayoutBinding
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat

class CrabListAdapter(
    val onItemClicked: (Int, Crab) -> Unit
) : RecyclerView.Adapter<CrabListAdapter.MyViewHolder>() {

    val sdf = SimpleDateFormat("dd MMM yyyy : HH:mm:ss")
    private var list: MutableList<Crab> = arrayListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            ItemCrabLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
    }

    fun updateList(list: MutableList<Crab>) {
        this.list = list
        notifyDataSetChanged()
    }

    inner class MyViewHolder(val binding: ItemCrabLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Crab) {
            binding.date.text = sdf.format(item.date)
            Glide.with(itemView.context)
                .load(item.image)
                .error(android.R.drawable.stat_notify_error)
                .into(binding.ivResult)
            binding.tvOutputLabel.text = item.label[0]
            binding.tvOutputScore.text = item.score[0].toString()

            binding.crabLayout.setOnClickListener {
                onItemClicked.invoke(adapterPosition, item)
            }
        }
    }
}