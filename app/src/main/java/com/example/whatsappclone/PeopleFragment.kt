package com.example.whatsappclone

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

private const val DELETED_VIEW_TYPE=1
private const val NORMAL_VIEW_TYPE=2

class PeopleFragment : Fragment() {

    lateinit var mAdapter:FirestorePagingAdapter<User, RecyclerView.ViewHolder>
    val auth by lazy {
        FirebaseAuth.getInstance()
    }
    val database by lazy {
        FirebaseFirestore.getInstance().collection("users")
            .orderBy("name", Query.Direction.ASCENDING)
    }

    override fun onCreateView(
        inflater: LayoutInflater,container:ViewGroup?,
        savedInstanceState: Bundle?
    ): View?{
        setupAdapter()
        return inflater.inflate(R.layout.fragment_chats,container,false)
    }

    private fun setupAdapter() {
         val config = PagingConfig(10,2,false)

        val options = FirestorePagingOptions.Builder<User>()
            .setLifecycleOwner(viewLifecycleOwner)
            .setQuery(database, config, User::class.java)
            .build()

        mAdapter = object :FirestorePagingAdapter<User,RecyclerView.ViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

                return when(viewType){
                    NORMAL_VIEW_TYPE -> UserViewHolder(layoutInflater.inflate(R.layout.list_item,parent,false))
                 else ->EmptyViewHolder(layoutInflater.inflate(R.layout.empty_view, parent,false))
                }

            }


            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: User) {
                if(holder is UserViewHolder)
                {
                    holder.bind(user = model){ name: String, photo: String, id: String ->
                        val intent = Intent(requireContext(),ChatActivity::class.java)
                        intent.putExtra(UID,id)
                        intent.putExtra(NAME,name)
                        intent.putExtra(IMAGE,photo)
                        startActivity(intent)



                    }
                }
                else
                {
                  //Todo - Something
                }
            }

            override fun getItemViewType(position: Int): Int {
                val item =getItem(position)?.toObject(User::class.java)
                return if(auth.uid == item!!.uid)
                {
                    DELETED_VIEW_TYPE
                }
                else
                {
                    NORMAL_VIEW_TYPE
                }
            }


        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView= view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter= mAdapter
        }
    }

}