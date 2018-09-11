package com.janhafner.myskatemap.apps.trackrecorder.export.gpx

import java.io.OutputStream

interface IGpxFileWriter {
    fun writeGpxContent(gpxData: GpxData, stream: OutputStream)
}