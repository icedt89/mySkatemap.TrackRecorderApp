using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using Android.App;
using Android.Content;
using Android.OS;
using Android.Runtime;
using Android.Views;
using Android.Widget;
using mySkatemap.Apps.TrackRecorder.Infrastructure.Location;

namespace mySkatemap.Apps.TrackRecorder.Infrastructure.TrackRecorder
{
	[Service(Exported = false)]
	internal sealed class TrackRecorderServiceWorker : Service
	{
		private readonly ICollection<Location.Location> locations;

		private IBinder binder;

		public TrackRecorderServiceWorker()
		{
			this.LocationProvider = new DefaultLocationProvider();
		}

		public ILocationProvider LocationProvider { get; }

		public override IBinder OnBind(Intent intent)
		{
			this.binder = this.binder ?? new TrackRecorderServiceWorkerBinder(this);

			return this.binder;
		}

		public override StartCommandResult OnStartCommand(Intent intent, StartCommandFlags flags, Int32 startId)
		{
			return StartCommandResult.Sticky;
		}
	}
}