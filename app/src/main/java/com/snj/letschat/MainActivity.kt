package com.snj.letschat

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.format.DateFormat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.snj.letschat.adapter.ChatFirebaseRecycleAdapter
import com.snj.letschat.model.File
import com.snj.letschat.model.Message
import com.snj.letschat.model.User
import com.snj.letschat.utils.SharedPrefConfigUtils
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText
import java.io.ByteArrayOutputStream
import java.util.*


class MainActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private var mFirebaseAuth: FirebaseAuth? = null
    private var mFirebaseUser: FirebaseUser? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mFirebaseDatabaseReference: DatabaseReference? = null
    private var storage = FirebaseStorage.getInstance()


    private var userModel: User? = null

    private var msgListRecyclerView: RecyclerView? = null
    private var mLinearLayoutManager: LinearLayoutManager? = null
    private var btSendMessage: ImageView? = null
    private var btEmoji: ImageView? = null
    private var edMessage: EmojiconEditText? = null
    private var contentRoot: View? = null
    private var emojIcon: EmojIconActions? = null

    private var filePathImageCamera: java.io.File = java.io.File("demo")


    companion object {

        const val CHAT_REFERENCE = "message"
        const val STORAGE_PATH = "gs://friendlychat-ee637.appspot.com"
        const val STORAGE_FOLDER = "images"


        private const val IMAGE_GALLERY_REQUEST = 1
        private const val IMAGE_CAMERA_REQUEST = 2
        private const val PLACE_PICKER_REQUEST = 3

        val TAG =  "${MainActivity::class.java.name}"

        // Storage Permissions
        private const val REQUEST_EXTERNAL_STORAGE = 1
        private val PERMISSIONS_STORAGE = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build()
        mGoogleApiClient?.connect()
        bindViews()
        checkUserLog()

    }

    override fun onResume() {
        super.onResume()
        if (!mGoogleApiClient?.isConnected!!) {
            mGoogleApiClient?.connect()
        }


    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        val storageRef = storage.getReferenceFromUrl(STORAGE_PATH).child(STORAGE_FOLDER)
        if (requestCode == IMAGE_GALLERY_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                val selectedImageUri = data!!.data
                if (selectedImageUri != null) {
                    sendFileFirebase(storageRef, selectedImageUri)
                } else {
                    //URI IS NULL
                }
            }
        } else if (requestCode == IMAGE_CAMERA_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                if (filePathImageCamera != null && filePathImageCamera!!.exists()) {
                    val imageCameraRef = storageRef.child(filePathImageCamera!!.name + "_camera")
                    sendFileFirebase(imageCameraRef, filePathImageCamera)
                } else {
                    //IS NULL
                }
            }
        } else if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                val place = PlacePicker.getPlace(this, data)
                if (place != null) {
                    val latLng = place?.latLng
                    val mapModel = com.snj.letschat.model.Map("${latLng.latitude}", "${latLng.longitude}")
                    val chatModel = Message(user = userModel, timeStamp = "${Calendar.getInstance().time}", map = mapModel, file = null, id = null, messgage = null)
                    mFirebaseDatabaseReference!!.child(CHAT_REFERENCE).push().setValue(chatModel)
                } else {
                    //PLACE IS NULL
                }
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.sendPhoto -> verifyStoragePermissions()
            R.id.sendPhotoGallery -> photoGalleryIntent()
            R.id.sendLocation -> locationPlacesIntent()
            R.id.sign_out -> signOut()
        }//                photoCameraIntent();

        return super.onOptionsItemSelected(item)
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.d(TAG, "onConnectionFailed:$connectionResult")
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.buttonMessage -> sendMessageFirebase()
        }
    }

    private fun sendFileFirebase(storageReference: StorageReference?, file: Uri) {
        if (storageReference != null) {
            val name = DateFormat.format("yyyy-MM-dd_hhmmss", Date()).toString()
            val imageGalleryRef = storageReference?.child(name + "_gallery")

            val uploadTask = imageGalleryRef.putFile(file)
            uploadTask.addOnFailureListener({ e ->
                Log.e("Upload fail", "onFailure sendFileFirebase " + e.message)
            }).addOnCompleteListener(
                    object : OnCompleteListener<UploadTask.TaskSnapshot> {
                        override fun onComplete(p0: Task<UploadTask.TaskSnapshot>) {
                            imageGalleryRef.downloadUrl.addOnSuccessListener { e ->
                                run {
                                    Log.e("Upload..", "onSuccess sendFileFirebase")
                                    val fileModel = File("img", e.toString(), name, "")
                                    val chatModel = Message(user = userModel, timeStamp = "${Calendar.getInstance().time}", map = null, file = fileModel, id = null, messgage = null)
                                    mFirebaseDatabaseReference!!.child(CHAT_REFERENCE).push().setValue(chatModel)
                                    Log.d("File..", fileModel.toString())
                                }


                            }

                        }
                    }

            )

        } else {
            //IS NULL
        }

    }

    private fun sendFileFirebase(storageReference: StorageReference?, file: java.io.File) {
        if (storageReference != null) {
            val photoURI = FileProvider.getUriForFile(applicationContext,
                    "ibas.provider",
                    file)
            val name = DateFormat.format("yyyy-MM-dd_hhmmss", Date()).toString()
            val imageGalleryRef = storageReference?.child(name)
            var bmp: Bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoURI)
            var baos: ByteArrayOutputStream = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos)
            var data = baos.toByteArray()
            Log.e("Upload..", "Asyntask...")
            val uploadTask = imageGalleryRef.putBytes(data)
            uploadTask.addOnFailureListener({ e ->
                Log.e("Upload fail", "onFailure sendFileFirebase " + e.message)
            }).addOnCompleteListener(
                    object : OnCompleteListener<UploadTask.TaskSnapshot> {
                        override fun onComplete(p0: Task<UploadTask.TaskSnapshot>) {
                            imageGalleryRef.downloadUrl.addOnSuccessListener { e ->
                                run {
                                    Log.e("Upload..", "onSuccess sendFileFirebase")
                                    val fileModel = File("img", e.toString(), name, "")
                                    val chatModel = Message(user = userModel, timeStamp = "${Calendar.getInstance().time}", map = null, file = fileModel, id = null, messgage = null)
                                    mFirebaseDatabaseReference!!.child(CHAT_REFERENCE).push().setValue(chatModel)
                                    Log.d("File..", fileModel.toString())
                                }


                            }

                        }
                    }

            )
        } else {
            Log.d(TAG,"Storage ref is null!")
        }

    }


    private fun locationPlacesIntent() {
        try {
            val builder = PlacePicker.IntentBuilder()
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST)
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }

    }

    private fun photoCameraIntent() {
        val nomeFoto = DateFormat.format("yyyy-MM-dd_hhmmss", Date()).toString()
        filePathImageCamera = java.io.File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), nomeFoto + "camera.jpg")
        val it = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoURI = FileProvider.getUriForFile(this@MainActivity,
                "ibas.provider",
                filePathImageCamera!!)
        it.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        startActivityForResult(it, IMAGE_CAMERA_REQUEST)
    }

    private fun photoGalleryIntent() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture_title)), IMAGE_GALLERY_REQUEST)
    }


    private fun sendMessageFirebase() {
        val model = Message(user = userModel, messgage = edMessage!!.text.toString(), timeStamp = "${Calendar.getInstance().time}", file = null, id = null, map = null)
        mFirebaseDatabaseReference!!.child(CHAT_REFERENCE).push().setValue(model)
        edMessage!!.setText("")
    }


    private fun readMessageFirebase() {

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().reference
        mFirebaseDatabaseReference?.keepSynced(true)
        var options: FirebaseRecyclerOptions<Message> =
                FirebaseRecyclerOptions.Builder<Message>()
                        .setIndexedQuery(mFirebaseDatabaseReference!!.child(CHAT_REFERENCE).orderByKey(), mFirebaseDatabaseReference!!.child(CHAT_REFERENCE), Message::class.java)
//                        .setQuery(teamQuery, Message::class.java)
                        .setLifecycleOwner(this)
                        .build()
        val firebaseAdapter = ChatFirebaseRecycleAdapter(this, options, userModel!!.email!!, userModel!!.name!!)
        firebaseAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                val friendlyMessageCount = firebaseAdapter.itemCount
                val lastVisiblePosition = mLinearLayoutManager!!.findLastCompletelyVisibleItemPosition()
                if (lastVisiblePosition == -1 || positionStart >= friendlyMessageCount - 1 && lastVisiblePosition == positionStart - 1) {
                    msgListRecyclerView!!.scrollToPosition(positionStart)
                }
            }
        })

        msgListRecyclerView!!.layoutManager = mLinearLayoutManager
        msgListRecyclerView!!.adapter = firebaseAdapter

        firebaseAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                val friendlyMessageCount = firebaseAdapter.itemCount
                val lastVisiblePosition = mLinearLayoutManager!!.findLastCompletelyVisibleItemPosition()
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                msgListRecyclerView?.post(Runnable { msgListRecyclerView?.smoothScrollToPosition(firebaseAdapter.itemCount - 1) })

                if (lastVisiblePosition == -1 || positionStart >= friendlyMessageCount - 1 && lastVisiblePosition == positionStart - 1) {

                }
            }
        })



    }


    private fun checkUserLog() {
        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseUser = mFirebaseAuth!!.currentUser
        Log.d("$TAG FirebaseUser", mFirebaseUser.toString())
        if (mFirebaseUser == null) {
            signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            userModel = User(mFirebaseUser!!.uid, mFirebaseUser!!.displayName, mFirebaseUser!!.photoUrl!!.toString(), mFirebaseUser?.email)
            readMessageFirebase()
        }
    }

    private fun bindViews() {
        contentRoot = findViewById<View>(R.id.contentRoot)
        edMessage = findViewById<View>(R.id.editTextMessage) as EmojiconEditText?
        btSendMessage = findViewById<View>(R.id.buttonMessage) as ImageView
        btSendMessage!!.setOnClickListener(this)
        btEmoji = findViewById<View>(R.id.buttonEmoji) as ImageView
        emojIcon = EmojIconActions(this, contentRoot, edMessage, btEmoji)
        emojIcon!!.ShowEmojIcon()
        msgListRecyclerView = findViewById<View>(R.id.messageRecyclerView) as RecyclerView?
        mLinearLayoutManager = LinearLayoutManager(this)
        mLinearLayoutManager!!.stackFromEnd = true
    }

    private fun signOut() {
        SharedPrefConfigUtils.clear(this )
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }


    private fun verifyStoragePermissions() {
        // Check if we have write permission
        val permission = ActivityCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            //No permission
            ActivityCompat.requestPermissions(
                    this@MainActivity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            )
        } else {
            // permission given
            photoCameraIntent()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        when (requestCode) {
            REQUEST_EXTERNAL_STORAGE ->
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    photoCameraIntent()
                }
        }
    }
}