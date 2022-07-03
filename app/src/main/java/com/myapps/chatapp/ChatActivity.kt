package com.myapps.chatapp

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.myapps.chatapp.adapters.MensajesAdapter
import com.myapps.chatapp.entidades.Group
import com.myapps.chatapp.entidades.Mensaje
import kotlinx.android.synthetic.main.activity_chat.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class ChatActivity : AppCompatActivity() {

    private val PHOTO_SEND: Int = 1

    val messages: ArrayList<Mensaje> = arrayListOf()
    lateinit var adapter: MensajesAdapter

    var groupPos: Int = -1
    private lateinit var group: Group

    private val database = FirebaseDatabase.getInstance()
    private lateinit var ref: DatabaseReference
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private lateinit var storageReference: StorageReference //= storage.getReference("imagenes_chat")

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        getData()
        addingChildEvent()

        // seleccionar foto para enviar
        galleryImageButton.setOnClickListener {
            val i = Intent(Intent.ACTION_GET_CONTENT).setType("image/jpeg")
            i.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            startActivityForResult(Intent.createChooser(i, "Selecciona una foto"), PHOTO_SEND)
        }

        adapter = MensajesAdapter(this, messages)
        mensajesChatListView.adapter = adapter

        nombreChat.text = group.name

        if (group.foto != "") {
            Glide.with(this).load(group.foto).into(fotoPerfilChat)
        }
        if (group.onlyCreatorSendMessages && MainActivity.ID != group.creatorId)
            enviarBtn.isEnabled = false

        // enviando mensaje normal
        enviarBtn.setOnClickListener {
            if (editTextMensaje.text.toString() != "") {
                val hora = getTime()
                ref = database.getReference("All groups/" + group.id + "/messages")
                ref.push().setValue(
                    Mensaje(
                        editTextMensaje.text.toString(),
                        MainActivity.ID,
                        "",
                        "1", hora)
                )
                editTextMensaje.setText("")
            }
        }

        backFromChatBtn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        nombreChat.setOnClickListener {
            startActivity(Intent(this, EditGroupActivity::class.java).putExtra("mygroup pos", groupPos))
        }

    }

    // enviando mensaje de foto
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PHOTO_SEND && resultCode == RESULT_OK) {
            val u: Uri? = data?.data
            storageReference = storage.getReference("imagenes_chat") //imagenes_chat
            ref = database.getReference("All groups/" + group.id + "/images")
            ref.push().setValue(u.toString())
            val fotoReferencia = storageReference.child(u!!.lastPathSegment!!)
            fotoReferencia.putFile(u).addOnSuccessListener(this@ChatActivity) { taskSnapshot ->
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnCompleteListener { task ->
                    val uri = task.result!!
                }
                val hora = getTime()
                val m = Mensaje(
                    "",
                        MainActivity.ID,
                        u.toString(),
                        "2",
                        hora)
                ref = database.getReference("All groups/" + group.id + "/messages")
                ref.push().setValue(m)
                adapter.notifyDataSetChanged()

            }
        }
    }

    private fun getData() {
        groupPos = intent.getIntExtra("mygroup pos", -1)
        group = ChatsFragment.myGroups[groupPos]
    }


    private fun addingChildEvent() {
        ref = database.getReference("All groups/" + group.id + "/messages")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                val m: Mensaje? = dataSnapshot.getValue(Mensaje()::class.java)
                if (m != null) {
                    messages.add(m)
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getTime(): String {
        val date = LocalDateTime.now()
        val formato = DateTimeFormatter.ofPattern("HH:mm")
        return date.format(formato)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}