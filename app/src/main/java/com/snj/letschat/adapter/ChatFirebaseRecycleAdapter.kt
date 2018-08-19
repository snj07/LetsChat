package com.snj.letschat.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.snj.letschat.FullScreenActivity
import com.snj.letschat.R
import com.snj.letschat.model.Message
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView


class ChatFirebaseRecycleAdapter : FirebaseRecyclerAdapter<Message, ChatFirebaseRecycleAdapter.ChatViewHolder>{

    companion object {
        const val SEND_MSG = 0
        const val RECV_MSG = 1
        const val SEND_IMG_MSG = 2
        const val RECV_IMG_MSG = 3

    }


    private var userName: String
    private var email: String
    var context: Context

    constructor(context: Context, fbRecycleOption: FirebaseRecyclerOptions<Message>, email: String, username: String) : super(fbRecycleOption) {
        this.userName = username
        this.email = email
        this.context = context
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        Log.d("ViewType", "$viewType--")

        var view: View = when (viewType) {
            RECV_MSG -> LayoutInflater.from(parent.context).inflate(R.layout.item_receive_messge, parent, false)
            SEND_MSG -> LayoutInflater.from(parent.context).inflate(R.layout.item_send_messge, parent, false)
            SEND_IMG_MSG -> LayoutInflater.from(parent.context).inflate(R.layout.item_send_img, parent, false)
            else -> LayoutInflater.from(parent.context).inflate(R.layout.item_receive_img, parent, false)
        }

        return ChatViewHolder(view)

    }

    override fun getItemViewType(position: Int): Int {
        var msg = getItem(position)
        return when {
            msg.map != null -> if (msg.user!!.email == email) {
                SEND_IMG_MSG
            } else {
                RECV_IMG_MSG
            }
            msg.file != null -> if (msg.file!!.type == "img" && msg.user!!.email == email) {
                Log.d("Adapter", userName)
                SEND_IMG_MSG
            } else {
                RECV_IMG_MSG
            }
            msg.user!!.email == email -> SEND_MSG
            else -> RECV_MSG
        }
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int, model: Message) {
        holder.setIvUser(model.user!!.photo!!)
        if (holder.txtMessage != null) {
            holder.txtMessage!!.text = model.messgage
        }
        holder.tvTimestamp.text = model.timeStamp
        if (holder.tvLocation != null)
            holder.tvLocation!!.visibility = View.GONE
        if (model.file != null) {
            if (holder.tvLocation != null)
                holder.tvLocation!!.visibility = View.GONE
            holder.setIvChatPhoto(model.file.url)
        } else if (model.map != null) {
            holder.setIvChatPhoto(local(model.map!!.latitude, model.map!!.longitude))
            if (holder.tvLocation != null)
                holder.tvLocation!!.visibility = View.VISIBLE
        }
    }

    private fun local(latitudeFinal: String, longitudeFinal: String): String {
        return "https://maps.googleapis.com/maps/api/staticmap?center=$latitudeFinal,$longitudeFinal&zoom=18&size=280x280&markers=color:red|$latitudeFinal,$longitudeFinal"
    }

    inner class ChatViewHolder : RecyclerView.ViewHolder, View.OnClickListener {
        override fun onClick(p0: View?) {
            val position = adapterPosition
            val model = getItem(position)
            if (model.map != null) {
                clickImageMapChat(model.map?.latitude!!, model.map?.longitude!!)
            } else {
                clickImageChat(model.file!!.url)
            }
        }

        var tvTimestamp: TextView
        var tvLocation: TextView?
        var txtMessage: EmojiconTextView?
        var ivChatPhoto: ImageView?
        var ivUser: ImageView?

        constructor (itemView: View) : super(itemView) {
            tvTimestamp = itemView.findViewById(R.id.timestamp)
            txtMessage = itemView.findViewById(R.id.txtMessage)
            tvLocation = itemView.findViewById(R.id.tvLocation)
            ivChatPhoto = itemView.findViewById(R.id.img_chat)
            ivUser = itemView.findViewById(R.id.ivUserChat)
        }

        fun setIvUser(urlPhotoUser: String) {
            if (ivUser == null) return
            //  Glide.with(ivUser.context).load(urlPhotoUser).centerCrop().transform(CircleTransform(ivUser.context)).override(40, 40).into(ivUser)
            Glide.with(context)
                    .load(urlPhotoUser)
                    .apply(RequestOptions().circleCrop() )
                    .into(ivUser!!)
        }

        fun setIvChatPhoto(url: String) {
            if (ivChatPhoto == null) return
            Glide.with(context)
                    .load(url)
                    .into(ivChatPhoto!!)

            ivChatPhoto!!.setOnClickListener(this)
        }

        private fun clickImageMapChat( latitude: String, longitude: String) {
            val uri = String.format("geo:%s,%s?z=17&q=%s,%s", latitude, longitude, latitude, longitude)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            context.startActivity(intent)
        }

        private fun clickImageChat( urlPhotoClick: String) {
            val intent = Intent(context, FullScreenActivity::class.java)
            intent.putExtra("urlPhotoClick", urlPhotoClick)
            context.startActivity(intent)
        }

    }

}
