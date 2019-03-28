package com.janhafner.myskatemap.apps.activityrecorder.views.activities.viewfinishedactivity

import android.content.Intent
import androidx.lifecycle.Lifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.janhafner.myskatemap.apps.activityrecorder.R
import com.janhafner.myskatemap.apps.activityrecorder.core.Identity
import com.janhafner.myskatemap.apps.activityrecorder.core.formatDefault
import com.janhafner.myskatemap.apps.activityrecorder.views.ActivityWithAppNavigationPresenter
import com.janhafner.myskatemap.apps.activityrecorder.views.TabDefinition
import com.janhafner.myskatemap.apps.activityrecorder.views.TabDefinitionTabsAdapter
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.viewfinishedactivity.map.MapTabFragment
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.viewfinishedactivity.overview.OverviewTabFragment
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.app_toolbar.*
import kotlinx.android.synthetic.main.viewfinishedactivity_activity.*


internal final class ViewFinishedActivityActivityPresenter(view: ViewFinishedActivityActivity): ActivityWithAppNavigationPresenter<ViewFinishedActivityActivity>(view, R.layout.viewfinishedactivity_activity) {
    init {
        this.view.activityLoader
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this.view, Lifecycle.Event.ON_DESTROY)))
                .subscribe {
                    if(it.value != null) {
                        this.view.title = this.view.getString(R.string.viewfinishedactivityactivity_title, it.value!!.finishedAt!!.formatDefault())
                    } else {
                        // TODO: FEHLERBEHANDLUNG
                    }
                }
        this.view.setSupportActionBar(this.view.app_toolbar)

        val actionBar = this.view.supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_24dp)

        val viewPager = this.view.viewfinishedtrackactivity_toolbar_viewpager

        val tabDefinitions = listOf(
                TabDefinition("OVERVIEW", {
                    OverviewTabFragment()
                }, 0,null),
                TabDefinition("MAP", {
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

    companion object {
        public const val EXTRA_ACTIVITY_KEY = "track_recording"
    }
}