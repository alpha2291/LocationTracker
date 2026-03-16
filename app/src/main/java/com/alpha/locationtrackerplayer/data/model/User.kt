package com.alpha.locationtrackerplayer.data.model

import io.github.xilinjia.krdb.types.RealmObject
import io.github.xilinjia.krdb.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class User : RealmObject {

    @PrimaryKey
    var _id: ObjectId = ObjectId()

    var email: String = ""

    var password: String = ""

    var isLoggedIn: Boolean = false

}