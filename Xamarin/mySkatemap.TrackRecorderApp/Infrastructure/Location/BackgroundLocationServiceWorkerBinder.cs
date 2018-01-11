using System;
using Android.OS;

namespace mySkatemap.Apps.TrackRecorder.Infrastructure.Location
{
	internal sealed class BackgroundLocationServiceWorkerBinder : Binder
	{
		public BackgroundLocationServiceWorkerBinder(BackgroundLocationServiceWorker service)
		{
			this.Service = service ?? throw new ArgumentNullException(nameof(service));
		}

		public BackgroundLocationServiceWorker Service { get; }

		public Boolean IsBound { get; set; }
	}
}