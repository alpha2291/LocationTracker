package com.alpha.locationtrackerplayer.data.model

import io.github.xilinjia.krdb.types.RealmObject
import io.github.xilinjia.krdb.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class LocationHistory : RealmObject {

    @PrimaryKey
    var _id: ObjectId = ObjectId()

    var userId: String = ""

    var latitude: Double = 0.0

    var longitude: Double = 0.0

    var timestamp: Long = 0L

}