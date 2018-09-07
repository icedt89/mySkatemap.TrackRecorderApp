package com.janhafner.myskatemap.apps.trackrecorder.services.burnedenergy

internal interface IMetActivityDefinitionFactory {
    fun preload()

    fun getMetActivityDefinitionByCode(code: String) : MetActivityDefinition?
}