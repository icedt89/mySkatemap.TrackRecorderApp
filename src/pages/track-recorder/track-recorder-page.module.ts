import {
    ShowSavedTrackRecordingModalModule
} from "./show-saved-track-recording-modal/show-saved-track-recording-modal.module";
import { MapComponentAccessor } from "../../components/map/map-component-accessor";
import { MockedMapComponentAccessor } from "../../components/map/mocked-map-component-accessor";
import { TrackRecorder } from "../../infrastructure/track-recorder/track-recorder";
import { MockedTrackUploader } from "../../infrastructure/track-uploader/mocked-track-uploader";
import { TrackUploader } from "../../infrastructure/track-uploader/track-uploader";
import { TrackRecorderPopoverModule } from "./track-recorder-popover/track-recorder-popover.module";
import { TrackRecordingStore } from "../../infrastructure/track-store/track-recording-store";
import { ArchivedTrackRecordingStore } from "../../infrastructure/track-store/archived-track-recording-store";
import { TrackAttachmentsModalModule } from "../../components/track-attachments-modal/track-attachments-modal.module";
import { MapModule } from "../../components/map/map.module";
import { IonicModule } from "ionic-angular";
import { TrackRecorderPageComponent } from "./track-recorder-page.component";
import { NgModule } from "@angular/core";
import { TrackRecorderSettingsModalModule } from "./track-recorder-settings-modal/track-recorder-settings-modal.module";
import { MockedTrackRecorder } from "../../infrastructure/track-recorder/mocked-track-recorder";

@NgModule({
    imports: [
        TrackRecorderSettingsModalModule,
        TrackAttachmentsModalModule,
        TrackRecorderPopoverModule,
        ShowSavedTrackRecordingModalModule,
        MapModule,
        IonicModule
    ],
    declarations: [
        TrackRecorderPageComponent,
    ],
    entryComponents: [
        TrackRecorderPageComponent
    ],
    providers: [
        ArchivedTrackRecordingStore,
        TrackRecordingStore,
        {
            provide: "TrackRecorder",
            // useClass: MockedTrackRecorder
            useClass: TrackRecorder
        },
        {
            provide: "TrackUploader",
            // useClass: MockedTrackUploader
            useClass: TrackUploader
        },
        {
            provide: "MapComponentAccessor",
            // useClass: MockedMapComponentAccessor
            useClass: MapComponentAccessor
        }]
})
export class TrackRecorderPageModule { }