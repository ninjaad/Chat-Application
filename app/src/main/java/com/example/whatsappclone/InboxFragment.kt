package com.example.whatsappclone

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query

class InboxFragment : Fragment() {
    private lateinit var mAdapter: FirebaseRecyclerAdapter<Inbox, InboxViewHolder>
    private lateinit var viewManager:RecyclerView.LayoutManager
    private val mDatabase by lazy{
        FirebaseDatabase.getInstance()
    }
    private val auth by lazy{
        FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewManager = WrapContentLinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        setUpAdapter()
        return inflater.inflate(R.layout.fragment_chats,container,false)
    }

    private fun setUpAdapter() {
        val baseQuery: Query =mDatabase.reference.child("chats").child(auth.uid!!)
        val options=FirebaseRecyclerOptions.Builder<Inbox>().setLifecycleOwner(viewLifecycleOwner)
            .setQuery(baseQuery,Inbox::class.java).build()
        //Instantiate adapter
        mAdapter=object:FirebaseRecyclerAdapter<Inbox,InboxViewHolder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InboxViewHolder {
                val inflater=layoutInflater
                return InboxViewHolder(inflater.inflate(R.layout.list_item, parent, false))
            }

            override fun onBindViewHolder(viewHolder: InboxViewHolder, position: Int, inbox: Inbox) {
                viewHolder.bind(inbox) { name: String, photo: String, id: String ->

                    val intent= Intent(requireContext(),ChatActivity::class.java)
                    intent.putExtra(UID,id)
                    intent.putExtra(NAME,name)
                    intent.putExtra(IMAGE,photo)
                    startActivity(intent)

                }
            }

        }
    }
    override fun onStart(){
        super.onStart()
        mAdapter.startListening()
    }
    override fun onStop(){
        super.onStop()
        mAdapter.stopListening()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView=view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.apply{
            setHasFixedSize(true)
            layoutManager=viewManager
            adapter=mAdapter
        }
    }
}