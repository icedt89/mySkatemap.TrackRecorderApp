import { TrackRecorderPopoverComponent } from "./track-recorder-popover/track-recorder-popover.component";
import { TrackAttachmentsModalModule } from "../../components/track-attachments-modal/track-attachments-modal.module";
import { TrackUploader } from "../../infrastructure/track-uploader";
import { TrackRecorder } from "../../infrastructure/track-recorder/track-recorder";
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
    providers: [TrackUploader, TrackRecorder]
})
export class TrackRecorderPageModule { }