package com.janhafner.myskatemap.apps.activityrecorder.core

public final class PropertyChangedData(public val propertyName: String, public val oldValue: Any?, public val newValue: Any?) {
    public val hasChanged: Boolean
        get() = this.oldValue != this.newValue
}