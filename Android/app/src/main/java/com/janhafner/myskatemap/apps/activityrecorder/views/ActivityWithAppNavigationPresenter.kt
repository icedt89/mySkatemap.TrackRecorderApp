package com.janhafner.myskatemap.apps.activityrecorder.views

import android.content.Context
import android.content.Intent
import android.view.MenuItem
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.jakewharton.rxbinding2.view.clicks
import com.janhafner.myskatemap.apps.activityrecorder.BuildConfig
import com.janhafner.myskatemap.apps.activityrecorder.R
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.about.AboutActivity
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.appsettings.AppSettingsActivity
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.playground.PlaygroundActivity
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.activityhistory.ActivityHistoryActivity
import com.janhafner.myskatemap.apps.activityrecorder.views.activities.userprofilesettings.UserProfileSettingsActivity
import com.uber.autodispose.AutoDispose
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider

internal abstract class ActivityWithAppNavigationPresenter<TActivity : AppCompatActivity>(protected val view: TActivity, @LayoutRes contentViewId: Int) : DrawerLayout.DrawerListener {
    protected var navigationDrawersOpened = false

    protected val app_navigationdrawer: DrawerLayout

    init {
        this.view.setContentView(contentViewId)

        this.app_navigationdrawer = this.view.findViewById(R.id.app_navigationdrawer)

        this.setupAppNavigation()
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

    public fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            this.app_navigationdrawer.openDrawer(GravityCompat.START)
        }

        return true
    }

    public fun onBackPressed(): Boolean {
        val cancelBack = !this.navigationDrawersOpened

        if (this.navigationDrawersOpened) {
            this.app_navigationdrawer.closeDrawers()

            this.navigationDrawersOpened = false
        }

        return cancelBack
    }

    private fun setupAppNavigation() {
        this.app_navigationdrawer.addDrawerListener(this)

        this.view.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.app_navigation_action_user_profile)
                .clicks()
                .doOnDispose {
                    this.app_navigationdrawer.removeDrawerListener(this)
                }
                .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this.view, Lifecycle.Event.ON_DESTROY)))
                .subscribe {
                    this.view.startActivity(Intent(this.view, UserProfileSettingsActivity::class.java))

                    this.app_navigationdrawer.closeDrawers()
                }
        this.view.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.app_navigation_action_settings)
                .clicks()
                .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this.view, Lifecycle.Event.ON_DESTROY)))
                .subscribe {
                    this.view.startActivity(Intent(this.view, AppSettingsActivity::class.java))

                    this.app_navigationdrawer.closeDrawers()
                }
        this.view.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.app_navigation_action_about)
                .clicks()
                .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this.view, Lifecycle.Event.ON_DESTROY)))
                .subscribe {
                    this.view.startActivity(Intent(this.view, AboutActivity::class.java))

                    this.app_navigationdrawer.closeDrawers()
                }
        this.view.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.app_navigation_action_activityhistory)
                .clicks()
                .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this.view, Lifecycle.Event.ON_DESTROY)))
                .subscribe {
                    this.view.startActivity(Intent(this.view, ActivityHistoryActivity::class.java))

                    this.app_navigationdrawer.closeDrawers()
                }
        this.view.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.app_navigation_action_signin)
                .clicks()
                .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this.view, Lifecycle.Event.ON_DESTROY)))
                .subscribe {
                    val googleSignInClient = this.getGoogleSignInClient(this.view)

                    this.view.startActivityForResult(googleSignInClient.signInIntent, GOOGLE_SIGNIN_REQUEST_CODE)

                    this.app_navigationdrawer.closeDrawers()
                }
        this.view.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.app_navigation_action_playground)
                .clicks()
                .`as`(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this.view, Lifecycle.Event.ON_DESTROY)))
                .subscribe {
                    this.view.startActivity(Intent(this.view, PlaygroundActivity::class.java))

                    this.app_navigationdrawer.closeDrawers()
                }
    }

    protected fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(BuildConfig.APP_BACKEND_OAUTH2_CLIENTID)
                .build()

        return GoogleSignIn.getClient(context, googleSignInOptions)
    }

    companion object {
        public const val GOOGLE_SIGNIN_REQUEST_CODE: Int = 1

        public const val ENABLE_LOCATION_SERVICES_REQUEST_CODE: Int = 2
    }
}