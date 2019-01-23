package com.janhafner.myskatemap.apps.trackrecorder.views.activities.viewfinishedtrack

import android.content.Intent
import android.view.MenuItem
import androidx.core.view.GravityCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.janhafner.myskatemap.apps.trackrecorder.BuildConfig
import com.janhafner.myskatemap.apps.trackrecorder.R
import com.janhafner.myskatemap.apps.trackrecorder.common.Identity
import com.janhafner.myskatemap.apps.trackrecorder.common.types.TrackRecording
import com.janhafner.myskatemap.apps.trackrecorder.views.TabDefinition
import com.janhafner.myskatemap.apps.trackrecorder.views.TabDefinitionTabsAdapter
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.about.AboutActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.appsettings.AppSettingsActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.playground.PlaygroundActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.tracklist.TrackListActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.userprofilesettings.UserProfileSettingsActivity
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.viewfinishedtrack.map.MapTabFragment
import com.janhafner.myskatemap.apps.trackrecorder.views.activities.viewfinishedtrack.overview.OverviewTabFragment
import kotlinx.android.synthetic.main.activity_track_recorder.*
import kotlinx.android.synthetic.main.app_toolbar.*


internal final class ViewFinishedTrackActivityPresenter(private val view: ViewFinishedTrackActivity) {
    private var trackRecording: TrackRecording? = null

    init {
        this.view.setContentView(R.layout.activity_view_finished_track)

        this.view.setSupportActionBar(this.view.app_toolbar)

        val actionBar = this.view.supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_bright_24dp)

        val viewPager = this.view.findViewById<ViewPager>(R.id.viewfinishedtrackactivity_toolbar_viewpager)

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

        val tabLayout = this.view.findViewById<TabLayout>(R.id.viewfinishedtrackactivity_toolbar_tablayout)
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

    private fun initializeWithTrackRecording(trackRecording: TrackRecording): TrackRecording {


        return trackRecording
    }

    public fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            this.view.trackrecorderactivity_navigationdrawer.openDrawer(GravityCompat.START)
        }

        return true
    }

    private fun setupLeftNavigationView() {
        val navigationView = this.view.findViewById<NavigationView>(R.id.viewfinishedtrackactivity_navigation)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            if (menuItem.itemId == R.id.trackrecorderactivity_navigation_drawer_action_user_profile) {
                this@ViewFinishedTrackActivityPresenter.view.startActivity(Intent(this@ViewFinishedTrackActivityPresenter.view, UserProfileSettingsActivity::class.java))
            } else if (menuItem.itemId == R.id.trackrecorderactivity_navigation_drawer_action_settings) {
                this@ViewFinishedTrackActivityPresenter.view.startActivity(Intent(this@ViewFinishedTrackActivityPresenter.view, AppSettingsActivity::class.java))
            } else if (menuItem.itemId == R.id.trackrecorderactivity_navigation_drawer_action_about) {
                this@ViewFinishedTrackActivityPresenter.view.startActivity(Intent(this@ViewFinishedTrackActivityPresenter.view, AboutActivity::class.java))
            } else if (menuItem.itemId == R.id.trackrecorderactivity_navigation_drawer_action_tracklist) {
                this@ViewFinishedTrackActivityPresenter.view.startActivity(Intent(this@ViewFinishedTrackActivityPresenter.view, TrackListActivity::class.java))
            } else if (menuItem.itemId == R.id.trackrecorderactivity_navigation_drawer_action_signin) {
                val googleSignInClient = this@ViewFinishedTrackActivityPresenter.getGoogleSignInClient()

                this@ViewFinishedTrackActivityPresenter.view.startActivityForResult(googleSignInClient.signInIntent, GOOGLE_SIGNIN_REQUEST_CODE)
            } else if (menuItem.itemId == R.id.trackrecorderactivity_navigation_drawer_action_playground) {
                this@ViewFinishedTrackActivityPresenter.view.startActivity(Intent(this@ViewFinishedTrackActivityPresenter.view, PlaygroundActivity::class.java))
            }

            this@ViewFinishedTrackActivityPresenter.view.trackrecorderactivity_navigationdrawer.closeDrawers()

            true
        }
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