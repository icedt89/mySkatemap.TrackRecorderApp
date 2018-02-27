package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder

import android.content.Context
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.jakewharton.rxbinding2.view.clicks
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.infrastructure.ViewHolder
import com.janhafner.myskatemap.apps.trackrecorder.location.TrackRecorderServiceState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable


internal final class TrackRecorderActivity: AppCompatActivity() {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    private var optionsMenuSubscriptions: CompositeDisposable? = null

    private val viewHolder: ViewHolder = ViewHolder()

    public lateinit var presenter: ITrackRecorderActivityPresenter

    private var selectedTabFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.setContentView(R.layout.activity_track_recorder)

        val trackRecorderToolbar = this.findViewById<Toolbar>(R.id.trackrecorderactivity_toolbar)
        this.setSupportActionBar(trackRecorderToolbar)

        val viewPager = this.findViewById<ViewPager>(R.id.trackrecorderactivity_toolbar_viewpager)
        viewPager.adapter = TrackRecorderTabsAdapter(this, this.supportFragmentManager)
        viewPager.offscreenPageLimit = viewPager.adapter!!.count

        val tabLayout = this.findViewById<TabLayout>(R.id.trackrecorderactivity_toolbar_tablayout)
        tabLayout.setupWithViewPager(viewPager)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                val self = this@TrackRecorderActivity

                if (self.currentFocus == null) {
                    return
                }

                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(self.currentFocus?.windowToken, 0)
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                this@TrackRecorderActivity.selectedTabFragment = tab!!.customView as Fragment
            }
        })

        this.presenter = TrackRecorderActivityPresenter(this)
        this.presenter.startAndBindService()
    }

    public override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menuInflater.inflate(R.menu.track_recorder_activity_toolbar_menu, menu)

        this.viewHolder.store(R.id.trackrecorderactivity_toolbar_discard_currenttracking, menu.findItem(R.id.trackrecorderactivity_toolbar_discard_currenttracking))
        this.viewHolder.store(R.id.trackrecorderactivity_toolbar_finish_currenttracking, menu.findItem(R.id.trackrecorderactivity_toolbar_finish_currenttracking))

        this.subscribeToOptionsMenu()

        return true
    }

    public override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        this.presenter.trackSessionStateChanged.first(TrackRecorderServiceState.Initializing)!!.subscribe{
            it ->
                if(it != TrackRecorderServiceState.Initializing) {
                    this.presenter.saveCurrentRecording()
                }
        }
    }

    override fun onResume() {
        super.onResume()

        this.subscribeToPresenter()
        this.subscribeToOptionsMenu()
    }

    override fun onPause() {
        super.onPause()

        this.subscriptions.clear()
        this.optionsMenuSubscriptions?.clear()
        this.optionsMenuSubscriptions = null
    }

    override fun onDestroy() {
        super.onDestroy()

       this.presenter.unbindService()

        this.subscriptions.clear()
        this.optionsMenuSubscriptions?.clear()
        this.optionsMenuSubscriptions = null
        this.viewHolder.clear()
    }

    private fun subscribeToPresenter() {
        this.subscriptions.addAll(
                this.presenter.trackSessionStateChanged.observeOn(AndroidSchedulers.mainThread()).subscribe{
                    currentState ->
                    when (currentState) {
                        TrackRecorderServiceState.Running -> {
                            Toast.makeText(this, R.string.trackrecorderactivity_toast_recording_running, Toast.LENGTH_LONG).show()
                        }
                        TrackRecorderServiceState.Paused,
                        TrackRecorderServiceState.LocationServicesUnavailable ->
                            Toast.makeText(this, R.string.trackrecorderactivity_toast_recording_paused, Toast.LENGTH_LONG).show()
                        else -> {
                        }
                    }
                }
        )
    }

    private fun subscribeToOptionsMenu() {
        val finishCurrentTrackRecordingMenuItem = this.viewHolder.tryRetrieve<MenuItem>(R.id.trackrecorderactivity_toolbar_finish_currenttracking)
        val discardCurrentTrackRecordingMenuItem = this.viewHolder.tryRetrieve<MenuItem>(R.id.trackrecorderactivity_toolbar_discard_currenttracking)

        if(this.optionsMenuSubscriptions != null ||
                (finishCurrentTrackRecordingMenuItem == null
                      || discardCurrentTrackRecordingMenuItem == null)) {
            return
        }

        this.optionsMenuSubscriptions = CompositeDisposable()
        this.optionsMenuSubscriptions!!.addAll(
                this.presenter.canFinishRecordingChanged.observeOn(AndroidSchedulers.mainThread()).subscribe{ finishCurrentTrackRecordingMenuItem.isEnabled = it },
                finishCurrentTrackRecordingMenuItem.clicks().subscribe({
                    this.presenter.finishRecording()
                }),

                this.presenter.canDiscardRecordingChanged.observeOn(AndroidSchedulers.mainThread()).subscribe{ discardCurrentTrackRecordingMenuItem.isEnabled = it},
                discardCurrentTrackRecordingMenuItem.clicks().subscribe({
                    this.presenter.discardRecording()
                })
        )
    }
}