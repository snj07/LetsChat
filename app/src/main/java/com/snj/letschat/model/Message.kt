package com.snj.letschat.model
data class Message(val id:String?, val messgage:String?, val timeStamp : String, val user : User?, val file: File?, var map : Map?)
{
    constructor():this("","","",null,null,null)
}