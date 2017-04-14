import { IonicModule } from "ionic-angular";
import { TrackRecorderSettingsComponent } from "./track-recorder-settings.component";
import { NgModule } from "@angular/core";

@NgModule({
    imports: [IonicModule],
    declarations: [TrackRecorderSettingsComponent],
    entryComponents: [TrackRecorderSettingsComponent],
    exports: [TrackRecorderSettingsComponent]
})
export class TrackRecorderSettingsModule { }