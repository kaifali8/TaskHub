package com.example.taskhub.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskhub.activities.TaskListActivity
import com.example.taskhub.databinding.ItemTaskBinding
import com.example.taskhub.models.Task
import java.util.Collections

open class TaskListItemsAdapter(private val context:Context,private var list:ArrayList<Task>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mPositionDraggedFrom = -1
    private var mPositionDraggedTo = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTaskBinding.inflate(inflater, parent, false)
        val layoutParams=LinearLayout.LayoutParams(
            (parent.width * 0.8).toInt(),LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins((15.toDp()).toPx(),0,(40.toDp()).toPx(),0)
        binding.root.layoutParams=layoutParams
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model=list[holder.adapterPosition]
        if (holder is MyViewHolder){
            if (position == list.size-1){
                holder.binding.tvAddTaskList.visibility=View.VISIBLE
                holder.binding.llTaskItem.visibility=View.GONE
            }else{
                holder.binding.tvAddTaskList.visibility=View.GONE
                holder.binding.llTaskItem.visibility=View.VISIBLE
                holder.binding.tvAddCard.visibility=View.VISIBLE
            }
            holder.binding.tvTaskListTitle.text=model.title
            holder.binding.tvAddTaskList.setOnClickListener {
                holder.binding.tvAddTaskList.visibility=View.GONE
                holder.binding.cvAddTaskListName.visibility=View.VISIBLE
            }
            holder.binding.ibCloseListName.setOnClickListener {
                holder.binding.tvAddTaskList.visibility=View.VISIBLE
                holder.binding.cvAddTaskListName.visibility=View.GONE
            }
            holder.binding.ibDoneListName.setOnClickListener {
                val listName=holder.binding.etTaskListName.text.toString()
                if (listName.isNotEmpty()){
                    if (context is TaskListActivity){
                        context.createTaskList(listName)
                    }
                }else{
                    Toast.makeText(context, "Please Enter List Name", Toast.LENGTH_SHORT).show()
                }
            }
            holder.binding.ibEditListName.setOnClickListener {
                holder.binding.etEditTaskListName.setText(model.title)
                holder.binding.llTitleView.visibility=View.GONE
                holder.binding.cvEditTaskListName.visibility=View.VISIBLE
            }
            holder.binding.ibCloseEditableView.setOnClickListener {
                holder.binding.llTitleView.visibility=View.VISIBLE
                holder.binding.cvEditTaskListName.visibility=View.GONE
            }
            holder.binding.ibDoneEditListName.setOnClickListener {
                val listName=holder.binding.etEditTaskListName.text.toString()
                if (listName.isNotEmpty()){
                    if (context is TaskListActivity){
                        context.updateTaskList(position,listName,model)
                    }
                }else{
                    Toast.makeText(context, "Please Enter a List Name", Toast.LENGTH_SHORT).show()
                }
            }
            holder.binding.ibDeleteList.setOnClickListener {
                alertDialogForDeleteList(position,model.title)
            }
            holder.binding.tvAddCard.setOnClickListener {
                holder.binding.tvAddCard.visibility=View.GONE
                holder.binding.cvAddCard.visibility=View.VISIBLE
            }
            holder.binding.ibCloseCardName.setOnClickListener {
                holder.binding.tvAddCard.visibility=View.VISIBLE
                holder.binding.cvAddCard.visibility=View.GONE
            }
            holder.binding.ibDoneCardName.setOnClickListener {
                val cardName=holder.binding.etCardName.text.toString()
                if (cardName.isNotEmpty()){
                    if (context is TaskListActivity){
                        context.addCardToTaskList(position, cardName)
                    }
                }else{
                    Toast.makeText(context, "Please Enter a Card Name", Toast.LENGTH_SHORT).show()
                }
            }
            holder.binding.rvCardList.layoutManager=LinearLayoutManager(context)
            holder.binding.rvCardList.setHasFixedSize(true)
            val adapter=CardListItemsAdapter(context,model.cards)
            holder.binding.rvCardList.adapter=adapter

            adapter.setOnClickListener(
                object: CardListItemsAdapter.OnClickListener{
                    override fun onClick(cardPosition: Int) {
                        if (context is TaskListActivity){
                            context.cardDetails(position, cardPosition)
                        }
                    }
                }
            )
            val dividerItemDecoration=DividerItemDecoration(context,DividerItemDecoration.VERTICAL)
            holder.binding.rvCardList.addItemDecoration(dividerItemDecoration)

            val helper = ItemTouchHelper(
                object : ItemTouchHelper.SimpleCallback(
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
                ){
                    override fun onMove(
                        recyclerView: RecyclerView,
                        dragged: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                    ): Boolean {
                        val draggedPosition = dragged.adapterPosition
                        val targetPosition =target.adapterPosition
                        if (mPositionDraggedFrom == -1){
                            mPositionDraggedFrom=draggedPosition
                        }
                        mPositionDraggedTo=targetPosition
                        Collections.swap(list[position].cards,draggedPosition,targetPosition)
                        adapter.notifyItemMoved(draggedPosition,targetPosition)
                        return false
                    }

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    }

                    override fun clearView(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder
                    ) {
                        super.clearView(recyclerView, viewHolder)
                        if (mPositionDraggedFrom!=-1 && mPositionDraggedTo!=-1
                            && mPositionDraggedFrom!=mPositionDraggedTo){
                            (context as TaskListActivity).updateCardsInTaskList(position,list[position].cards)
                        }
                        mPositionDraggedFrom=-1
                        mPositionDraggedTo=-1
                    }
                }
            )
            helper.attachToRecyclerView(holder.binding.rvCardList)
        }
    }
    private fun alertDialogForDeleteList(position: Int,title:String){
         val builder=AlertDialog.Builder(context)
        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to delete $title?")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes"){
            dialogInterface,which->
            dialogInterface.dismiss()
            if (context is TaskListActivity){
                context.deleteTaskList(position)
            }
        }
        builder.setNegativeButton("No"){
            dialogInterface,which->
            dialogInterface.dismiss()
        }
        val alertDialog:AlertDialog=builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
    private fun Int.toDp():Int = (this / Resources.getSystem().displayMetrics.density).toInt()
    private fun Int.toPx():Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    class MyViewHolder(val binding: ItemTaskBinding):RecyclerView.ViewHolder(binding.root)
}