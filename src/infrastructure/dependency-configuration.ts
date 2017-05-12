import { LocalizationService } from "./localization/localization-service";
import { MockedLocalizationService } from "./localization/mocked-localization-service";
import { TrackRecorder } from "./track-recorder/track-recorder";
import { TrackUploader } from "./track-uploader/track-uploader";
import { MapComponentAccessor } from "../components/map/map-component-accessor";
import { MockedMapComponentAccessor } from "../components/map/mocked-map-component-accessor";
import { MockedTrackUploader } from "./track-uploader/mocked-track-uploader";
import { MockedTrackRecorder } from "./track-recorder/mocked-track-recorder";
export class DependencyConfiguration {
    private static mockAll = false;
    private static mockTrackRecorder = DependencyConfiguration.mockAll ? true : false;
    private static mockTrackUploader = DependencyConfiguration.mockAll ? true : false;
    private static mockMapComponentAccessor = DependencyConfiguration.mockAll ? true : false;
    private static mockLocalizationService = DependencyConfiguration.mockAll ? true : false;

    public static useTrackRecorder = DependencyConfiguration.mockTrackRecorder ? MockedTrackRecorder : TrackRecorder;

    public static useTrackUploader = DependencyConfiguration.mockTrackUploader ? MockedTrackUploader : TrackUploader;

    public static useMapComponentAccessor = DependencyConfiguration.mockMapComponentAccessor ? MockedMapComponentAccessor : MapComponentAccessor;

    public static useLocalizationService = DependencyConfiguration.mockLocalizationService ? MockedLocalizationService : LocalizationService;
}