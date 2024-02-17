package com.example.taskhub.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskhub.adapters.MemberListItemsAdapter
import com.example.taskhub.databinding.DialogListBinding
import com.example.taskhub.models.User

abstract class MembersListDialog(
    context: Context, private var list: ArrayList<User>, private val title:String=""): Dialog(context){

    private var adapter:MemberListItemsAdapter?=null
    private lateinit var binding:DialogListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DialogListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerView()
    }
    private fun setUpRecyclerView() {
        binding.tvTitle.text=title
        if (list.size>0){
            binding.rvList.layoutManager= LinearLayoutManager(context)
            adapter= MemberListItemsAdapter(context,list)
            binding.rvList.adapter=adapter

            adapter!!.setOnTheClickListener(object : MemberListItemsAdapter.OnClickListener{
                override fun onClick(position: Int, user: User, action: String) {
                    dismiss()
                    onItemSelected(user,action)
                }
            })
        }
    }
    protected abstract fun onItemSelected(user: User,action:String)
}