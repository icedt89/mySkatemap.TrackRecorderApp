package com.janhafner.myskatemap.apps.trackrecorder.core.types

import android.os.Parcel
import android.os.Parcelable
import org.joda.time.DateTime
import org.joda.time.Period
import java.util.*

public final class TrackInfo() : Parcelable {
    public var id: UUID = UUID.randomUUID()

    public var displayName: String? = null

    public var distance: Float? = null

    public var recordingTime: Period = Period.ZERO

    public var startedAt: DateTime? = null

    public var activityCode: String? = null

    private constructor(parcel: Parcel) : this() {
        this.id = parcel.readValue(UUID::class.java.classLoader) as UUID
        this.displayName = parcel.readValue(String::class.java.classLoader) as String
        this.distance = parcel.readValue(Float::class.java.classLoader) as? Float
        this.recordingTime = parcel.readValue(Period::class.java.classLoader) as Period
        this.startedAt = parcel.readValue(DateTime::class.java.classLoader) as DateTime
        this.activityCode = parcel.readValue(String::class.java.classLoader) as String
    }

    public override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(this.id)
        parcel.writeValue(this.displayName)
        parcel.writeValue(this.distance)
        parcel.writeValue(this.recordingTime)
        parcel.writeValue(this.startedAt)
        parcel.writeValue(this.activityCode)
    }

    public override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TrackInfo> {
        public override fun createFromParcel(parcel: Parcel): TrackInfo {
            return TrackInfo(parcel)
        }

        public override fun newArray(size: Int): Array<TrackInfo?> {
            return arrayOfNulls(size)
        }
    }
}