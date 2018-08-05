package com.snj.letschat.model

data class User(val id :String, val name:String?, val photo : String?)
{
    constructor() : this("","","")
}