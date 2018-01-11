package com.janhafner.myskatemap.apps.trackrecorder.infrastructure

internal interface IDataStore<T> {
    fun save(data: T);

    fun delete();

    fun getData(): T?;
}