package com.example.taskhub.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.taskhub.R
import com.example.taskhub.adapters.BoardsItemAdapter
import com.example.taskhub.databinding.ActivityMainBinding
import com.example.taskhub.databinding.AppBarMainBinding
import com.example.taskhub.databinding.MainContentBinding
import com.example.taskhub.databinding.NavHeaderMainBinding
import com.example.taskhub.firebase.FirestoreClass
import com.example.taskhub.models.Board
import com.example.taskhub.models.User
import com.example.taskhub.utils.Constants
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var mUserName:String
    private lateinit var mSharedPreferences: SharedPreferences
    private lateinit var mainContentBinding: MainContentBinding
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarMainBinding: AppBarMainBinding
    private lateinit var navHeaderMainBinding: NavHeaderMainBinding

    companion object{
        const val MY_PROFILE_REQUEST_CODE:Int = 11
        const val CREATE_BOARD_REQUEST_CODE:Int=12
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Bind included layouts
        appBarMainBinding = AppBarMainBinding.bind(binding.appBarMain.root)
        mainContentBinding = MainContentBinding.bind(appBarMainBinding.mainContent.root)
        navHeaderMainBinding = NavHeaderMainBinding.bind(binding.navView.getHeaderView(0))
        setupActionBar()
        
        binding.navView.setNavigationItemSelectedListener(this)

        mSharedPreferences=this.getSharedPreferences(Constants.TASKHUB_PREFERENCES,Context.MODE_PRIVATE)
        val tokenUpdated=mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED,false)
        if (tokenUpdated){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().loadUserData(this,true)
        }else{
            FirebaseMessaging.getInstance().token.addOnSuccessListener(this@MainActivity){result->
                updateFcmToken(result)
            }
        }

        FirestoreClass().loadUserData(this,true)
        
        appBarMainBinding.fabCreateBoard.setOnClickListener {
            val intent=Intent(this, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME,mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }
    }
    fun populateBoardsListToUI(boardsList:ArrayList<Board>){
        hideProgressDialog()
        if (boardsList.size>0){
            mainContentBinding.rvBoardList.visibility=View.VISIBLE
            mainContentBinding.tvNoBoardsAvailable.visibility=View.GONE
            mainContentBinding.rvBoardList.layoutManager=LinearLayoutManager(this)
            mainContentBinding.rvBoardList.setHasFixedSize(true)
            val adapter=BoardsItemAdapter(this,boardsList)
            mainContentBinding.rvBoardList.adapter=adapter
            adapter.setOnClickListener(object :BoardsItemAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board) {
                    val intent=Intent(this@MainActivity,TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID,model.documentId)
                    startActivity(intent)
                }
            })
        }else{
            mainContentBinding.rvBoardList.visibility=View.GONE
            mainContentBinding.tvNoBoardsAvailable.visibility=View.VISIBLE
        }
    }
    private fun setupActionBar(){
        setSupportActionBar(appBarMainBinding.toolbarMainActivity)
        appBarMainBinding.toolbarMainActivity.setNavigationIcon(R.drawable.ic_action_navigation_menu)
        appBarMainBinding.toolbarMainActivity.setNavigationOnClickListener {
            toggleDrawer()
        }
    }
    private fun toggleDrawer(){
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)){
           binding.drawerLayout.closeDrawer(GravityCompat.START)
        }else{
           binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }
    fun updateNavigationUserDetails(user: User,readBoardsList:Boolean){
        hideProgressDialog()
        mUserName=user.name
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(navHeaderMainBinding.navUserImage);
        navHeaderMainBinding.tvUsername.text=user.name
        if (readBoardsList){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardsList(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode==Activity.RESULT_OK && requestCode== MY_PROFILE_REQUEST_CODE){
            FirestoreClass().loadUserData(this)
        }else if (resultCode==Activity.RESULT_OK && requestCode== CREATE_BOARD_REQUEST_CODE){
            FirestoreClass().getBoardsList(this)
        } else{
            Log.e("Activity Result:","Cancelled")
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)){
           binding.drawerLayout.closeDrawer(GravityCompat.START)
        }else{
            doubleBackToExit()
        }
        super.onBackPressed()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_my_profile->{
                startActivityForResult(Intent(this, MyProfileActivity::class.java),
                    MY_PROFILE_REQUEST_CODE)
            }
            R.id.nav_sign_out->{
                FirebaseAuth.getInstance().signOut()

                mSharedPreferences.edit().clear().apply()

                val intent=Intent(this,IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
       binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    fun tokenUpdateSuccess(){
        hideProgressDialog()
        val editor:SharedPreferences.Editor=mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED,true)
        editor.apply()
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().loadUserData(this,true)
    }
    private fun updateFcmToken(token:String){
        val userHashMap=HashMap<String,Any>()
        userHashMap[Constants.FCM_TOKEN]=token
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().updateUserProfileData(this,userHashMap)
    }
}