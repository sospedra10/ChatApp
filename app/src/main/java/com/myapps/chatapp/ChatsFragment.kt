package com.myapps.chatapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import com.myapps.chatapp.adapters.GroupsAdapter
import com.myapps.chatapp.entidades.Group
import com.myapps.chatapp.entidades.Mensaje
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.fragment_chats.*
import kotlinx.android.synthetic.main.fragment_chats.view.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class ChatsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    // companion object abajo


    private val database = FirebaseDatabase.getInstance()
    private lateinit var ref: DatabaseReference
    private var done: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        myGroups = arrayListOf()
        addingMyGroupsFromDatabaseAutomatically()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_chats, container, false)
        buttons(root)
        makeAdapter(root)

        return root
    }


    private fun makeAdapter(root: View) {
        adapter = GroupsAdapter(root.context, myGroups, 0)
        root.groupsListView.adapter = adapter
    }

    @SuppressLint("ResourceType", "ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun buttons(root: View) {
        val scaleUp = AnimationUtils.loadAnimation(root.context, R.anim.scale_up)
        val scaleDown = AnimationUtils.loadAnimation(root.context, R.anim.scale_down)

        root.settingsBtn.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> root.settingsBtn.startAnimation(scaleUp)
                MotionEvent.ACTION_UP -> {
                    root.settingsBtn.startAnimation(scaleDown)
                    val leaveLeftAnim = AnimationUtils.loadAnimation(root.context, R.anim.leave_left_animation)
                    mainChatsFragmentsLayout.startAnimation(leaveLeftAnim)
                    Handler().postDelayed( {
                        mainChatsFragmentsLayout.visibility = View.GONE
                        startActivity(Intent(root.context, SettingsActivity::class.java))
                    }, 200)
                }
            }
            true
        }


        root.addGroupBtn.setOnClickListener {
            val maxGroupsPerUser: Int
            if (MainActivity.SPECIAL)
                maxGroupsPerUser = getString(R.integer.max_groups_per_user_special).toInt()
            else
                maxGroupsPerUser = getString(R.integer.max_groups_per_user).toInt()
            if (MainActivity.GROUPS_ID.size < maxGroupsPerUser) {

                root.addGroupBtn.visibility = View.INVISIBLE
                root.plusTextView.visibility = View.INVISIBLE
                root.groupsListView.visibility = View.INVISIBLE
                root.createNewGroupLayout.visibility = View.VISIBLE
                root.newGroupNameEditText.showKeyboard()
                root.newGroupNameEditText.requestFocus()
            }
            else {
                if (MainActivity.SPECIAL)
                    Toast.makeText(root.context, "You can only have $maxGroupsPerUser groups", Toast.LENGTH_LONG).show()
                else
                    Toast.makeText(root.context, "You can only have $maxGroupsPerUser groups. Upgrade to premium to have more", Toast.LENGTH_LONG).show()

            }

        }

        root.newGroupNameEditText.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (root.newGroupNameEditText.text.toString() != "") {

                    ref = database.getReference("All groups")
                    val key = ref.push().key.toString()

                    val g = Group(key, root.newGroupNameEditText.text.toString(), MainActivity.ID, getTotalDate(),
                            "", 1, 0, true, false)

                    MainActivity.GROUPS_ID.add(key)
                    val newRef = database.getReference("Usuarios/" + MainActivity.ID + "/groupsId/" + key)
                    newRef.setValue(key)
                    //modificamos allgroups
                    ref.child(key).setValue(g)

                    root.newGroupNameEditText.setText("")
                    root.createNewGroupLayout.visibility = View.GONE
                    root.addGroupBtn.visibility = View.VISIBLE
                    root.plusTextView.visibility = View.VISIBLE
                    root.groupsListView.visibility = View.VISIBLE
                    root.newGroupNameEditText.hideKeyboard()
                }
                true
            } else false
        })

        root.cancelNewGroupBtn.setOnClickListener {
            root.newGroupNameEditText.setText("")
            root.createNewGroupLayout.visibility = View.GONE
            root.addGroupBtn.visibility = View.VISIBLE
            root.plusTextView.visibility = View.VISIBLE
            root.newGroupNameEditText.hideKeyboard()
        }

        root.groupsListView.setOnItemClickListener { parent, view, position, id ->
            val i = Intent(root.context, ChatActivity::class.java)
            i.putExtra("mygroup pos", position)
            startActivity(i)
        }
    }

    private fun addingMyGroupsFromDatabaseAutomatically() {
        database.getReference("All groups").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                if (MainActivity.GROUPS_ID.contains(dataSnapshot.key) && myGroups.filter { it.id == dataSnapshot.key }.isEmpty()) {
                    val g = getGroupFromDatabase(dataSnapshot)
                    myGroups.add(g)
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun getGroupFromDatabase(dataSnapshot: DataSnapshot): Group {
        val id = dataSnapshot.child("id").getValue(String::class.java).toString()
        val name = dataSnapshot.child("name").getValue(String::class.java).toString()
        val creatorId = dataSnapshot.child("creatorId").getValue(String::class.java).toString()
        val date = dataSnapshot.child("date").getValue(String::class.java).toString()
        val foto = dataSnapshot.child("foto").getValue(String::class.java).toString()
        val n = dataSnapshot.child("numMembers").getValue(Int::class.java).toString().toInt()
        val maxNumMembers = dataSnapshot.child("maxNumMembers").getValue(Int::class.java).toString().toInt()
        val privateSettings = dataSnapshot.child("privateSettings").getValue(Boolean::class.java).toString().toBoolean()
        val onlyCreatorSendMessages = dataSnapshot.child("onlyCreatorSendMessages").getValue(Boolean::class.java).toString().toBoolean()

        val g = Group(id, name, creatorId, date, foto, n, maxNumMembers, privateSettings, onlyCreatorSendMessages,
            arrayListOf(), arrayListOf())
        for (ds in dataSnapshot.child("messages").children) {
            val m: Mensaje? = ds.getValue(Mensaje::class.java)
            g.messages.add(m!!)
        }
        for (ds in dataSnapshot.child("images").children) {
            val i: String = ds.getValue(String::class.java).toString()
            g.images.add(i)
        }
        return g
    }


    private fun View.showKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }
    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getTotalDate(): String {
        val date = LocalDateTime.now()
        val formato = DateTimeFormatter.ofPattern("dd MM yyyy HH mm ss")
        return date.format(formato)
    }

    companion object {
        lateinit var adapter: GroupsAdapter
        var myGroups: ArrayList<Group> = arrayListOf()


        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                ChatsFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}