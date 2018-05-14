package com.janhafner.myskatemap.apps.trackrecorder.views.activities.tracklist

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.ListView
import android.widget.TextView
import com.janhafner.myskatemap.apps.trackrecorder.ITrackService
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.getApplicationInjector
import io.reactivex.Observable
import javax.inject.Inject

internal final class TrackListActivity : AppCompatActivity() {
    @Inject
    public lateinit var trackService: ITrackService

    public override fun onCreate(savedInstanceState: Bundle?) {
        this.getApplicationInjector().inject(this)

        super.onCreate(savedInstanceState)

        // Presenter
        this.setContentView(R.layout.activity_track_list)

        val trackRecorderToolbar = this.findViewById<Toolbar>(R.id.trackrecorderactivity_toolbar)
        this.setSupportActionBar(trackRecorderToolbar)

        val items = this.trackService.getAllTrackRecordings()

        val itemsObservable = Observable.fromArray(items)

        val itemsAdapter = TrackListItemsAdapter(this)

        itemsAdapter.itemViewCreated.subscribe{
            val textView = it.view.findViewById<TextView>(R.id.info_text)
            textView.text = it.item.name
        }

        val listView = this.findViewById<ListView>(R.id.tracklistactivity_trackrecordings_grid)
        listView.adapter = itemsAdapter

        itemsAdapter.subscribeTo(itemsObservable)
    }
}