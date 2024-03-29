package com.example.taskhub.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskhub.R
import com.example.taskhub.adapters.TaskListItemsAdapter
import com.example.taskhub.databinding.ActivityTaskListBinding
import com.example.taskhub.firebase.FirestoreClass
import com.example.taskhub.models.Board
import com.example.taskhub.models.Card
import com.example.taskhub.models.Task
import com.example.taskhub.models.User
import com.example.taskhub.utils.Constants

class TaskListActivity : BaseActivity() {
    private lateinit var mBoardDetails : Board
    private lateinit var mBoardDocumentId:String
    lateinit var mAssignedMembersDetailList:ArrayList<User>
    private lateinit var binding:ActivityTaskListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (intent.hasExtra(Constants.DOCUMENT_ID)){
            mBoardDocumentId=intent.getStringExtra(Constants.DOCUMENT_ID)!!
        }
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this,mBoardDocumentId)
    }
    fun boardDetails(board: Board){
        mBoardDetails=board
        hideProgressDialog()
        setupActionBar()

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAssignedMembersListDetails(this@TaskListActivity,mBoardDetails.assignedTo)
    }
    fun addUpdateTaskListSuccess(){
        hideProgressDialog()
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getBoardDetails(this,mBoardDetails.documentId)
    }
    fun createTaskList(taskListName:String){
        val task=Task(taskListName,FirestoreClass().getCurrentUserID())
        mBoardDetails.taskList.add(0,task)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)
        showProgressDialog(resources.getString(R.string.please_wait))
        mBoardDetails.documentId=mBoardDocumentId
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }
    fun updateTaskList(position:Int,listName:String,model:Task){
        val task=Task(listName,model.createdBy)
        mBoardDetails.taskList[position]=task
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)
        showProgressDialog(resources.getString(R.string.please_wait))
        mBoardDetails.documentId=mBoardDocumentId
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }
    fun deleteTaskList(position:Int){
        mBoardDetails.taskList.removeAt(position)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)
        showProgressDialog(resources.getString(R.string.please_wait))
        mBoardDetails.documentId=mBoardDocumentId
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }
    fun addCardToTaskList(position: Int,cardName:String){
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        val cardAssignedUsersList: ArrayList<String> = ArrayList()
        cardAssignedUsersList.add(FirestoreClass().getCurrentUserID())

        val card=Card(cardName,FirestoreClass().getCurrentUserID(),cardAssignedUsersList)

        val cardsList=mBoardDetails.taskList[position].cards
        cardsList.add(card)

        val task=Task(mBoardDetails.taskList[position].title,mBoardDetails.taskList[position].createdBy,cardsList)
        mBoardDetails.taskList[position]=task
        showProgressDialog(resources.getString(R.string.please_wait))
        mBoardDetails.documentId=mBoardDocumentId
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode== MEMBERS_REQUEST_CODE || requestCode== CARD_DETAILS_REQUEST_CODE){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardDetails(this,mBoardDocumentId)
        }else{
            Log.e("Cancelled","Cancelled")
        }
    }
    fun cardDetails(taskListPosition: Int,cardPosition:Int){
        val intent=Intent(this,CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL,mBoardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION,taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION,cardPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST,mAssignedMembersDetailList)
        intent.putExtra(Constants.DOCUMENT_ID,mBoardDocumentId)
        startActivityForResult(intent, CARD_DETAILS_REQUEST_CODE)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_members->{
                val intent=Intent(this,MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL,mBoardDetails)
                intent.putExtra(Constants.DOCUMENT_ID,mBoardDocumentId)
                startActivityForResult(intent, MEMBERS_REQUEST_CODE)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun setupActionBar(){
        setSupportActionBar(binding.toolbarTaskListActivity)
        val actionBar=supportActionBar
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_back_icon)
            actionBar.title=mBoardDetails.name
        }
        binding.toolbarTaskListActivity.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    fun boardMembersDetailList(list:ArrayList<User>){
        mAssignedMembersDetailList=list
        hideProgressDialog()
        val addTaskList=Task("Add List")
        mBoardDetails.taskList.add(addTaskList)

        binding.rvTaskList.layoutManager=LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        binding.rvTaskList.setHasFixedSize(true)

        val adapter=TaskListItemsAdapter(this,mBoardDetails.taskList)
        binding.rvTaskList.adapter=adapter
    }
    fun updateCardsInTaskList(taskListPosition: Int,cards:ArrayList<Card>){
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        mBoardDetails.taskList[taskListPosition].cards=cards
        showProgressDialog(resources.getString(R.string.please_wait))
        mBoardDetails.documentId=mBoardDocumentId
        FirestoreClass().addUpdateTaskList(this@TaskListActivity,mBoardDetails)
    }
    companion object{
        const val MEMBERS_REQUEST_CODE: Int = 13
        const val CARD_DETAILS_REQUEST_CODE:Int=14
    }
}