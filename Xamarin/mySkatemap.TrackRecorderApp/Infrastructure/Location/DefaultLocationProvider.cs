using System;
using Android.App;
using Android.Content;
using Android.Locations;
using Android.OS;

namespace mySkatemap.Apps.TrackRecorder.Infrastructure.Location
{
	internal sealed class DefaultLocationProvider : LocationProvider
	{
		private readonly LocationListenerAdapter locationListenerAdapter;

		private readonly LocationManager locationManager;

		public DefaultLocationProvider()
		{
			this.locationManager = (LocationManager) Application.Context.GetSystemService(Context.LocationService);

			this.locationListenerAdapter = new LocationListenerAdapter(this.OnLocationChanged,
				this.OnProviderDisabled,
				this.OnProviderEnabled,
				this.OnStatusChanged);
		}

		public override void StartLocationUpdates()
		{
			if (this.HasRequestedLocationUpdates)
			{
				return;
			}

			this.locationManager.RequestLocationUpdates(LocationManager.GpsProvider, 0, 0, this.locationListenerAdapter);

			this.HasRequestedLocationUpdates = true;
		}

		public override void StopLocationUpdates()
		{
			if (!this.HasRequestedLocationUpdates)
			{
				return;
			}

			this.locationManager.RemoveUpdates(this.locationListenerAdapter);

			this.HasRequestedLocationUpdates = false;
		}

		public override void RequestLocation()
		{
			this.locationManager.RequestSingleUpdate(LocationManager.GpsProvider, this.locationListenerAdapter,
				Application.Context.MainLooper);
		}

		private void OnLocationChanged(Android.Locations.Location location)
		{
			this.PostLocationUpdate(new Location
			{
				Latitude = location.Latitude
			});
		}

		private void OnProviderDisabled(String provider)
		{
		}

		private void OnProviderEnabled(String provider)
		{
		}

		private void OnStatusChanged(String provider, Availability status, Bundle extras)
		{
		}

		private sealed class LocationListenerAdapter : Java.Lang.Object, ILocationListener
		{
			private readonly Action<Android.Locations.Location> onLocationChanged;

			private readonly Action<String> onProviderDisabled;

			private readonly Action<String> onProviderEnabled;

			private readonly Action<String, Availability, Bundle> onStatusChanged;

			public LocationListenerAdapter(Action<Android.Locations.Location> onLocationChanged = null,
				Action<String> onProviderDisabled = null,
				Action<String> onProviderEnabled = null,
				Action<String, Availability, Bundle> onStatusChanged = null)
			{
				this.onLocationChanged = onLocationChanged;
				this.onProviderDisabled = onProviderDisabled;
				this.onProviderEnabled = onProviderEnabled;
				this.onStatusChanged = onStatusChanged;
			}

			void ILocationListener.OnLocationChanged(Android.Locations.Location location)
			{
				this.onLocationChanged?.Invoke(location);
			}

			void ILocationListener.OnProviderDisabled(string provider)
			{
				this.onProviderDisabled?.Invoke(provider);
			}

			void ILocationListener.OnProviderEnabled(string provider)
			{
				this.onProviderEnabled?.Invoke(provider);
			}

			void ILocationListener.OnStatusChanged(string provider, Availability status, Bundle extras)
			{
				this.onStatusChanged?.Invoke(provider, status, extras);
			}
		}

	}
}