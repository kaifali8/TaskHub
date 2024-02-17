package com.example.taskhub.activities

import android.app.Activity
import android.app.Dialog
import android.os.AsyncTask
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskhub.R
import com.example.taskhub.adapters.MemberListItemsAdapter
import com.example.taskhub.databinding.ActivityMembersBinding
import com.example.taskhub.databinding.DialogSearchMemberBinding
import com.example.taskhub.firebase.FirestoreClass
import com.example.taskhub.models.Board
import com.example.taskhub.models.User
import com.example.taskhub.utils.Constants
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class MembersActivity : BaseActivity() {

    private lateinit var mBoardDetails:Board
    private lateinit var mAssignedMembersList:ArrayList<User>
    private var anyChangesMade: Boolean=false
    private lateinit var membersBinding: ActivityMembersBinding
    private lateinit var dialogSearchMemberBinding:DialogSearchMemberBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialogSearchMemberBinding=DialogSearchMemberBinding.inflate(layoutInflater)
        membersBinding=ActivityMembersBinding.inflate(layoutInflater)
        setContentView(membersBinding.root)
        if (intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails=intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }
        setupActionBar()
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAssignedMembersListDetails(this,mBoardDetails.assignedTo)
    }
    fun setupMembersList(list:ArrayList<User>){
        mAssignedMembersList=list
        hideProgressDialog()
        membersBinding.rvMembersList.layoutManager=LinearLayoutManager(this)
        membersBinding.rvMembersList.setHasFixedSize(true)
        val adapter = MemberListItemsAdapter(this,list)
        membersBinding.rvMembersList.adapter=adapter
    }
    fun membersDetails(user: User){
        mBoardDetails.assignedTo.add(user.id)
        mBoardDetails.documentId=intent.getStringExtra(Constants.DOCUMENT_ID)!!
        FirestoreClass().assignMembersToBoard(this,mBoardDetails,user)
    }
    private fun setupActionBar(){
        setSupportActionBar(membersBinding.toolbarMembersActivity)
        val actionBar=supportActionBar
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_back_icon)
            actionBar.title="Members"
        }
        membersBinding.toolbarMembersActivity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_add_member->{
                dialogSearchMember()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun dialogSearchMember(){
        val dialog =Dialog(this)
        dialog.setContentView(dialogSearchMemberBinding.root)
        dialogSearchMemberBinding.tvAdd.setOnClickListener {
            val email= dialogSearchMemberBinding.etEmailSearchMember.text.toString()
            if (email.isNotEmpty()){
                dialog.dismiss()
                showProgressDialog(resources.getString(R.string.please_wait))
                FirestoreClass().getMembersDetails(this,email)
            }else{
                Toast.makeText(this, "Please enter the email address", Toast.LENGTH_SHORT).show()
            }
        }
        dialogSearchMemberBinding.tvCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onBackPressed() {
        if (anyChangesMade){
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }
    fun memberAssignSuccess(user: User){
        hideProgressDialog()
        mAssignedMembersList.add(user)
        anyChangesMade = true

        setupMembersList(mAssignedMembersList)

        SendNotificationToUserAsyncTask(mBoardDetails.name,user.fcmToken).execute()
    }
    private inner class SendNotificationToUserAsyncTask(val boardName:String,val token:String):AsyncTask<Any,Void,String>(){
        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog(resources.getString(R.string.please_wait))
        }
        override fun doInBackground(vararg params: Any?): String {
            var result:String
            var connection:HttpURLConnection?=null
            try {
                val url=URL(Constants.FCM_BASE_URL)
                connection=url.openConnection() as HttpURLConnection
                connection.doOutput=true
                connection.doInput=true
                connection.instanceFollowRedirects=false
                connection.requestMethod="POST"

                connection.setRequestProperty("Content-Type","application/json")
                connection.setRequestProperty("charset","utf-8")
                connection.setRequestProperty("Accept","application/json")

                connection.setRequestProperty(Constants.FCM_AUTHORIZATION,"${Constants.FCM_KEY}=${Constants.FCM_SERVER_KEY}")
                connection.useCaches=false

                val wr=DataOutputStream(connection.outputStream)
                val jsonRequest=JSONObject()
                val dataObject=JSONObject()
                dataObject.put(Constants.FCM_TITLE,"Assigned to the board $boardName")
                dataObject.put(Constants.FCM_MESSAGE,"You have been assigned to the board by ${mAssignedMembersList[0].name}")
                jsonRequest.put(Constants.FCM_DATA,dataObject)
                jsonRequest.put(Constants.FCM_TO,token)
                wr.writeBytes(jsonRequest.toString())
                wr.flush()
                wr.close()
                val httpResult:Int=connection.responseCode
                if (httpResult==HttpURLConnection.HTTP_OK){
                    val inputStream=connection.inputStream
                    val reader=BufferedReader(InputStreamReader(inputStream))
                    val stringBuilder=StringBuilder()
                    var line:String?
                    try {
                        while (reader.readLine().also { line=it }!=null){
                            stringBuilder.append(line + "\n")
                        }
                    }catch (e:IOException){
                        e.printStackTrace()
                    }finally {
                        try {
                            inputStream.close()
                        }catch (e:IOException){
                            e.printStackTrace()
                        }
                    }
                    result=stringBuilder.toString()
                }else{
                    result=connection.responseMessage
                }
            }catch (e:SocketTimeoutException){
                result="Connection Timeout"
            }catch (e:Exception){
                result="Error: "+e.message
            }finally {
                connection?.disconnect()
            }
            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            hideProgressDialog()
        }
    }
}