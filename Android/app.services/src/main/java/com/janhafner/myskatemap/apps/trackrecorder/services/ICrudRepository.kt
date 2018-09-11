package com.janhafner.myskatemap.apps.trackrecorder.services

public interface ICrudRepository<TDocument> {
    fun getAll() : List<TDocument>

    fun getByIdOrNull(id: String) : TDocument?

    fun save(item: TDocument)

    fun delete(id: String)
}