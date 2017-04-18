import { TrackRecorderPopoverComponent } from "./track-recorder-popover/track-recorder-popover.component";
import { TrackAttachmentsModule } from "../../components/track-attachments/track-attachments.module";
import { TrackUploader } from "../../infrastructure/track-uploader";
import { TrackRecorder } from "../../infrastructure/track-recorder/track-recorder";
import { MapModule } from "../../components/map/map.module";
import { TrackRecorderSettingsModule } from "../../components/track-recorder-settings/track-recorder-settings.module";
import { IonicModule } from "ionic-angular";
import { HttpModule } from "@angular/http";
import { TrackRecorderPageComponent } from "./track-recorder-page.component";
import { NgModule } from "@angular/core";

@NgModule({
    imports: [HttpModule, IonicModule, TrackRecorderSettingsModule, TrackAttachmentsModule, MapModule],
    declarations: [TrackRecorderPageComponent, TrackRecorderPopoverComponent],
    entryComponents: [TrackRecorderPageComponent, TrackRecorderPopoverComponent],
    exports: [TrackRecorderPageComponent],
    providers: [TrackUploader, TrackRecorder]
})
export class TrackRecorderPageModule { }