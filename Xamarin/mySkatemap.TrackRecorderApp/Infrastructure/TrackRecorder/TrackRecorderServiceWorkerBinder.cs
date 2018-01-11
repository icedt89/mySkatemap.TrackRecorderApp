using System;
using Android.OS;
using mySkatemap.Apps.TrackRecorder.Infrastructure.Location;

namespace mySkatemap.Apps.TrackRecorder.Infrastructure.TrackRecorder
{
	internal sealed class TrackRecorderServiceWorkerBinder : Binder
	{
		public TrackRecorderServiceWorkerBinder(TrackRecorderServiceWorker service)
		{
			this.Service = service ?? throw new ArgumentNullException(nameof(service));
		}

		public TrackRecorderServiceWorker Service { get; }

		public Boolean IsBound { get; set; }
	}
}