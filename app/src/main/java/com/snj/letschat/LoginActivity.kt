package com.snj.letschat

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.snj.letschat.utils.CheckNet
import com.snj.letschat.utils.SharedPrefConfigUtils
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(),
        GoogleApiClient.OnConnectionFailedListener {

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    var mGoogleApiClient: GoogleApiClient? = null
    var mAuth: FirebaseAuth? = null

    val RC_SIGN_IN = 9001
    val TAG: String = javaClass.name

//    var mCallbackManager: CallbackManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)
        super.onCreate(savedInstanceState)
        FirebaseAuth.getInstance().signOut()
        setContentView(R.layout.activity_login)
        ButterKnife.bind(this)

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
        mGoogleApiClient = GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions).build()
        mGoogleApiClient?.connect()

        mAuth = FirebaseAuth.getInstance()
        gmail_signin_button2?.setOnClickListener(View.OnClickListener {
            submit()
        }
        )

    }

    internal fun submit() {
        if (CheckNet.isOnline(applicationContext)) {
            signIn()
        } else {
            // showSnackbar(resources.getString(R.string.no_internet))

        }
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
                // Log.d(TAG, "onConnectionFailed: --- " + result.status.toString())
            } else {

                //facebook login
                //  mCallbackManager.onActivityResult(requestCode, resultCode, data)
            }
        } catch (e: Exception) {

            //  showSnackbar("Error in login!")

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
            handleSignInResult(name, email, personPhotoUrl)


        } else {
            // Signed out, show unauthenticated UI.
            //showSnackbar(resources.getString(R.string.login_failed))
        }
    }

    internal fun handleSignInResult(personName: String?, email: String?, personPhotoUrl: String) {

        //Log.e(TAG, "Name: " + personName + ", email: " + email
        //      + ", Image: " + personPhotoUrl)
        //val user = User()
        //user.setUsername(personName)
        //user.setEmail(email)
        //user.setImage_url(personPhotoUrl)
        //user.setPassword("")

        //appDatabase = AppDatabaseSingleton.getInstance(this)
        var et = SharedPrefConfigUtils.getSharedPreference(this).edit()


        //et.putLong(SharedPrefConfigUtils.USER_ID, user1[0].getUid())
        et.putString(SharedPrefConfigUtils.USER_NAME, personName)
        et.putString(SharedPrefConfigUtils.USER_EMAIL, email)
        et.putString(SharedPrefConfigUtils.USER_IMAGE, personPhotoUrl)
        Log.d(TAG, SharedPrefConfigUtils.USER_EMAIL + "")
        et.commit()
        startActivity(Intent(this, MainActivity::class.java))
        finish()


    }
}
