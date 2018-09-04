package com.snj.letschat.model

 data class User(val id :String, val name:String?, val photo : String?, val email: String?)
{
    constructor() : this("","","",email=null)

}