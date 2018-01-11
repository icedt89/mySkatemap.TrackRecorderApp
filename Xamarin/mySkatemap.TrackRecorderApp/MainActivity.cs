using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Drawing;
using Android.App;
using Android.Content;
using Android.Gms.Maps;
using Android.Gms.Maps.Model;
using Android.Locations;
using Android.OS;
using Android.Support.V4.Widget;
using Android.Util;
using Android.Views;
using Android.Widget;
using Autofac;
using mySkatemap.Apps.TrackRecorder.Infrastructure;
using mySkatemap.Apps.TrackRecorder.Infrastructure.Location;
using Location = Android.Locations.Location;

namespace mySkatemap.Apps.TrackRecorder
{
	[Activity(Label = "mySkatemap.TrackRecorderApp", MainLauncher = true, Icon = "@mipmap/icon")]
	public class MainActivity : Activity, IOnMapReadyCallback
	{
		private GoogleMap trackRecorderMap;

		private BackgroundLocationServiceWorkerConnection backgroundLocationServiceWorkerConnection;

		private readonly IContainer autofacContainer;

		public MainActivity()
		{
			var autofacContainerBuilder = new ContainerBuilder();

			autofacContainerBuilder.Register(_ =>
				{
					var serviceType = typeof(BackgroundLocationServiceWorker);

					Application.Context.StartService(new Intent(Application.Context, serviceType));

					var result = new BackgroundLocationServiceWorkerConnection();

					Application.Context.BindService(new Intent(Application.Context, serviceType), result, Bind.AutoCreate);

					return result;
				})
				.AsSelf()
				.SingleInstance();

			this.autofacContainer = autofacContainerBuilder.Build();
		}

		protected override void OnDestroy()
		{
			base.OnDestroy();

			if (this.backgroundLocationServiceWorkerConnection != null)
			{
				Application.Context.UnbindService(this.backgroundLocationServiceWorkerConnection);
			}

			if (this.backgroundLocationServiceWorkerConnection?.Binder.Service != null)
			{
				this.backgroundLocationServiceWorkerConnection.Binder.Service.Dispose();
			}
		}

		protected override void OnPause()
		{
			base.OnPause();

			Log.Debug(nameof(MainActivity), $"Application suspended");
		}

		protected override void OnResume()
		{
			base.OnResume();

			Log.Debug(nameof(MainActivity), $"Application resumed");
		}

		protected override void OnCreate(Bundle savedInstanceState)
		{
			base.OnCreate(savedInstanceState);

			SetContentView(Resource.Layout.Main);
			
			this.InitializeUiControls();

			this.backgroundLocationServiceWorkerConnection = this.autofacContainer.Resolve<BackgroundLocationServiceWorkerConnection>();
		}

		private void InitializeUiControls()
		{
			var refresher = FindViewById<SwipeRefreshLayout>(Resource.Id.SwipeRefresher);
			refresher.Refresh += RefreshView;

			this.InitializeActionBar();

			this.InitializeMap();
		}

		public override bool OnCreateOptionsMenu(IMenu menu)
		{
			MenuInflater.Inflate(Resource.Menu.TrackRecorderMenu, menu);

			return base.OnCreateOptionsMenu(menu);
		}

		public override bool OnOptionsItemSelected(IMenuItem item)
		{
			if (item.ItemId == Resource.Id.trackrecordermenu_currenttrackrecording_finish)
			{
				this.backgroundLocationServiceWorkerConnection.Binder.Service.StartLocationUpdates();

				//PopupMenu menu = new PopupMenu(this, item.ActionView);
				//menu.Inflate(Resource.Menu.menu);
				//menu.Show();
			}
			else
			{
				Toast.MakeText(this, "Action selected: " + item.TitleFormatted,
					ToastLength.Short).Show();
			}

			return base.OnOptionsItemSelected(item);
		}

		private void InitializeActionBar()
		{
			var toolbar = FindViewById<Toolbar>(Resource.Id.toolbar);
			SetActionBar(toolbar);

			ActionBar.Title = this.Resources.GetString(Resource.String.app_name);
		}

		private void RefreshView(Object sender, EventArgs eventArgs)
		{
			var polylineOptions = new PolylineOptions();

			var polyline = this.trackRecorderMap.AddPolyline(polylineOptions);
		}

		private void InitializeMap()
		{
			var mapFragment = this.FragmentManager.FindFragmentByTag<MapFragment>("map");

			if (mapFragment == null)
			{
				var mapOptions = new GoogleMapOptions()
					.InvokeMapType(GoogleMap.MapTypeNormal)
					.InvokeZoomControlsEnabled(false)
					.InvokeCompassEnabled(false)
					.InvokeMapToolbarEnabled(false)
					.InvokeRotateGesturesEnabled(false)
					.InvokeScrollGesturesEnabled(false)
					.InvokeTiltGesturesEnabled(false)
					.InvokeZoomGesturesEnabled(false)
					.InvokeCamera(CameraPosition.FromLatLngZoom(new LatLng(50.8357, 12.92922), 13));
				using (var fragmentTransaction = FragmentManager.BeginTransaction())
				{
					mapFragment = MapFragment.NewInstance(mapOptions);

					fragmentTransaction.Add(Resource.Id.map, mapFragment, "map");

					fragmentTransaction.Commit();
				}
			}
			mapFragment.GetMapAsync(this);
		}

		public void OnMapReady(GoogleMap googleMap)
		{
			this.trackRecorderMap = googleMap;
		}
	}
}