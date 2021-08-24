package com.fbiego.dt78.model

import com.fbiego.dt78.data.HeartData
import com.fbiego.dt78.data.OxygenData
import com.fbiego.dt78.data.PressureData

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
    var heartData : ArrayList<HeartData> = ArrayList()
    var oxygenData : ArrayList<OxygenData> = ArrayList()
    var pressureData : ArrayList<PressureData> = ArrayList()


}
