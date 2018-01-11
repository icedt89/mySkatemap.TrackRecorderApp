using System;

namespace mySkatemap.Apps.TrackRecorder.Infrastructure.Location
{
	internal interface ILocationProvider : IDisposable, IObservable<Location>
	{
		void StartLocationUpdates();

		void StopLocationUpdates();

		void RequestLocation();

		Boolean HasRequestedLocationUpdates { get; }

		IObservable<Location> LocationUpdates { get; }
	}
}