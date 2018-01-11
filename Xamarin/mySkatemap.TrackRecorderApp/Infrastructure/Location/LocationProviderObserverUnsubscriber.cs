using System;
using System.Collections.Generic;
using System.Diagnostics;

namespace mySkatemap.Apps.TrackRecorder.Infrastructure.Location
{
	internal sealed class LocationProviderObserverUnsubscriber : IDisposable
	{
		private readonly ISet<IObserver<Location>> observers;

		private readonly IObserver<Location> observer;

		public LocationProviderObserverUnsubscriber(ISet<IObserver<Location>> observers, IObserver<Location> observer)
		{
			this.observers = observers ?? throw new ArgumentNullException(nameof(observers));
			this.observer = observer ?? throw new ArgumentNullException(nameof(observer));
		}

		public void Dispose()
		{
			var removed = this.observers.Remove(this.observer);

			Debug.Assert(removed, $"Observer not removed!");
		}
	}
}