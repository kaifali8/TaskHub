package com.example.taskhub.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.taskhub.R
import com.example.taskhub.adapters.CardMemberListItemsAdapter
import com.example.taskhub.databinding.ActivityCardDetailsBinding
import com.example.taskhub.dialogs.LabelColorListDialog
import com.example.taskhub.dialogs.MembersListDialog
import com.example.taskhub.firebase.FirestoreClass
import com.example.taskhub.models.Board
import com.example.taskhub.models.Card
import com.example.taskhub.models.SelectedMembers
import com.example.taskhub.models.Task
import com.example.taskhub.models.User
import com.example.taskhub.utils.Constants
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CardDetailsActivity : BaseActivity() {
    private lateinit var mBoardDetails:Board
    private var mTaskListPosition=-1
    private var mCardListPosition=-1
    private var mSelectedColor=""
    private lateinit var mMembersDetailList:ArrayList<User>
    private var mSelectedDueDateMilliSeconds:Long=0
    private lateinit var cardDetailsBinding: ActivityCardDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cardDetailsBinding=ActivityCardDetailsBinding.inflate(layoutInflater)
        setContentView(cardDetailsBinding.root)
        getIntentData()
        setupActionBar()
        cardDetailsBinding.etNameCardDetails.setText(mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].name)
        cardDetailsBinding.etNameCardDetails.setSelection(cardDetailsBinding.etNameCardDetails.text.toString().length)
        mSelectedColor=mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].labelColor
        if (mSelectedColor.isNotEmpty()){
            setColor()
        }
        cardDetailsBinding.btnUpdateCardDetails.setOnClickListener {
            if (cardDetailsBinding.etNameCardDetails.text.toString().isNotEmpty()){
                updateCardDetails()
            }else{
                Toast.makeText(this@CardDetailsActivity, "Please enter a card name", Toast.LENGTH_SHORT).show()
            }
        }
        cardDetailsBinding.tvSelectLabelColor.setOnClickListener {
            labelColorsListDialog()
        }
        cardDetailsBinding.tvSelectMembers.setOnClickListener {
            membersListDialog()
        }
        setUpSelectedMembersList()
        mSelectedDueDateMilliSeconds=mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].dueDate
        if (mSelectedDueDateMilliSeconds>0){
            val simpleDateFormat=SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val selectedDate=simpleDateFormat.format(Date(mSelectedDueDateMilliSeconds))
            cardDetailsBinding.tvSelectDueDate.text=selectedDate
        }
        cardDetailsBinding.tvSelectDueDate.setOnClickListener {
            showDatePicker()
        }
    }
    fun addUpdateTaskListSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }
    private fun setupActionBar(){
        setSupportActionBar(cardDetailsBinding.toolbarCardDetailsActivity)
        val actionBar=supportActionBar
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_back_icon)
            actionBar.title=mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].name
        }
        cardDetailsBinding.toolbarCardDetailsActivity.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    private fun colorsList():ArrayList<String>{
        val colorList:ArrayList<String> = ArrayList()
        colorList.add("#f03535")
        colorList.add("#b2c73e")
        colorList.add("#3ec7a5")
        colorList.add("#3eb7c7")
        colorList.add("#a73ec7")
        colorList.add("#d966be")
        colorList.add("#f075a4")
        colorList.add("#f5f293")
        return colorList
    }
    private fun setColor(){
        cardDetailsBinding.tvSelectLabelColor.text=""
        cardDetailsBinding.tvSelectLabelColor.setBackgroundColor(Color.parseColor(mSelectedColor))
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_delete_card->{
                alertDialogForDeleteCard(mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].name)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun getIntentData(){
        if (intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails=intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }
        if (intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)){
            mTaskListPosition=intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION,-1)
        }
        if (intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)){
            mCardListPosition=intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION,-1)
        }
        if (intent.hasExtra(Constants.BOARD_MEMBERS_LIST)){
            mMembersDetailList=intent.getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST)!!
        }
    }
    private fun membersListDialog(){
        var cardAssignedMembersList=mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo
        if (cardAssignedMembersList.size > 0){
            for (i in mMembersDetailList.indices){
                for (j in cardAssignedMembersList){
                    if (mMembersDetailList[i].id == j){
                        mMembersDetailList[i].selected=true
                    }
                }
            }
        }else{
            for (i in mMembersDetailList.indices){
                mMembersDetailList[i].selected=false
            }
        }
        val listDialog= object: MembersListDialog(
            this,
            mMembersDetailList,
            "Select Member"
        ){
            override fun onItemSelected(user: User, action: String) {
                if (action==Constants.SELECT){
                    if (!mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo.contains(user.id)){
                        mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo.add(user.id)
                    }
                }else{
                    mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo.remove(user.id)

                    for (i in mMembersDetailList.indices){
                        if (mMembersDetailList[i].id == user.id){
                            mMembersDetailList[i].selected=false
                        }
                    }
                }
                setUpSelectedMembersList()
            }
        }
        listDialog.show()
    }
    private fun updateCardDetails(){
        val card=Card(
            cardDetailsBinding.etNameCardDetails.text.toString(),
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].createdBy,
            mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo,
            mSelectedColor,mSelectedDueDateMilliSeconds
        )
        val taskList:ArrayList<Task> =mBoardDetails.taskList
        taskList.removeAt(taskList.size-1)

        mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition] = card
        mBoardDetails.documentId=intent.getStringExtra(Constants.DOCUMENT_ID)!!
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this@CardDetailsActivity,mBoardDetails)
    }
    private fun deleteCard(){
         val cardsList:ArrayList<Card> =mBoardDetails.taskList[mTaskListPosition].cards
        cardsList.removeAt(mCardListPosition)
        val taskList:ArrayList<Task> =mBoardDetails.taskList
        taskList.removeAt(taskList.size-1)

        taskList[mTaskListPosition].cards = cardsList
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this@CardDetailsActivity,mBoardDetails)
    }
    private fun alertDialogForDeleteCard(cardName:String){
        val builder= AlertDialog.Builder(this)
        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to delete $cardName?")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes"){
                dialogInterface,_ ->
            dialogInterface.dismiss()
            deleteCard()
        }
        builder.setNegativeButton("No"){
                dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog =builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
    private fun labelColorsListDialog(){
        val colorList:ArrayList<String> =colorsList()
        val listDialog= object :LabelColorListDialog(this@CardDetailsActivity,colorList,"Select Label Color"){
            override fun onItemSelected(color: String) {
                mSelectedColor=color
                setColor()
            }
        }
        listDialog.show()
    }
    private fun setUpSelectedMembersList(){
        val cardAssignedMembersList=mBoardDetails.taskList[mTaskListPosition].cards[mCardListPosition].assignedTo
        val selectedMembersList:ArrayList<SelectedMembers> = ArrayList()

        for (i in mMembersDetailList.indices){
            for (j in cardAssignedMembersList){
                if (mMembersDetailList[i].id == j){
                    val selectedMember=SelectedMembers(
                        mMembersDetailList[i].id,
                        mMembersDetailList[i].image
                    )
                    selectedMembersList.add(selectedMember)
                }
            }
        }
        if (selectedMembersList.size > 0){
            selectedMembersList.add(SelectedMembers("",""))
            cardDetailsBinding.tvSelectMembers.visibility=View.GONE
            cardDetailsBinding.rvSelectedMembersList.visibility=View.VISIBLE
            cardDetailsBinding.rvSelectedMembersList.layoutManager= GridLayoutManager(this,6)
            val adapter=CardMemberListItemsAdapter(this,selectedMembersList,true)
            cardDetailsBinding.rvSelectedMembersList.adapter=adapter
            adapter.setOnClickListener(object :CardMemberListItemsAdapter.OnClickListener{
                override fun onClick() {
                    membersListDialog()
                }

            })
        }else{
            cardDetailsBinding.tvSelectMembers.visibility=View.VISIBLE
            cardDetailsBinding.rvSelectedMembersList.visibility=View.GONE
        }
    }
    private fun showDatePicker(){
        val c=Calendar.getInstance()
        val year=c.get(Calendar.YEAR)
        val month=c.get(Calendar.MONTH)
        val day=c.get(Calendar.DAY_OF_MONTH)
        val dpd=DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                val sDayOfMonth= if (dayOfMonth<10) "0$dayOfMonth" else "$dayOfMonth"
                val sMonthOfYear= if ((monthOfYear + 1)<10)"0${monthOfYear+1}" else "${monthOfYear+1}"

                val selectedDate= "$sDayOfMonth/$sMonthOfYear/$year"
                cardDetailsBinding.tvSelectDueDate.text=selectedDate

                val sdf=SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                val theDate=sdf.parse(selectedDate)
                mSelectedDueDateMilliSeconds = theDate!!.time
            },year,month,day
        )
        dpd.show()
    }
}