package com.myapps.chatapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdRequest
import com.google.firebase.database.FirebaseDatabase
import com.myapps.chatapp.ChatsFragment
import com.myapps.chatapp.ExploreFragment
import com.myapps.chatapp.MainActivity
import com.myapps.chatapp.R
import com.myapps.chatapp.entidades.Group
import com.myapps.chatapp.entidades.User
import kotlinx.android.synthetic.main.group.view.*

class GroupsAdapter(private val mContext: Context, private var groups: ArrayList<Group>, private var activity: Int) : ArrayAdapter<Group>(mContext, 0, groups) {

    private val CHATS_FRAGMENT = 0
    private val EXPLORE_FRAGMENT = 1
    private val database = FirebaseDatabase.getInstance()
    @SuppressLint("ResourceType")
    val nPositions = mContext.getString(R.integer.positionsListViewBanner).toInt()

    @SuppressLint("ViewHolder", "SetTextI18n", "ShowToast", "ResourceType")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layout = LayoutInflater.from(mContext).inflate(R.layout.group, parent, false)

        val g: Group = groups[position]

        putBanner(layout, position)


        layout.nombreGroupList.text = g.name

        if (g.foto != "") {
            Glide.with(mContext).load(g.foto).into(layout.fotoGroupList)
        }
        val user = MainActivity.users.find { it.id == g.creatorId }
        if (activity == EXPLORE_FRAGMENT)
            setUsersInGroup(user!!, g, layout)
        else if (activity == CHATS_FRAGMENT)
            layout.membersInGroup.visibility = View.GONE

        layout.groupCreatorName.text = user!!.name
        val maxUsersPerGroup = mContext.getString(R.integer.max_users_in_group).toInt()
        val maxUsersPerGroupSpecial = mContext.getString(R.integer.max_users_in_group_special).toInt()
        if(!user.special && g.numMembers >= maxUsersPerGroup
            || user.special && g.numMembers >= maxUsersPerGroupSpecial
            || (g.maxNumMembers != 0 && g.numMembers >= g.maxNumMembers))
            layout.addOnGroupBtn.visibility = View.GONE

        // for chats fragement
        if (MainActivity.GROUPS_ID.contains(g.id))
            layout.addOnGroupBtn.visibility = View.GONE



        layout.addOnGroupBtn.setOnClickListener {
            val maxGroupsPerUser: Int
            if (MainActivity.SPECIAL)
                maxGroupsPerUser = mContext.getString(R.integer.max_groups_per_user_special).toInt()
            else
                maxGroupsPerUser = mContext.getString(R.integer.max_groups_per_user).toInt()


            if (MainActivity.GROUPS_ID.size < maxGroupsPerUser) {
                MainActivity.GROUPS_ID.add(g.id)
                g.numMembers++
                ChatsFragment.myGroups.add(g)
                ChatsFragment.adapter.notifyDataSetChanged()
                ExploreFragment.groups.removeAt(position)
                ExploreFragment.adapter.notifyDataSetChanged()
                // añadimos el id de grupo al usuario
                val ref = database.getReference("Usuarios/" + MainActivity.ID + "/groupsId/" + g.id)
                ref.setValue(g.id)
                // añadimos 1 al numero de miembros en el grupo
                val ref2 = database.getReference("All groups/" + g.id + "/numMembers")
                ref2.setValue(g.numMembers)
            }
            else {
                if (MainActivity.SPECIAL)
                    Toast.makeText(mContext, "You can only have $maxGroupsPerUser groups", Toast.LENGTH_LONG).show()
                else
                    Toast.makeText(mContext, "You can only have $maxGroupsPerUser groups. Upgrade to premium to have more", Toast.LENGTH_LONG).show()

            }



        }

        // delete a group of mine
        /* layout.doneOnGroupBtn.setOnClickListener { // we are removing a group of ours
            MainActivity.GROUPS_ID.remove(g.id)
            ChatsFragment.myGroups.removeAt(position)
            ChatsFragment.adapter.notifyDataSetChanged()
            val ref = database.getReference("Usuarios/" + MainActivity.ID +
                    "/groupsId/" + (position).toString())
            ref.removeValue()

        }*/

        return layout
    }

    @SuppressLint("ResourceType")
    private fun setUsersInGroup(user: User, group: Group, layout: View) {
        var text = group.numMembers.toString() + "/"
        if (group.maxNumMembers == 0) {
            if (user.special)
                text += layout.context.getString(R.integer.max_users_in_group_special)
            else
                text += layout.context.getString(R.integer.max_users_in_group)
        }
        else {
            text += group.maxNumMembers
        }
        layout.membersInGroup.text = text
    }

    private fun putBanner(layout: View, position: Int) {
        if (activity == EXPLORE_FRAGMENT) {
            if (position != 0 && position % nPositions == 0) {
                layout.bannerInListView.visibility = View.VISIBLE
                val adRequest = AdRequest.Builder().build()
                layout.bannerInListView.loadAd(adRequest)
            }
        }
    }
}