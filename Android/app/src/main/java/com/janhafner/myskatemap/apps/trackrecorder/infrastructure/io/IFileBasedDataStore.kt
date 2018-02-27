package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io

internal interface IFileBasedDataStore<T> {
    fun save(data: T)

    fun delete()

    fun getData(): T?
}