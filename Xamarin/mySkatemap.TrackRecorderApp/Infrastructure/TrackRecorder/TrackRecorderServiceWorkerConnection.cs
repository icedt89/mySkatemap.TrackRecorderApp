using Android.Content;
using Android.OS;

namespace mySkatemap.Apps.TrackRecorder.Infrastructure.TrackRecorder
{
	internal sealed class TrackRecorderServiceWorkerConnection : Java.Lang.Object, IServiceConnection
	{
		public TrackRecorderServiceWorkerBinder Binder { get; set; }

		public TrackRecorderServiceWorkerConnection(TrackRecorderServiceWorkerBinder binder = null)
		{
			if (binder != null)
			{
				this.Binder = binder;
			}
		}

		public void OnServiceConnected(ComponentName name, IBinder service)
		{
			if (service is TrackRecorderServiceWorkerBinder serviceBinder)
			{
				this.Binder = serviceBinder;
				this.Binder.IsBound = true;
			}
		}

		public void OnServiceDisconnected(ComponentName name)
		{
			this.Binder.IsBound = false;
		}
	}
}