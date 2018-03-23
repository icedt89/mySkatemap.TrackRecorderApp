package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.io

import android.net.Uri

internal final class ContentInfo(public val displayName: String,
                                 public val size: Long,
                                 public val uri: Uri,
                                 public val mimeType: String)