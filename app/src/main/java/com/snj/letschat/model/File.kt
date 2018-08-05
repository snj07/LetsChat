package com.snj.letschat.model

data class File(val type:String, val url:String, val name: String, val size: String){
    constructor() :this("","","","")
}