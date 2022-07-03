package com.myapps.chatapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.*
import android.widget.ArrayAdapter
import com.bumptech.glide.Glide
import com.myapps.chatapp.MainActivity
import com.myapps.chatapp.R
import com.myapps.chatapp.entidades.Mensaje
import kotlinx.android.synthetic.main.mensajes.view.*


class MensajesAdapter(private val mContext: Context, private var mensajes: ArrayList<Mensaje>) : ArrayAdapter<Mensaje>(mContext, 0, mensajes) {

    @SuppressLint("ViewHolder", "SetTextI18n", "ShowToast")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layout = LayoutInflater.from(mContext).inflate(R.layout.mensajes, parent, false)

        val m = mensajes[position]

        layout.mensajeMensaje.text = m.mensaje
        layout.horaMensaje.text = m.hora

        val fotoPerfil: String

        val user = MainActivity.users.find { it.id == m.userId }
        layout.nombreMensaje.text = user!!.name
        fotoPerfil = user.fotoPerfil
        if (fotoPerfil != "") {
            Glide.with(mContext).load(fotoPerfil).into(layout.fotoPerfilMensaje)
        }


        if (m.typeMensaje.toInt() == 2) { // enviamos foto en mensaje
            layout.fotoMensaje.visibility = View.VISIBLE
            layout.mensajeMensaje.visibility = View.VISIBLE
            Glide.with(mContext).load(m.urlFoto).into(layout.fotoMensaje)

        }
        else if (m.typeMensaje.toInt() == 1) { // se envia solo mensaje
            layout.mensajeMensaje.visibility = View.VISIBLE
        }

        return layout
    }
}