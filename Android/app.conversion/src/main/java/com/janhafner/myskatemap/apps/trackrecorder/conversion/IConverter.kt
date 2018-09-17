package com.janhafner.myskatemap.apps.trackrecorder.conversion

public interface IConverter<TInput : Number, TUnit : Enum<TUnit>, TResult : ConversionResult<TInput, TUnit>> {
    fun convert(value: TInput) : TResult
}
