using Android.App;
using Android.Gms.Location;

namespace mySkatemap.Apps.TrackRecorder.Infrastructure.Location
{
	internal sealed class FusedLocationProvider : LocationProvider
	{
		private readonly FusedLocationProviderClient fusedLocationProviderClient;

		private readonly LocationCallback locationCallback;

		public FusedLocationProvider()
		{
			this.fusedLocationProviderClient = LocationServices.GetFusedLocationProviderClient(Application.Context);

			this.locationCallback = new LocationCallback();
			this.locationCallback.LocationResult += (sender, args) =>
			{
				foreach (var location in args.Result.Locations)
				{
					this.PostLocationUpdate(new Location
					{
						Latitude = location.Latitude
					});
				}
			};
		}

		public override void StartLocationUpdates()
		{
			if (this.HasRequestedLocationUpdates)
			{
				return;
			}

			var locationRequest = new LocationRequest();
			locationRequest.SetPriority(LocationRequest.PriorityHighAccuracy);
			locationRequest.SetInterval(8000);
			locationRequest.SetFastestInterval(4000);

			this.fusedLocationProviderClient.RequestLocationUpdatesAsync(locationRequest, this.locationCallback);

			this.HasRequestedLocationUpdates = true;
		}

		public override void StopLocationUpdates()
		{
			if (!this.HasRequestedLocationUpdates)
			{
				return;
			}

			this.fusedLocationProviderClient.RemoveLocationUpdatesAsync(this.locationCallback);

			this.HasRequestedLocationUpdates = false;
		}

		public override void RequestLocation()
		{
			var lastLocation = this.fusedLocationProviderClient.GetLastLocationAsync().Result;

			this.PostLocationUpdate(new Location
			{
				Latitude = lastLocation.Latitude
			});
		}
	}
}