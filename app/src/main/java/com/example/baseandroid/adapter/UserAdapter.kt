package com.example.baseandroid.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.baseandroid.R
import com.example.baseandroid.adapter.UserAdapter.*
import com.example.baseandroid.databinding.ActivityMainBinding
import com.example.baseandroid.databinding.CardViewDesignBinding
import com.example.baseandroid.model.UserResponse

class UserAdapter(private val context: Context) : RecyclerView.Adapter<ViewHolder>() {
    private var userList = listOf<UserResponse>()
    fun setUserList(userList : List<UserResponse>){
        this.userList = userList
        notifyDataSetChanged()
    }
    //create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val inflater = LayoutInflater.from(parent.context)
        val binding = CardViewDesignBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.tvTitle.text = userList[position].title
        holder.binding.tvBody.text = userList[position].body
        Glide.with(context)
            .load("your_image_url")
            .apply(RequestOptions().placeholder(R.drawable.ic_placeholder))
            .into(holder.binding.ivPhoto)

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return userList.size
    }

    // Holds the views for adding text
    class ViewHolder(val binding: CardViewDesignBinding) : RecyclerView.ViewHolder(binding.root)

}