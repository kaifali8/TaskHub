package com.example.taskhub.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.taskhub.R
import com.example.taskhub.databinding.ItemBoardBinding
import com.example.taskhub.models.Board


open class BoardsItemAdapter(private val context: Context,
    private val list: ArrayList<Board>):RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var onClickListener:OnClickListener?=null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemBoardBinding.inflate(inflater, parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if (holder is MyViewHolder){
            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_board_place_holder)
                .into(holder.binding.ivBoardImage)
            holder.binding.tvName.text=model.name
            holder.binding.tvCreatedBy.text="Created by: ${model.createdBy}"
            holder.itemView.setOnClickListener {
                if (onClickListener!=null){
                    onClickListener?.onClick(position,model)
                }
            }
        }
    }
    interface OnClickListener{
        fun onClick(position: Int,model:Board)
    }
    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }
    private class MyViewHolder(val binding:ItemBoardBinding):RecyclerView.ViewHolder(binding.root){

    }
}