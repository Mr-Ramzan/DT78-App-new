package com.fbiego.dt78.model

open class User{
    var username : String? = ""
    var email : String? = ""
    var age : String? = ""
    var gender : String? = ""
    var profile_pic : String? = ""
    var phone : String? = ""
    var password : String? = ""
    var friends_family : ArrayList<String> =ArrayList()
    var id : String? = ""
    var healthData : ArrayList<HealthRate> = ArrayList()
}
