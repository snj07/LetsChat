package com.snj.letschat

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.facebook.drawee.backends.pipeline.Fresco
import kotlinx.android.synthetic.main.activity_full_screen.*


class FullScreenActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fresco.initialize(this)
        setContentView(R.layout.activity_full_screen)

        mPhotoDraweeView.setPhotoUri(Uri.parse( intent.getStringExtra("urlPhotoClick")))

//                .placeholder(R.drawable.loading_spinner)

//        Glide.with(this).load(urlPhotoUser).centerCrop().transform(new CircleTransform(this)).override(40,40).into(ivUser);

//        val futureTarget = Glide.with(this)
//                .asBitmap()
//                .load(urlPhotoClick)
//                .submit(640, 640)
//
//        val bitmap = futureTarget.get()


    }
}
