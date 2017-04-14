import { TrackUploader } from "../../infrastructure/track-uploader";
import { TrackRecorder } from "../../infrastructure/track-recorder/track-recorder";
import { MapModule } from "../../components/map/map.module";
import { TrackRecorderSettingsModule } from "../../components/track-recorder-settings/track-recorder-settings.module";
import { IonicModule } from "ionic-angular";
import { HttpModule } from "@angular/http";
import { TrackRecorderPageComponent } from "./track-recorder-page.component";
import { NgModule } from "@angular/core";

@NgModule({
    imports: [HttpModule, IonicModule, TrackRecorderSettingsModule, MapModule],
    declarations: [TrackRecorderPageComponent],
    entryComponents: [TrackRecorderPageComponent],
    exports: [TrackRecorderPageComponent],
    providers: [TrackUploader, TrackRecorder]
})
export class TrackRecorderPageModule { }