package com.janhafner.myskatemap.apps.trackrecorder.conversion

public open class ConversionResult<TOutput : Number, TUnit : Enum<TUnit>>(public val value: TOutput, public val unit: TUnit){
}