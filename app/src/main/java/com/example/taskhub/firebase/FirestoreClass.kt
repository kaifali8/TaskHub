package com.example.taskhub.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.taskhub.activities.CardDetailsActivity
import com.example.taskhub.activities.CreateBoardActivity
import com.example.taskhub.activities.MyProfileActivity
import com.example.taskhub.activities.MainActivity
import com.example.taskhub.activities.MembersActivity
import com.example.taskhub.activities.SignInActivity
import com.example.taskhub.activities.SignUpActivity
import com.example.taskhub.activities.TaskListActivity
import com.example.taskhub.models.Board
import com.example.taskhub.models.User
import com.example.taskhub.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreClass {
    private val mFireStore = FirebaseFirestore.getInstance()

    /**
     * A function to make an entry of the registered user in the firestore database.
     */
    fun registerUser(activity: SignUpActivity, userInfo: User) {

        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(
                    activity.javaClass.simpleName,
                    "Error writing document",
                    e
                )
            }
    }
    fun getBoardDetails(activity: TaskListActivity,documentId:String){
        mFireStore.collection(Constants.BOARDS)
            .document(documentId)
            .get()
            .addOnSuccessListener {
                    document->
                Log.i(activity.javaClass.simpleName,document.toString())
                val board=document.toObject(Board::class.java)!!
                board.documentId=document.id
                activity.boardDetails(document.toObject(Board::class.java)!!)
            }.addOnFailureListener {e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error while creating the board",e)
            }
    }
    fun createBoard(activity: CreateBoardActivity, board: Board){
        mFireStore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Document created successfully")
                Toast.makeText(activity, "Board created successfully", Toast.LENGTH_SHORT).show()
                activity.boardCreatedSuccessfully()
            }
            .addOnFailureListener {exception->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error creating document", exception)
            }
    }
    fun getBoardsList(activity: MainActivity){
        mFireStore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO,getCurrentUserID())
            .get()
            .addOnSuccessListener {
                document->
                Log.i(activity.javaClass.simpleName,document.documents.toString())
                val boardsList:ArrayList<Board> = ArrayList()
                for (i in document.documents){
                    val board=i.toObject(Board::class.java)!!
                    board.documentId=i.id
                    boardsList.add(board)
                }
                activity.populateBoardsListToUI(boardsList)
            }.addOnFailureListener {e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error while creating the board",e)
            }
    }
    fun addUpdateTaskList(activity: Activity,board: Board){
        val taskListHashMap=HashMap<String,Any>()
        taskListHashMap[Constants.TASK_LIST]=board.taskList
        if (board.documentId.isNotEmpty()){
            mFireStore.collection(Constants.BOARDS)
                .document(board.documentId)
                .update(taskListHashMap)
                .addOnSuccessListener {
                    Toast.makeText(activity, "TaskList updated successfully", Toast.LENGTH_SHORT).show()
                    Log.i(activity.javaClass.simpleName,"TaskList Updated Successfully")
                    if (activity is TaskListActivity)
                        activity.addUpdateTaskListSuccess()
                    else if (activity is CardDetailsActivity)
                        activity.addUpdateTaskListSuccess()
                }.addOnFailureListener { exception->
                    if (activity is TaskListActivity){
                        activity.hideProgressDialog()
                        Toast.makeText(activity, "Error creating the board", Toast.LENGTH_SHORT).show()
                        Toast.makeText(activity, exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                    else if (activity is CardDetailsActivity){
                        activity.hideProgressDialog()
                        Toast.makeText(activity, "Error creating the board", Toast.LENGTH_SHORT).show()
                    }
                    Log.e(activity.javaClass.simpleName,"Error while creating the board",exception)
                }
        }else{
            Toast.makeText(activity, "Document Id is empty", Toast.LENGTH_SHORT).show()
        }

    }
    fun updateUserProfileData(activity: Activity, userHashMap: HashMap<String,Any>){
        mFireStore.collection(Constants.USERS).document(getCurrentUserID())
            .update(userHashMap).addOnSuccessListener {
                Log.i(activity.javaClass.simpleName,"Profile data updated successfully!")
                Toast.makeText(activity, "Profile updated successfully!", Toast.LENGTH_LONG).show()
                when(activity){
                    is MainActivity->{
                        activity.tokenUpdateSuccess()
                    }
                    is MyProfileActivity->{
                        activity.profileUpdateSuccess()
                    }
                }
            }.addOnFailureListener {
                e->
                when(activity){
                    is MainActivity->{
                        activity.hideProgressDialog()
                    }
                    is MyProfileActivity->{
                        activity.hideProgressDialog()
                    }
                }

                Log.e(activity.javaClass.simpleName,"Error while creating a board",e)
                Toast.makeText(activity, "Error when updating the profile", Toast.LENGTH_SHORT).show()
            }
    }
    /**
     * A function to SignIn using firebase and get the user details from Firestore Database.
     */
    fun loadUserData(activity: Activity,readBoardsList:Boolean=false) {

        // Here we pass the collection name from which we wants the data.
        mFireStore.collection(Constants.USERS)
            // The document id to get the Fields of user.
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                Log.e(
                    activity.javaClass.simpleName, document.toString()
                )
                val loggedInUser = document.toObject(User::class.java)!!
                when(activity){
                    is SignInActivity->{
                        activity.signInSuccess(loggedInUser)
                    }
                    is MainActivity->{
                        activity.updateNavigationUserDetails(loggedInUser,readBoardsList)
                    }
                    is MyProfileActivity ->{
                        activity.setUserDataInUI(loggedInUser)
                    }
                }
            }
            .addOnFailureListener {e ->
                when(activity){
                    is SignInActivity->{
                        activity.hideProgressDialog()
                    }
                    is MainActivity->{
                        activity.hideProgressDialog()
                    }
                    is MyProfileActivity ->{
                        activity.hideProgressDialog()
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting loggedIn user details",
                    e
                )
            }
    }

    /**
     * A function for getting the user id of current logged user.
     */
    fun getCurrentUserID(): String {
        // An Instance of currentUser using FirebaseAuth
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }
        return currentUserID
    }
    fun getAssignedMembersListDetails(activity: Activity,assignedTo:ArrayList<String>){
        mFireStore.collection(Constants.USERS)
            .whereIn(Constants.ID,assignedTo)
            .get()
            .addOnSuccessListener {document->
                Log.i(activity.javaClass.simpleName,document.documents.toString())
                val usersList:ArrayList<User> =ArrayList()
                for (i in document.documents){
                    val user= i.toObject(User::class.java)!!
                    usersList.add(user)
                }
                if (activity is MembersActivity)
                    activity.setupMembersList(usersList)
                else if (activity is TaskListActivity)
                    activity.boardMembersDetailList(usersList)
            }.addOnFailureListener {e->
                if (activity is MembersActivity)
                    activity.hideProgressDialog()
                else if (activity is TaskListActivity)
                    activity.hideProgressDialog()

                Log.e(activity.javaClass.simpleName,
                    "Error while adding members", e)
            }
    }
    fun getMembersDetails(activity: MembersActivity,email:String){
        mFireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL,email)
            .get()
            .addOnSuccessListener {document->
                if (document.size() > 0){
                    val user=document.documents[0].toObject(User::class.java)!!
                    activity.membersDetails(user)
                }else{
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("No such member found")
                }
            }.addOnFailureListener {e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,
                    "Error while getting user details", e)
            }
    }
    fun assignMembersToBoard(activity: MembersActivity,board: Board,user: User){
        val assignedToHashMap=HashMap<String,Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo
        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                activity.memberAssignSuccess(user)
            }.addOnFailureListener {e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error while updating member", e)
            }
    }
}