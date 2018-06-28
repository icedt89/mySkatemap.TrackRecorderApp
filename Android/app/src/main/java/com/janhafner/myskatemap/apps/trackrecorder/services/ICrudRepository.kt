package com.janhafner.myskatemap.apps.trackrecorder.services

internal interface ICrudRepository<TDocument> {
    fun getAll() : List<TDocument>

    fun getByIdOrNull(id: String) : TDocument?

    fun save(item: TDocument)

    fun delete(id: String)
}