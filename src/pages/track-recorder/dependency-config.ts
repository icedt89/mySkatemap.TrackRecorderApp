import { MapComponentAccessor } from "../../components/map/map-component-accessor";
import { MockedMapComponentAccessor } from "../../components/map/mocked-map-component-accessor";
import { TrackUploader } from "../../infrastructure/track-uploader/track-uploader";
import { MockedTrackUploader } from "../../infrastructure/track-uploader/mocked-track-uploader";
import { TrackRecorder } from "../../infrastructure/track-recorder/track-recorder";
import { MockedTrackRecorder } from "../../infrastructure/track-recorder/mocked-track-recorder";

export class DependencyConfig {
    private static mockTrackRecorder = true;
    private static mockTrackUploader = true;
    private static mockMapComponentAccessor = true;

    public static useTrackRecorder = DependencyConfig.mockTrackRecorder ? MockedTrackRecorder : TrackRecorder;

    public static useTrackUploader = DependencyConfig.mockTrackUploader ? MockedTrackUploader : TrackUploader;

    public static useMapComponentAccessor = DependencyConfig.mockMapComponentAccessor ? MockedMapComponentAccessor : MapComponentAccessor;
}