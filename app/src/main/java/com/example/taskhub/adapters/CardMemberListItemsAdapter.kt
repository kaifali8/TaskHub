package com.example.taskhub.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.taskhub.R
import com.example.taskhub.databinding.ItemCardSelectedMemberBinding
import com.example.taskhub.models.SelectedMembers

open class CardMemberListItemsAdapter(private val context:Context, private val list: ArrayList<SelectedMembers>,
                                      private val assignMembers:Boolean ):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var onClickListener:OnClickListener?=null
    private lateinit var binding:ItemCardSelectedMemberBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater=LayoutInflater.from(parent.context)
        val binding=ItemCardSelectedMemberBinding.inflate(inflater,parent,false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model=list[holder.adapterPosition]
        if (holder is MyViewHolder){
            if (position == list.size-1 && assignMembers){
                holder.binding.ivAddMember.visibility=View.VISIBLE
                holder.binding.ivSelectedMemberImage.visibility=View.GONE
            }else{
                holder.binding.ivAddMember.visibility=View.GONE
                holder.binding.ivSelectedMemberImage.visibility=View.VISIBLE

                Glide
                    .with(context)
                    .load(model.image)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(holder.binding.ivSelectedMemberImage)
            }
            holder.itemView.setOnClickListener {
                if (onClickListener != null){
                    onClickListener!!.onClick()
                }
            }
        }
    }
    class MyViewHolder(val binding: ItemCardSelectedMemberBinding):RecyclerView.ViewHolder(binding.root)

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener=onClickListener
    }

    interface OnClickListener{
        fun onClick()
    }
}