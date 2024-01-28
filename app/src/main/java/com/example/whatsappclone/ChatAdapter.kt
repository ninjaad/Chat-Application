package com.example.whatsappclone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView


class ChatAdapter(private val list: MutableList<ChatEvent>, private val mCurrentUid: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var highFiveClick: ((id: String, status: Boolean) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflate = { layout: Int ->
            LayoutInflater.from(parent.context).inflate(layout, parent, false)
        }
        return when (viewType) {
            TEXT_MESSAGE_RECEIVED -> {
                MessageViewHolder(inflate(R.layout.list_item_chat_recv_message))
            }
            TEXT_MESSAGE_SENT -> {
                MessageViewHolder(inflate(R.layout.list_item_chat_sent_message))
            }
            DATE_HEADER -> {
                DateViewHolder(inflate(R.layout.list_item_date_header))
            }
            else -> MessageViewHolder(inflate(R.layout.list_item_chat_recv_message))
        }
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = list[position]) {
            is DateHeader -> {
                holder.itemView.findViewById<TextView>(R.id.textView)?.text = item.date
            }
            is Message -> {
                val contentTextView = holder.itemView.findViewById<TextView>(R.id.content)
                contentTextView?.text = item.msg

                val timeTextView = holder.itemView.findViewById<TextView>(R.id.time)
                timeTextView?.text = item.sentAt.formatAsTime()

                when (getItemViewType(position)) {
                    TEXT_MESSAGE_RECEIVED -> {
                        val cardView = holder.itemView.findViewById<MaterialCardView>(R.id.messageCardView)
                        cardView?.setOnClickListener(object : DoubleClickListener() {
                            override fun onDoubleClick(v: View?) {
                                highFiveClick?.invoke(item.msgId,  !item.liked)
                            }
                        })

                        val highFiveImg = holder.itemView.findViewById<ImageView>(R.id.highFiveImg)
                        highFiveImg?.apply {
                            isVisible = position == itemCount - 1 || item.liked
                            isSelected = item.liked
                            setOnClickListener {
                                highFiveClick?.invoke(item.msgId, !isSelected)
                            }
                        }
                    }
                    TEXT_MESSAGE_SENT -> {
                        val highFiveImg = holder.itemView.findViewById<ImageView>(R.id.highFiveImg)
                        highFiveImg?.apply {
                            isVisible = item.liked
                        }
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (val event = list[position]) {
            is Message -> {
                if (event.senderId == mCurrentUid) {
                    TEXT_MESSAGE_SENT
                } else {
                    TEXT_MESSAGE_RECEIVED
                }
            }
            is DateHeader -> DATE_HEADER
            else -> UNSUPPORTED
        }
    }

    class DateViewHolder(view: View) : RecyclerView.ViewHolder(view)
    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view)

    companion object {
        private const val UNSUPPORTED = -1
        private const val TEXT_MESSAGE_RECEIVED = 0
        private const val TEXT_MESSAGE_SENT = 1
        private const val DATE_HEADER = 2
    }
}

abstract class DoubleClickListener : View.OnClickListener {
    var lastClickTime: Long = 0

    override fun onClick(v: View?) {
        val clickTime = System.currentTimeMillis()
        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            onDoubleClick(v)
            lastClickTime = 0
        }
//        else {
//            onSingleClick(v)
//        }
        lastClickTime = clickTime
    }

    //    abstract fun onSingleClick(v: View?)
    abstract fun onDoubleClick(v: View?)

    companion object {
        private const val DOUBLE_CLICK_TIME_DELTA: Long = 300 //milliseconds
    }
}
