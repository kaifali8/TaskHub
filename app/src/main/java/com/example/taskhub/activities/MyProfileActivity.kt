package com.example.taskhub.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.taskhub.R
import com.example.taskhub.databinding.ActivityMyProfileBinding
import com.example.taskhub.firebase.FirestoreClass
import com.example.taskhub.models.User
import com.example.taskhub.utils.Constants
import com.example.taskhub.utils.Constants.PICK_IMAGE_REQUEST_CODE
import com.example.taskhub.utils.Constants.READ_STORAGE_PERMISSION_CODE
import com.example.taskhub.utils.Constants.getFileExtension
import com.example.taskhub.utils.Constants.showImageChooser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class MyProfileActivity : BaseActivity() {
    private var mSelectedImageFileUri: Uri?=null
    private lateinit var mUserDetails:User
    private var mProfileImageUrl:String=""
    private lateinit var binding:ActivityMyProfileBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActionBar()
        FirestoreClass().loadUserData(this)
        binding.ivProfileUserImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED){
                showImageChooser(this)
            }else{
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                , READ_STORAGE_PERMISSION_CODE)
            }
        }
        binding.btnUpdate.setOnClickListener {
            if (mSelectedImageFileUri!=null){
                uploadUserImage()
            }else{
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode== READ_STORAGE_PERMISSION_CODE){
            if (grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                showImageChooser(this)
            }
        }else{
            Toast.makeText(this,
                "You have denied the permission for storage. You can allow it from the settings",
                Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode== PICK_IMAGE_REQUEST_CODE && data!!.data != null ){
            mSelectedImageFileUri=data.data
            try {
                Glide
                    .with(this@MyProfileActivity )
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(binding.ivProfileUserImage);
            }catch (e:IOException){
                e.printStackTrace()
            }
        }
    }
    private fun setupActionBar(){
        setSupportActionBar(binding.toolbarMyProfileActivity)
        val actionBar=supportActionBar
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_back_icon)
            actionBar.title="My Profile"
        }
        binding.toolbarMyProfileActivity.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    fun setUserDataInUI(user: User){
        mUserDetails=user
        Glide
            .with(this@MyProfileActivity )
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(binding.ivProfileUserImage);
        binding.etName.setText(user.name)
        binding.etEmail.setText(user.email)
        if (user.mobile != null){
            binding.etMobile.setText(user.mobile.toString())
        }
    }
    private fun updateUserProfileData(){
        val userHashMap=HashMap<String,Any>()
        var anyChangesMade=false
        if (mProfileImageUrl.isNotEmpty() && mProfileImageUrl!=mUserDetails.image){
            userHashMap[Constants.IMAGE]=mProfileImageUrl
            anyChangesMade=true
        }
        if (binding.etName.text.toString() != mUserDetails.name){
            userHashMap[Constants.NAME]=binding.etName.text.toString()
            anyChangesMade=true
        }
        if (binding.etMobile.text.toString() != mUserDetails.mobile.toString()){
            userHashMap[Constants.MOBILE]=binding.etMobile.text.toString().toLong()
            anyChangesMade=true
        }
        if (anyChangesMade){
            FirestoreClass().updateUserProfileData(this,userHashMap)
        }

    }
    private fun uploadUserImage(){
        showProgressDialog(resources.getString(R.string.please_wait))
        if (mSelectedImageFileUri != null){
            val sRef:StorageReference=FirebaseStorage.getInstance().reference.child(
                "USER_IMAGE"+System.currentTimeMillis()+getFileExtension(this,mSelectedImageFileUri))
            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                taskSnapshot->
                Log.i("Firebase Image URL",taskSnapshot.metadata!!.reference!!.downloadUrl.toString())
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri->
                    Log.i("Downloadable Image URL",uri.toString())
                    mProfileImageUrl=uri.toString()

                    updateUserProfileData()
                }
            }.addOnFailureListener {
                exception->
                Toast.makeText(this@MyProfileActivity, exception.message, Toast.LENGTH_LONG).show()
            }
            hideProgressDialog()
        }
    }

    fun profileUpdateSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }
}