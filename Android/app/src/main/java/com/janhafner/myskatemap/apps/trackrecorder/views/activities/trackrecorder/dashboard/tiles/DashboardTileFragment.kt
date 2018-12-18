package com.janhafner.myskatemap.apps.trackrecorder.views.activities.trackrecorder.dashboard.tiles

import android.view.View.GONE
import androidx.fragment.app.Fragment
import com.jakewharton.rxbinding2.view.visibility
import com.jakewharton.rxbinding2.widget.text
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_dashboard_tile_default.view.*

internal abstract class DashboardTileFragment : Fragment() {
    private val subscriptions: CompositeDisposable = CompositeDisposable()

    public var presenter: DashboardTileFragmentPresenter? = null
        public set(value) {
            field?.destroy()
            this.subscriptions.clear()

            if(value != null) {
                this.subscribeToPresenter(value)
                value.onResume()

                field = value
            }
        }

    private fun subscribeToPresenter(presenter: DashboardTileFragmentPresenter) {
        if(this.subscriptions.size() > 0) {
            return
        }

        this.view!!.fragment_dashboard_tile_title.text().accept(presenter.title)

        this.subscriptions.addAll(
                presenter.valueChanged
                        .distinctUntilChanged()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this.view!!.fragment_dashboard_tile_value.text()),
                presenter.unitChanged
                        .distinctUntilChanged()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this.view!!.fragment_dashboard_tile_unit.text()),
                presenter.unitChanged
                        .map {
                            !it.isBlank()
                        }
                        .distinctUntilChanged()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this.view!!.fragment_dashboard_tile_unit.visibility(GONE))
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