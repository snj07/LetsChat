package com.snj.letschat

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.snj.letschat.utils.CheckNet
import com.snj.letschat.utils.SharedPrefConfigUtils
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity(),
        GoogleApiClient.OnConnectionFailedListener {

    override fun onConnectionFailed(p0: ConnectionResult) {
        showSnackbar("Failed to connect!!")
    }

    companion object {
        const val RC_SIGN_IN: Int = 9001
        val TAG: String = "${LoginActivity::class.java.name}"

        init {
            //here goes static initializer code
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        }
    }

    private var mGoogleApiClient: GoogleApiClient? = null
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)
        FirebaseAuth.getInstance().signOut()

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)


        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().requestIdToken(getString(R.string.default_web_client_id)).build()
        mGoogleApiClient = GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions).build()


        mAuth = FirebaseAuth.getInstance()


        if (SharedPrefConfigUtils.getUserEmailId(this) != "") {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }


//        Auth.GoogleSignInApi.signOut(mGoogleApiClient)
        gmail_signin_button2?.setOnClickListener(View.OnClickListener {
            submit()
        })
        facebook_button.setOnClickListener(View.OnClickListener {
            showSnackbar("Facebook login is not implemented!! Check TechNews Project on my github")
        })

    }

    class doAsync(val handler: () -> Unit) : AsyncTask<Void, Void, Void>() {
        init {
            execute()
        }
        override fun doInBackground(vararg params: Void?): Void? {
            handler()
            return null
        }
    }

    override fun onResume() {
        super.onResume()
//        mGoogleApiClient?.connect()
        doAsync {
            mGoogleApiClient?.blockingConnect()
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(object : ResultCallback<Status> {
                override fun onResult(status: Status) {
                    mAuth?.signOut()

                }

            })
        }
    }

    private fun submit() {
        if (CheckNet.isOnline(applicationContext)) {
            signIn()
        } else {
            showSnackbar(resources.getString(R.string.no_internet))
        }
    }

    override fun onStop() {
        super.onStop()
        mGoogleApiClient?.disconnect()
    }

    private fun signIn() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
        mGoogleApiClient?.connect()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        try {
            // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            if (requestCode == RC_SIGN_IN) {

                val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
                handleGoogle(result)
                Log.d(TAG, "onConnectionFailed: --- " + result.status.toString())
            } else {
                //facebook login
                //  mCallbackManager.onActivityResult(requestCode, resultCode, data)
            }
        } catch (e: Exception) {
            showSnackbar(resources.getString(R.string.error_in_login))
            Log.d("error", e.localizedMessage)
        }

    }

    private fun handleGoogle(result: GoogleSignInResult) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess)
        if (result.isSuccess) {
            // Signed in successfully, show authenticated UI.
            val acct = result.signInAccount

            Log.e(TAG, "display name: " + acct!!.displayName!!)

            val name = acct.displayName
            val email = acct.email
            var personPhotoUrl = ""
            if (acct.photoUrl != null)
                personPhotoUrl = acct.photoUrl!!.toString()
            val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
            Log.d("$TAG token", acct.idToken)
            mAuth!!.signInWithCredential(credential)

                    .addOnCompleteListener(this) { task ->
                        run {
                            if(task.isSuccessful){
                                Log.d(TAG, "signInWithCredential:onComplete: " + task.result.user.displayName)
                                handleSignInResult(name, email, personPhotoUrl)
                            }else{
                                Log.w(TAG, "signInWithCredential", task.exception)
                                showSnackbar("Authentication failed!");
                            }
                        }

                    }
                    .addOnFailureListener(this) { task ->
                        run {
                            Log.w(TAG, "signInWithCredential $task")
                            showSnackbar(resources.getString(R.string.auth_failed))
                        }

                    }


        } else {
            // Signed out, show unauthenticated UI.
            showSnackbar(resources.getString(R.string.login_failed))
        }
    }

    private fun handleSignInResult(personName: String?, email: String?, personPhotoUrl: String) {

        var et = SharedPrefConfigUtils.getSharedPreference(this).edit()

        et.putString(SharedPrefConfigUtils.USER_NAME, personName)
        et.putString(SharedPrefConfigUtils.USER_EMAIL, email)
        et.putString(SharedPrefConfigUtils.USER_IMAGE, personPhotoUrl)
        Log.d(TAG, SharedPrefConfigUtils.USER_EMAIL + "")
        et.commit()

        startActivity(Intent(this, MainActivity::class.java))
        finish()


    }


    private fun showSnackbar(msg: String) {
        val parentLayout = findViewById<View>(android.R.id.content)
        Snackbar.make(parentLayout, msg, Snackbar.LENGTH_LONG)
                .setAction(resources.getText(R.string.close)) { }
                .setActionTextColor(resources.getColor(android.R.color.holo_red_light))
                .show()
    }

}
