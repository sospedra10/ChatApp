package com.myapps.chatapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.myapps.chatapp.adapters.UserAdapter
import com.myapps.chatapp.entidades.Group
import com.myapps.chatapp.entidades.User
import kotlinx.android.synthetic.main.activity_edit_group.*


class EditGroupActivity : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private lateinit var storageReference: StorageReference

    private val PHOTO_PERFIL: Int = 2

    private var groupPos: Int = -1
    private lateinit var group: Group
    @SuppressLint("SetTextI18n", "ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_group)

        getData()

        makeAdapter()

        if (group.privateSettings && MainActivity.ID != group.creatorId) {
            privateSettingsCheckBox.isEnabled = false
            onlyCreatorSendMessagesCheckBox.isEnabled = false
            maxNumUsersEditText.isEnabled = false
            setMaxNumUsersTv.isEnabled = false
        }
        if (group.privateSettings)
            privateSettingsCheckBox.isChecked = true
        if (group.onlyCreatorSendMessages)
            onlyCreatorSendMessagesCheckBox.isChecked = true

        if (group.foto != "") {
            Glide.with(this).load(group.foto).into(fotoEditGroup)
        }
        groupNameEditGroupTextView.text = group.name

        maxNumUsersEditText.setText(group.maxNumMembers.toString())

        val user = MainActivity.users.find { it.id == group.creatorId }

        setUsersInGroup(user!!)
        groupCreatorNameEditGroupTv.text = "Created by " + user.name
        buttons(user)
    }

    private fun makeAdapter() {
        val users = MainActivity.users.filter { it.groupsId.contains(group.id) }
        val adapter = UserAdapter(this, users as ArrayList<User>)
        usersInGroupListView.adapter = adapter
    }

    @SuppressLint("ResourceType")
    private fun setUsersInGroup(user: User) {
        var text = group.numMembers.toString() + "/"
        if (group.maxNumMembers == 0) {
            if (user.special)
                text += getString(R.integer.max_users_in_group_special)
            else
                text += getString(R.integer.max_users_in_group)
        }
        else {
            text += group.maxNumMembers
        }
        numMembersEditGroupTv.text = text
    }

    @SuppressLint("ResourceType")
    private fun buttons(user: User) { // creator user
        
        leaveTheGroupLayout.setOnClickListener {
            if (MainActivity.ID == group.creatorId) {// creator is leaving
                if (group.numMembers == 1) {// he is alone
                    // we delete the group
                    val ref = database.getReference("All groups/" + group.id)
                    ref.removeValue()
                    // we delete the group from the users groups list
                    deleteGroupsFromUsers()
                }
                else {
                    // there are more members
                    val users = MainActivity.users.filter { it.groupsId.contains(group.id) }
                    val newAdmin = users.random()
                    var ref = database.getReference("All groups/" + group.id)
                    ref.child("creatorId").setValue(newAdmin.id)
                    ref.child("numMembers").setValue(--group.numMembers)
                    ref = database.getReference("Usuarios/" + MainActivity.ID + "/groupsId/" + group.id)
                    ref.removeValue()
                }
            }
            else { // user is not the creator
                // it cant be possible that there is only 1 member because if not he will be admin
                var ref = database.getReference("All groups/" + group.id)
                ref.child("numMembers").setValue(--group.numMembers)
                ref = database.getReference("Usuarios/" + MainActivity.ID + "/groupsId/" + group.id)
                ref.removeValue()
            }
            MainActivity.GROUPS_ID.remove(group.id)
            startActivity(Intent(this, MainActivity::class.java))
        }
        
        privateSettingsCheckBox.setOnClickListener {
            group.privateSettings = privateSettingsCheckBox.isChecked
        }
        onlyCreatorSendMessagesCheckBox.setOnClickListener {
            group.onlyCreatorSendMessages = onlyCreatorSendMessagesCheckBox.isChecked
        }

        backEditGroupBtn.setOnClickListener {
            // we update changes in group
            val ref = database.getReference("All groups/" + group.id)
            ref.child("privateSettings").setValue(group.privateSettings)
            ref.child("onlyCreatorSendMessages").setValue(group.onlyCreatorSendMessages)
            if (maxNumUsersEditText.text.toString().trim().matches("^[0-9]*$".toRegex()))
                ref.child("maxNumMembers").setValue(maxNumUsersEditText.text.toString().toInt())
            startActivity(Intent(this, ChatActivity::class.java).putExtra("mygroup pos", groupPos))
        }

        groupNameEditGroupTextView.setOnClickListener {
            if (group.privateSettings && MainActivity.ID == group.creatorId || !group.privateSettings) {
                    groupNameEditGroupTextView.visibility = View.GONE
                    groupNameEditGroupEditText.visibility = View.VISIBLE
                    groupNameEditGroupEditText.setText(group.name)
                    groupNameEditGroupEditText.requestFocus()
                    groupNameEditGroupEditText.showKeyboard()
            }
        }

        groupNameEditGroupEditText.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                ChatsFragment.myGroups[groupPos].name = groupNameEditGroupEditText.text.toString()
                group.name = groupNameEditGroupEditText.text.toString()
                updateName()
                groupNameEditGroupEditText.visibility = View.GONE
                groupNameEditGroupTextView.visibility = View.VISIBLE
                groupNameEditGroupTextView.text = group.name
                groupNameEditGroupEditText.hideKeyboard()
                true
            } else false
        })

        maxNumUsersEditText.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                group.maxNumMembers = maxNumUsersEditText.text.toString().toInt()
                val special = getString(R.integer.max_users_in_group_special).toInt()
                val notSpecial = getString(R.integer.max_users_in_group).toInt()

                if (group.maxNumMembers > notSpecial) {
                    if (user.special) {
                        if (group.maxNumMembers > special) {
                            group.maxNumMembers = special
                            maxNumUsersEditText.setText(group.maxNumMembers.toString())
                        }
                    }
                    else {
                        group.maxNumMembers = notSpecial
                        maxNumUsersEditText.setText(group.maxNumMembers.toString())
                    }
                }

                setUsersInGroup(user)
                maxNumUsersEditText.hideKeyboard()
                maxNumUsersEditText.clearFocus()
                true
            } else false
        })

        fotoEditGroup.setOnClickListener {
            if (group.privateSettings && MainActivity.ID == group.creatorId || !group.privateSettings) {
                val i = Intent(Intent.ACTION_GET_CONTENT).setType("image/jpeg")
                i.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                startActivityForResult(Intent.createChooser(i, "Selecciona una foto"), PHOTO_PERFIL)
            }
        }
    }

    private fun deleteGroupsFromUsers() {
        val ref = database.getReference("Usuarios/")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (data in dataSnapshot.children) {
                    //val u: User? = data.getValue(User()::class.java)
                    val userId = data.child("id").getValue(String()::class.java).toString()
                    val userGroups = arrayListOf<String>()
                    for (ds in dataSnapshot.child("groupsId").children) {
                        val i: String = ds.getValue(String::class.java).toString()
                        userGroups.add(i)
                    }
                    if (userGroups.contains(group.id)) {
                        ref.child(userId).child("groupsId").child(group.id)
                        ref.removeValue()
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PHOTO_PERFIL && resultCode == RESULT_OK) {
            val u: Uri? = data?.data
            storageReference = storage.getReference("imagenes_perfil") //imagenes_chat
            val ref = database.getReference("All groups/" + group.id + "/foto")
            ref.setValue(u.toString())
            group.foto = u.toString()
            ChatsFragment.myGroups[groupPos].foto = group.foto
            ChatsFragment.adapter.notifyDataSetChanged()
            Glide.with(this).load(group.foto).into(fotoEditGroup)
            val fotoReferencia = storageReference.child(u!!.lastPathSegment!!)
            fotoReferencia.putFile(u).addOnSuccessListener(this) { taskSnapshot ->
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnCompleteListener { task ->
                    val uri = task.result!!
                }
            }
        }
    }

    private fun getData() {
        groupPos = intent.getIntExtra("mygroup pos", -1)
        group = ChatsFragment.myGroups[groupPos]
    }

    private fun updateName() {
        val ref = database.getReference("All groups/" + group.id + "/name")
        ref.setValue(group.name)
    }

    private fun View.showKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }
    private fun View.hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }


}