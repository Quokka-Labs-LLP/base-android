package com.example.baseandroid.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.baseandroid.R
import com.example.baseandroid.adapter.UserAdapter.*
import com.example.baseandroid.model.UserResponse

class UserAdapter(private val userList:List<UserResponse>):RecyclerView.Adapter<ViewHolder>() {
    //create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_view_design, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text = userList[position].title
        holder.body.text = userList[position].body

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return userList.size
    }

    // Holds the views for adding text
    class ViewHolder(ItemView:View):RecyclerView.ViewHolder(ItemView){
        val title: TextView = itemView.findViewById(R.id.tvTitle)
        val body: TextView = itemView.findViewById(R.id.tvBody)

    }

}