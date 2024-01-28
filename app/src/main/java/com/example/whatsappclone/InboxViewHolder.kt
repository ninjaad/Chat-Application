package com.example.whatsappclone

import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso

class InboxViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(item: Inbox, onClick: (name: String, photo: String, id: String) -> Unit) {
        with(itemView) {
            val countTv = findViewById<TextView>(R.id.countTv)
            countTv.isVisible = item.count > 0
            countTv.text = item.count.toString()

            findViewById<TextView>(R.id.timeTv).text = item.time.formatAsListItem(context)
            findViewById<TextView>(R.id.titleTv).text = item.name
            findViewById<TextView>(R.id.subtitleTv).text = item.msg

            val userImgView= findViewById<ShapeableImageView>(R.id.userImgView)
            Picasso.get().load(item.image).placeholder(R.drawable.defaultavatar).error(R.drawable.defaultavatar).into(userImgView)

            setOnClickListener {
                onClick.invoke(item.name, item.image, item.from)
            }
        }
        //done
        //project
    }
}
