package com.example.whatsappclone

import java.util.Date

data class Inbox (
    val msg:String,
    var from:String,
    var name:String,
    var image:String,
    val time:Date = Date(),
    var count:Int
){

    constructor():this("","","","",Date(),0)
}
