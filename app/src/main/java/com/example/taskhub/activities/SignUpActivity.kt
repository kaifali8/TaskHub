package com.example.taskhub.activities

import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import com.example.taskhub.R.*
import com.example.taskhub.databinding.ActivitySignUpBinding
import com.example.taskhub.firebase.FirestoreClass
import com.example.taskhub.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignUpActivity : BaseActivity() {
    private lateinit var binding:ActivitySignUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setActionBar()
    }
    private fun setActionBar(){
        setSupportActionBar(binding.tbSignUp)
        val actionBar=supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(drawable.ic_black_back_icon)
        }
        binding.tbSignUp.setNavigationOnClickListener {
            onBackPressed()
        }
        binding.btnSignUpScreen.setOnClickListener {
            registerUser()
        }
    }
    private fun registerUser(){
        val name:String=binding.etName.text.toString().trim{it <= ' '}
        val email:String=binding.etEmailSignUp.text.toString().trim{it <= ' '}
        val password:String=binding.etPasswordSignUp.text.toString().trim{it <= ' '}
        if (validateForm(name, email, password)){
            showProgressDialog(resources.getString(string.please_wait))
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password).addOnCompleteListener{
                task->
                if (task.isSuccessful){
                    val firebaseUser:FirebaseUser=task.result!!.user!!
                    val registeredEmail=firebaseUser.email!!
                    val user= User(firebaseUser.uid,name,registeredEmail)
                    FirestoreClass().registerUser(this,user)
                }else{
                    Toast.makeText(this, "Registration Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun validateForm(name:String,email:String,password:String):Boolean{
        return when{
            TextUtils.isEmpty(name)->{
                showErrorSnackBar("Please enter a name")
                false
            }
            TextUtils.isEmpty(email)->{
                showErrorSnackBar("Please enter the email")
                false
            }
            TextUtils.isEmpty(password)->{
                showErrorSnackBar("Please enter the password")
                false
            }else->{
                true
            }
        }
    }
    fun userRegisteredSuccess(){
        Toast.makeText(this, "You have successfully registered.", Toast.LENGTH_SHORT).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }
}