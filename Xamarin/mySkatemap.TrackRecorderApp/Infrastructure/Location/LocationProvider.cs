using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;

namespace mySkatemap.Apps.TrackRecorder.Infrastructure.Location
{
	internal abstract class LocationProvider : ILocationProvider, IDisposable
	{
		private readonly ISet<IObserver<Location>> observableLocations;

		protected LocationProvider()
		{
			this.observableLocations = new HashSet<IObserver<Location>>();
		}

		public abstract void StartLocationUpdates();

		public abstract void StopLocationUpdates();

		public abstract void RequestLocation();

		public Boolean HasRequestedLocationUpdates { get; protected set; }

		public IObservable<Location> LocationUpdates
		{
			get { return this; }
		}

		public virtual void Dispose()
		{
			this.observableLocations.Clear();
		}

		protected void PostLocationUpdate(Location location)
		{
			foreach (var observableLocation in this.observableLocations.ToList())
			{
				observableLocation.OnNext(location);
			}
		}

		IDisposable IObservable<Location>.Subscribe(IObserver<Location> observer)
		{
			if (observer == null)
			{
				throw new ArgumentNullException(nameof(observer));
			}

			var added = this.observableLocations.Add(observer);

			Debug.Assert(added, $"Observer not added!");

			return new LocationProviderObserverUnsubscriber(this.observableLocations, observer);
		}
	}
}