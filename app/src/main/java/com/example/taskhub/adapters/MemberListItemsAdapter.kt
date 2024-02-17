package com.example.taskhub.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.taskhub.R
import com.example.taskhub.databinding.ItemMemberBinding
import com.example.taskhub.models.User
import com.example.taskhub.utils.Constants

open class MemberListItemsAdapter(private val context: Context, private var list:ArrayList<User>)
    :RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var onClickListener:OnClickListener?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater=LayoutInflater.from(parent.context)
        val binding=ItemMemberBinding.inflate(inflater,parent,false)
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
                .placeholder(R.drawable.ic_user_place_holder)
                .into(holder.binding.ivMemberImage)
            holder.binding.tvMemberName.text=model.name
            holder.binding.tvMemberEmail.text=model.email
            if (model.selected){
                holder.binding.ivSelectedMember.visibility=View.VISIBLE
            }else
                holder.binding.ivSelectedMember.visibility=View.GONE
            holder.itemView.setOnClickListener {
                if (onClickListener!=null){
                    if (model.selected){
                        onClickListener!!.onClick(position,model,Constants.UN_SELECT)
                    }else{
                        onClickListener!!.onClick(position,model,Constants.SELECT)
                    }
                }
            }
        }
    }
    fun setOnTheClickListener(onClickListener: OnClickListener){
        this.onClickListener=onClickListener
    }

    class MyViewHolder(val binding:ItemMemberBinding):RecyclerView.ViewHolder(binding.root)

    interface OnClickListener{
        fun onClick(position: Int,user: User,action:String)
    }
}