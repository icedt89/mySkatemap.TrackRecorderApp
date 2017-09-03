import { IonicModule } from "ionic-angular";
import { TrackRecorderSettingsModalComponent } from "./track-recorder-settings-modal.component";
import { NgModule } from "@angular/core";

@NgModule({
    imports: [IonicModule],
    declarations: [TrackRecorderSettingsModalComponent],
    entryComponents: [TrackRecorderSettingsModalComponent],
    exports: [TrackRecorderSettingsModalComponent]
})
export class TrackRecorderSettingsModalModule { }