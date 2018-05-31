package com.janhafner.myskatemap.apps.trackrecorder.services.calories

internal interface IMetActivityDefinitionFactory {
    fun preload()

    fun getMetActivityDefinitionByCode(code: String) : MetActivityDefinition?
}