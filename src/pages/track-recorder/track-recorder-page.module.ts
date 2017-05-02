import { MockedTrackUploader } from "../../infrastructure/track-uploader/mocked-track-uploader";
import { MockedTrackRecorder } from "../../infrastructure/track-recorder/mocked-track-recorder";
import { TrackRecordingStore } from "../../infrastructure/track-store/track-recording-store";
import { ArchivedTrackRecordingStore } from "../../infrastructure/track-store/archived-track-recording-store";
import { TrackRecorderPopoverComponent } from "./track-recorder-popover/track-recorder-popover.component";
import { TrackAttachmentsModalModule } from "../../components/track-attachments-modal/track-attachments-modal.module";
import { MapModule } from "../../components/map/map.module";
import { TrackRecorderSettingsModalModule } from "../../components/track-recorder-settings-modal/track-recorder-settings-modal.module";
import { IonicModule } from "ionic-angular";
import { HttpModule } from "@angular/http";
import { TrackRecorderPageComponent } from "./track-recorder-page.component";
import { NgModule } from "@angular/core";

@NgModule({
    imports: [HttpModule, IonicModule, TrackRecorderSettingsModalModule, TrackAttachmentsModalModule, MapModule],
    declarations: [TrackRecorderPageComponent, TrackRecorderPopoverComponent],
    entryComponents: [TrackRecorderPageComponent, TrackRecorderPopoverComponent],
    exports: [TrackRecorderPageComponent],
    providers: [ArchivedTrackRecordingStore, TrackRecordingStore, {
        provide: "TrackRecorder",
        useClass: MockedTrackRecorder
    }, {
        provide: "TrackUploader",
        useClass: MockedTrackUploader
    }]
})
export class TrackRecorderPageModule { }