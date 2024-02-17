package com.example.taskhub.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.taskhub.databinding.ItemLabelColorBinding

class LabelColorListItemsAdapter(
    private val context: Context,
    private val list: ArrayList<String>,
    private val mSelectedColor:String):RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var onItemClickListener: OnItemClickListener?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater=LayoutInflater.from(parent.context)
        val binding=ItemLabelColorBinding.inflate(inflater,parent,false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item=list[position]
        if (holder is MyViewHolder){
            holder.binding.viewMain.setBackgroundColor(Color.parseColor(item))
            if (item==mSelectedColor){
                holder.binding.ivSelectedColor.visibility=View.VISIBLE
            }else{
                holder.binding.ivSelectedColor.visibility=View.GONE
            }
            holder.itemView.setOnClickListener{
                if (onItemClickListener!=null){
                    onItemClickListener!!.onClick(position,item)
                }
            }
        }
    }
    private class MyViewHolder(val binding:ItemLabelColorBinding):RecyclerView.ViewHolder(binding.root)

    interface OnItemClickListener{
        fun onClick(position: Int,color:String)
    }
}