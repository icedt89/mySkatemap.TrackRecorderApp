using Android.Content;
using Android.OS;

namespace mySkatemap.Apps.TrackRecorder.Infrastructure.Location
{
	internal sealed class BackgroundLocationServiceWorkerConnection : Java.Lang.Object, IServiceConnection
	{
		public BackgroundLocationServiceWorkerBinder Binder { get; set; }

		public BackgroundLocationServiceWorkerConnection(BackgroundLocationServiceWorkerBinder binder = null)
		{
			if (binder != null)
			{
				this.Binder = binder;
			}
		}

		public void OnServiceConnected(ComponentName name, IBinder service)
		{
			if (service is BackgroundLocationServiceWorkerBinder serviceBinder)
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