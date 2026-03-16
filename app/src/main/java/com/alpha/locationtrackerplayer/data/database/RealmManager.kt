package com.alpha.locationtrackerplayer.data.database

import com.alpha.locationtrackerplayer.data.model.LocationHistory
import com.alpha.locationtrackerplayer.data.model.User
import io.github.xilinjia.krdb.Realm
import io.github.xilinjia.krdb.RealmConfiguration

object RealmManager {

    val realm: Realm by lazy {

        val config = RealmConfiguration.create(
            schema = setOf(
                User::class,
                LocationHistory::class
            )
        )

        Realm.open(config)

    }

}