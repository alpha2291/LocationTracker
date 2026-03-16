package com.alpha.locationtrackerplayer.data.repository


import com.alpha.locationtrackerplayer.data.database.RealmManager
import com.alpha.locationtrackerplayer.data.model.LocationHistory
import io.github.xilinjia.krdb.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocationRepository {

    private val realm = RealmManager.realm

    fun getLocations(userId: String): Flow<List<LocationHistory>> {

        return realm.query<LocationHistory>(
            "userId == $0 SORT(timestamp DESC)",
            userId
        ).asFlow().map { it.list }

    }

}