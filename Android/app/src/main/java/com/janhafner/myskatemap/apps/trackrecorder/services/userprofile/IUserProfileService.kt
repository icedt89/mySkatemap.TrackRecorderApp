package com.janhafner.myskatemap.apps.trackrecorder.services.userprofile

internal interface IUserProfileService {
    fun getUserProfileOrDefault() : UserProfile

    fun saveUserProfile(userProfile: UserProfile)
}