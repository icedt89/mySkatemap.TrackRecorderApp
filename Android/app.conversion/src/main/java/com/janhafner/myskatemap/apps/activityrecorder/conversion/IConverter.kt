package com.janhafner.myskatemap.apps.activityrecorder.conversion

public interface IConverter<TInput : Number, TUnit : Enum<TUnit>, TResult : ConversionResult<TInput, TUnit>> {
    fun convert(value: TInput) : TResult
}
