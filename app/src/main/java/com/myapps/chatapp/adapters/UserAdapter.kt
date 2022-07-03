package com.myapps.chatapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.bumptech.glide.Glide
import com.myapps.chatapp.R
import com.myapps.chatapp.entidades.User
import kotlinx.android.synthetic.main.user.view.*

class UserAdapter (private val mContext: Context, private var users: ArrayList<User>) : ArrayAdapter<User>(mContext, 0, users) {

    @SuppressLint("ViewHolder", "SetTextI18n", "ShowToast", "ResourceType")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layout = LayoutInflater.from(mContext).inflate(R.layout.user, parent, false)


        val user = users[position]

        layout.userNameUser.text = user.name
        if (user.fotoPerfil != "")
            Glide.with(mContext).load(user.fotoPerfil).into(layout.userFotoUser)

        return layout
    }
}