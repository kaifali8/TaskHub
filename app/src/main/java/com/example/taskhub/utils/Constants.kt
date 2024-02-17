package com.example.taskhub.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap

object Constants {
    const val USERS:String="users"

    const val BOARDS:String="boards"

    const val IMAGE:String="image"
    const val NAME:String="name"
    const val MOBILE:String="mobile"
    const val ASSIGNED_TO:String="assignedTo"
    const val READ_STORAGE_PERMISSION_CODE=1
    const val PICK_IMAGE_REQUEST_CODE=2
    const val DOCUMENT_ID:String="documentId"
    const val TASK_LIST:String="taskList"
    const val BOARD_DETAIL:String="board_detail"
    const val ID:String="id"
    const val EMAIL: String="email"
    const val BOARD_MEMBERS_LIST:String="board_members_list"
    const val TASKHUB_PREFERENCES="TaskhubPreferences"
    const val FCM_TOKEN_UPDATED="fcmTokenUpdated"
    const val FCM_TOKEN="fcmToken"

    const val TASK_LIST_ITEM_POSITION:String="task_list_item_position"
    const val CARD_LIST_ITEM_POSITION:String="card_list_item_position"
    const val SELECT:String="Select"
    const val UN_SELECT:String="UnSelect"

    const val FCM_BASE_URL:String="https://fcm.googleapis.com/fcm/send"
    const val FCM_AUTHORIZATION:String="authorization"
    const val FCM_KEY:String="key"
    const val FCM_SERVER_KEY:String="AAAAaS8FDV8:APA91bHSjSOG6tWdgLzVHO849ZL2iuLnJb7iDych0WaMNFeHvjy8IniU4uvS7I5-4vvQAIMTtaDYHb8O7uaWpi2nZYqf0aIQ_VpWwgA0QCBDOfH0FbxeGtghrBfPzZQirXQlQxPyuxrp"
    const val FCM_TITLE:String="title"
    const val FCM_DATA:String="data"
    const val FCM_MESSAGE:String="message"
    const val FCM_TO:String="to"


    fun showImageChooser(activity: Activity){
        val galleryIntent= Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }
    fun getFileExtension(activity: Activity,uri: Uri?):String?{
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }
}