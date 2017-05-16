import { MapModule } from "../../../components/map/map.module";
import { IonicModule } from "ionic-angular";
import { ShowSavedTrackRecordingModalComponent } from "./show-saved-track-recording-modal.component";
import { NgModule } from "@angular/core";

@NgModule({
    imports: [IonicModule, MapModule],
    declarations: [ShowSavedTrackRecordingModalComponent],
    entryComponents: [ShowSavedTrackRecordingModalComponent],
    exports: [ShowSavedTrackRecordingModalComponent]
})
export class ShowSavedTrackRecordingModalModule { }