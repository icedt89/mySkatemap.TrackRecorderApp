package com.janhafner.myskatemap.apps.trackrecorder.infrastructure.nfc

import android.nfc.NdefMessage
import android.nfc.NdefRecord.createMime
import android.nfc.NfcAdapter
import android.nfc.NfcAdapter.CreateNdefMessageCallback
import android.nfc.NfcEvent
import android.support.v7.app.AppCompatActivity
import okio.Buffer
import java.security.InvalidParameterException

internal interface INdefPayloadSource{
    fun createPayload(): Buffer
}

internal final class NearFieldCommunicator(private val ndefPayloadSource: AppCompatActivity,
                                                                         private val nfcAdapter: NfcAdapter?) : CreateNdefMessageCallback {
    init {
        if(ndefPayloadSource !is INdefPayloadSource) {
            throw InvalidParameterException()
        }
    }

    public val isNfcAvailable: Boolean
        get() = this.nfcAdapter != null

    public val isNfcEnabled: Boolean
        get() = this.isNfcAvailable && this.nfcAdapter!!.isEnabled

    public override fun createNdefMessage(event: NfcEvent?): NdefMessage {
        val payload = (this.ndefPayloadSource as INdefPayloadSource).createPayload()

        return NdefMessage(createMime("application/vnd.com.example.android.beam", payload.readByteArray()))
    }

    public fun bindCallback() {
        this.nfcAdapter!!.setNdefPushMessageCallback(this, ndefPayloadSource)
    }
}