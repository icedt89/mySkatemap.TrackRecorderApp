import { TrackRecordingStore } from "../../infrastructure/track-store/track-recording-store";
import { ArchivedTrackRecordingStore } from "../../infrastructure/track-store/archived-track-recording-store";
import { TrackRecorderPopoverComponent } from "./track-recorder-popover/track-recorder-popover.component";
import { TrackAttachmentsModalModule } from "../../components/track-attachments-modal/track-attachments-modal.module";
import { MapModule } from "../../components/map/map.module";
import { TrackRecorderSettingsModalModule } from "../../components/track-recorder-settings-modal/track-recorder-settings-modal.module";
import { IonicModule } from "ionic-angular";
import { TrackRecorderPageComponent } from "./track-recorder-page.component";
import { NgModule } from "@angular/core";
import { DependencyConfiguration } from "../../infrastructure/dependency-configuration";

@NgModule({
    imports: [
        TrackRecorderSettingsModalModule,
        TrackAttachmentsModalModule,
        MapModule,
        IonicModule
    ],
    declarations: [
        TrackRecorderPageComponent,
        TrackRecorderPopoverComponent
    ],
    entryComponents: [
        TrackRecorderPopoverComponent,
        TrackRecorderPageComponent
    ],
    providers: [
        ArchivedTrackRecordingStore,
        TrackRecordingStore,
        {
            provide: "TrackRecorder",
            useClass: DependencyConfiguration.useTrackRecorder
        },
        {
            provide: "TrackUploader",
            useClass: DependencyConfiguration.useTrackUploader
        },
        {
            provide: "MapComponentAccessor",
            useClass: DependencyConfiguration.useMapComponentAccessor
        }]
})
export class TrackRecorderPageModule { }