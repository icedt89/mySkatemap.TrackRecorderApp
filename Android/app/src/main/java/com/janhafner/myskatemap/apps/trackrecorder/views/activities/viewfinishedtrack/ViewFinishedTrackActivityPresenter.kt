package com.janhafner.myskatemap.apps.trackrecorder.views.activities.viewfinishedtrack

import android.content.Intent
import android.view.MenuItem
import android.view.View
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.janhafner.myskatemap.apps.trackrecorder.BuildConfig
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.core.Identity
import com.janhafner.myskatemap.apps.trackrecorder.core.formatDefault
import com.janhafner.myskatemap.apps.trackrecorder.views.TabDefinition
import com.janhafner.myskatemap.apps.trackrecorder.views.TabDefinitionTabsAdapter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.about.AboutActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.appsettings.AppSettingsActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.playground.PlaygroundActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.tracklist.TrackListActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.userprofilesettings.UserProfileSettingsActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.viewfinishedtrack.map.MapTabFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.viewfinishedtrack.overview.OverviewTabFragment
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_view_finished_track.*
import kotlinx.android.synthetic.main.app_toolbar.*


internal final class ViewFinishedTrackActivityPresenter(private val view: ViewFinishedTrackActivity): DrawerLayout.DrawerListener {
    private var navigationDrawersOpened: Boolean = false

    init {
        this.view.setContentView(R.layout.activity_view_finished_track)

        this.view.trackRecordingLoader
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this.view, Lifecycle.Event.ON_DESTROY)))
                .subscribe {
                    if(it.value != null) {
                        this.view.title = this.view.getString(R.string.viewfinishedtrackactivity_title, it.value!!.finishedAt!!.formatDefault())
                    } else {
                        // TODO: FEHLERBEHANDLUNG
                    }
                }
        this.view.setSupportActionBar(this.view.app_toolbar)

        val actionBar = this.view.supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_bright_24dp)

        val viewPager = this.view.viewfinishedtrackactivity_toolbar_viewpager

        val tabDefinitions = listOf(
                TabDefinition("OVERVIEW", {
                    OverviewTabFragment()
                }, 0,null),
                TabDefinition(this.view.getString(R.string.trackrecorderactivity_tab_map_title), {
                    MapTabFragment()
                }, 1,null)
        )
        viewPager.adapter = TabDefinitionTabsAdapter(tabDefinitions, this.view.supportFragmentManager)
        viewPager.offscreenPageLimit = viewPager.adapter!!.count

        val tabLayout = this.view.viewfinishedtrackactivity_toolbar_tablayout
        tabLayout.setupWithViewPager(viewPager)

        for (tabDefinition in tabDefinitions
                .filter {
                    it.iconResource != null || it.customLayoutResource != null
                }
                .sortedBy {
                    it.position
                }) {
            if (tabDefinition.customLayoutResource != null) {
                tabLayout.getTabAt(tabDefinition.position)!!.setCustomView(tabDefinition.customLayoutResource)

            } else if (tabDefinition.iconResource != null) {
                tabLayout.getTabAt(tabDefinition.position)!!.setIcon(tabDefinition.iconResource)
            }
        }

        this.setupLeftNavigationView()
    }

    public fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            this.view.viewfinishedtrackactivity_navigationdrawer.openDrawer(GravityCompat.START)
        }

        return true
    }

    private fun setupLeftNavigationView() {
        this.view.viewfinishedtrackactivity_navigation.setNavigationItemSelectedListener { menuItem ->
            if (menuItem.itemId == R.id.trackrecorderactivity_navigation_drawer_action_user_profile) {
                this.view.startActivity(Intent(this.view, UserProfileSettingsActivity::class.java))
            } else if (menuItem.itemId == R.id.trackrecorderactivity_navigation_drawer_action_settings) {
                this.view.startActivity(Intent(this.view, AppSettingsActivity::class.java))
            } else if (menuItem.itemId == R.id.trackrecorderactivity_navigation_drawer_action_about) {
                this.view.startActivity(Intent(this.view, AboutActivity::class.java))
            } else if (menuItem.itemId == R.id.trackrecorderactivity_navigation_drawer_action_tracklist) {
                this.view.startActivity(Intent(this.view, TrackListActivity::class.java))
            } else if (menuItem.itemId == R.id.trackrecorderactivity_navigation_drawer_action_signin) {
                val googleSignInClient = this.getGoogleSignInClient()

                this.view.startActivityForResult(googleSignInClient.signInIntent, GOOGLE_SIGNIN_REQUEST_CODE)
            } else if (menuItem.itemId == R.id.trackrecorderactivity_navigation_drawer_action_playground) {
                this.view.startActivity(Intent(this.view, PlaygroundActivity::class.java))
            }

            this.view.viewfinishedtrackactivity_navigationdrawer.closeDrawers()

            true
        }

        this.view.viewfinishedtrackactivity_navigationdrawer.addDrawerListener(this)
    }

    public override fun onDrawerStateChanged(newState: Int) {
    }

    public override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
    }

    public override fun onDrawerClosed(drawerView: View) {
        this.navigationDrawersOpened = false
    }

    public override fun onDrawerOpened(drawerView: View) {
        this.navigationDrawersOpened = true
    }

    public fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == GOOGLE_SIGNIN_REQUEST_CODE) {
            val completedTask = GoogleSignIn.getSignedInAccountFromIntent(data)

            val account = completedTask.getResult(ApiException::class.java)

            val displayName = account!!.displayName!!
            val username = account.email!!
            val accessToken = account.idToken!!

            val googleIdentity = Identity(displayName, username, "Google")
            googleIdentity.accessToken = accessToken
        }
    }

    private fun getGoogleSignInClient(): GoogleSignInClient {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(BuildConfig.APP_BACKEND_OAUTH2_CLIENTID)
                .build()

        return GoogleSignIn.getClient(this.view, googleSignInOptions)
    }

    companion object {
        public const val GOOGLE_SIGNIN_REQUEST_CODE: Int = 1

        public const val EXTRA_TRACK_RECORDING_KEY = "track_recording"
    }
}