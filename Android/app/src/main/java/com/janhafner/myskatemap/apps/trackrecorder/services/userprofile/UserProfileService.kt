package com.janhafner.myskatemap.apps.trackrecorder.services.userprofile

import com.couchbase.lite.*
import java.util.*

internal final class UserProfileService(private val couchDb: Database) : IUserProfileService {
    public override fun getUserProfileOrDefault(): UserProfile {
        val result = this.couchDb.getDocument(UserProfileService.DEFAULT_USER_PROFILE_DOCUMENT_ID.toString())
        if (result == null) {
            return UserProfile(UserProfileService.DEFAULT_USER_PROFILE_DOCUMENT_ID)
        }

        return UserProfile.fromCouchDbDocument(result)
    }

    public override fun saveUserProfile(item: UserProfile) {
        val document = item.toCouchDbDocument()

        this.couchDb.save(document)
    }

    companion object {
        private val DEFAULT_USER_PROFILE_DOCUMENT_ID: UUID = UUID.fromString("6f1ad6df-222f-427a-9c21-b85eb30586ee")
    }
}