package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles

import androidx.fragment.app.Fragment
import com.jakewharton.rxbinding2.widget.text
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_dashboard_tile_default.view.*

internal abstract class DashboardTileFragment : Fragment() {
    private var presenter: DashboardTileFragmentPresenter? = null

    private val subscriptions: CompositeDisposable = CompositeDisposable()

    public fun setPresenter(presenter: DashboardTileFragmentPresenter) {
        this.presenter?.destroy()
        this.subscriptions.clear()

        this.subscribeToPresenter(presenter)
        presenter.onResume()

        this.presenter = presenter
    }

    private fun subscribeToPresenter(presenter: DashboardTileFragmentPresenter) {
        if(this.subscriptions.size() > 0) {
            return
        }

        this.subscriptions.addAll(
                presenter.titleChanged
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe (this.view!!.fragment_dashboard_tile_title.text()),
                presenter.valueChanged
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this.view!!.fragment_dashboard_tile_value.text()),
                presenter.unitChanged
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this.view!!.fragment_dashboard_tile_unit.text())
        )
    }

    public override fun onDestroyView() {
        this.presenter?.destroy()

        this.subscriptions.dispose()

        super.onDestroyView()
    }

    public override fun onResume() {
        super.onResume()

        if(this.presenter != null) {
            this.subscribeToPresenter(this.presenter!!)

            this.presenter!!.onResume()
        }
    }

    public override fun onPause() {
        super.onPause()

        this.subscriptions.clear()
        this.presenter!!.onPause()
    }
}