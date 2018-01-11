using System;
using Android.App;
using Android.Content;
using Android.Graphics.Drawables;
using Android.OS;

namespace mySkatemap.Apps.TrackRecorder.Infrastructure.Location
{
	[Service(Exported = false)]
	internal sealed class BackgroundLocationServiceWorker : Service
	{
		private IBinder binder;

		private const Int32 BackgroundLocationServiceWorkerRunningNotification = 9000;

		public BackgroundLocationServiceWorker()
		{
			this.LocationProvider = new FusedLocationProvider();
		}

		public ILocationProvider LocationProvider { get; }

		public override IBinder OnBind(Intent intent)
		{
			this.binder = this.binder ?? new BackgroundLocationServiceWorkerBinder(this);

			return this.binder;
		}

		public void StartLocationUpdates()
		{
			var backgroundLocationTrackingOngoingNotification = new Notification.Builder(this)
				.SetSmallIcon(Resource.Drawable.notification_action_background)
				.SetContentTitle("mySkatemap Streckenaufnahme")
				.SetContentText("Strecke wird aufgenommen.");

			var not = backgroundLocationTrackingOngoingNotification.Build();
			not.Flags |= NotificationFlags.NoClear | NotificationFlags.OngoingEvent;

			var notificationManager = (NotificationManager)GetSystemService(NotificationService);
			notificationManager.Notify(BackgroundLocationServiceWorkerRunningNotification, not);

			this.LocationProvider.StartLocationUpdates();
		}

		public void StopLocationUpdates()
		{
			var notificationManager = (NotificationManager)GetSystemService(NotificationService);
			notificationManager.Cancel(BackgroundLocationServiceWorkerRunningNotification);

			this.LocationProvider.StopLocationUpdates();
		}

		protected override void Dispose(bool disposing)
		{
			base.Dispose(disposing);

			this.StopLocationUpdates();
			this.LocationProvider.Dispose();
		}

		public override StartCommandResult OnStartCommand(Intent intent, StartCommandFlags flags, Int32 startId)
		{
			return StartCommandResult.Sticky;
		}
	}
}