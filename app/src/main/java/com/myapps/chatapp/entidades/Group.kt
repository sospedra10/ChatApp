package com.myapps.chatapp.entidades

import kotlin.collections.ArrayList

class Group(var id: String = "", var name: String="", var creatorId: String="", var date: String="",
    var foto: String="", var numMembers: Int = 1, var maxNumMembers: Int = 0, var privateSettings:Boolean = true,
            var onlyCreatorSendMessages: Boolean = false,
            var messages: ArrayList<Mensaje> = arrayListOf(), var images: ArrayList<String> = arrayListOf())

