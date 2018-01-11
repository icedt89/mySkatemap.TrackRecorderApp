using System;

namespace mySkatemap.Apps.TrackRecorder.Infrastructure.Location
{
	public sealed class Location
	{
		public Double Latitude { get; set; }

		public Double Longitude { get; set; }

		public String Provider { get; set; }

		public Double? Altitude { get; set; }

		public Double? Bearing { get; set; }

		public Double? Speed { get; set; }

		public DateTime CapturedAt { get; set; }
	}
}