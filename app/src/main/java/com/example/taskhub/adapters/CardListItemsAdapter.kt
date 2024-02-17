package com.example.taskhub.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskhub.activities.TaskListActivity
import com.example.taskhub.databinding.ItemCardBinding
import com.example.taskhub.models.Card
import com.example.taskhub.models.SelectedMembers

open class CardListItemsAdapter(
    private val context: Context,
    private val list: ArrayList<Card>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var onClickListener: OnClickListener? =null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater=LayoutInflater.from(parent.context)
        val binding=ItemCardBinding.inflate(inflater,parent,false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model=list[holder.adapterPosition]
        if (holder is MyViewHolder){
            if (model.labelColor.isNotEmpty()){
                holder.binding.viewLabelColor.visibility=View.VISIBLE
                holder.binding.viewLabelColor.setBackgroundColor(Color.parseColor(model.labelColor))
            }else{

                holder.binding.viewLabelColor.visibility=View.GONE
            }
            holder.binding.tvCardName.text =model.name

            if ((context as TaskListActivity).mAssignedMembersDetailList.size > 0){
                val selectedMembersList:ArrayList<SelectedMembers> = ArrayList()
                for (i in context.mAssignedMembersDetailList.indices){
                    for (j in model.assignedTo){
                        if (context.mAssignedMembersDetailList[i].id == j){
                            val selectedMember=SelectedMembers(
                                context.mAssignedMembersDetailList[i].id,
                                context.mAssignedMembersDetailList[i].image
                            )
                            selectedMembersList.add(selectedMember)
                        }
                    }
                }
                if (selectedMembersList.size > 0){
                    if (selectedMembersList.size == 1 && selectedMembersList[0].id == model.createdBy){
                        holder.binding.rvCardSelectedMembersList.visibility=View.GONE
                    }else{
                        holder.binding.rvCardSelectedMembersList.visibility=View.VISIBLE
                        holder.binding.rvCardSelectedMembersList.layoutManager=GridLayoutManager(context,4)

                        val adapter=CardMemberListItemsAdapter(context,selectedMembersList,false)
                        holder.binding.rvCardSelectedMembersList.adapter=adapter

                        adapter.setOnClickListener(object :CardMemberListItemsAdapter.OnClickListener{
                            override fun onClick() {
                                if (onClickListener!=null){
                                    onClickListener!!.onClick(position)
                                }
                            }
                        })
                    }
                }else{
                    holder.binding.rvCardSelectedMembersList.visibility=View.GONE
                }
            }

            holder.itemView.setOnClickListener {
                if (onClickListener!=null){
                    onClickListener!!.onClick(position)
                }
            }
        }
    }
    fun setOnClickListener(onClickListener:OnClickListener){
        this.onClickListener=onClickListener
    }
    interface OnClickListener{
        fun onClick(position: Int)
    }
    class MyViewHolder(val binding: ItemCardBinding):RecyclerView.ViewHolder(binding.root)
}