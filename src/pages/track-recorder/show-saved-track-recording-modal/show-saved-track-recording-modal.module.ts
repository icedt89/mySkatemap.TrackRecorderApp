import {
    TrackAttachmentsModalModule
} from "../../../components/track-attachments-modal/track-attachments-modal.module";
import { IonicModule } from "ionic-angular";
import { ShowSavedTrackRecordingModalComponent } from "./show-saved-track-recording-modal.component";
import { NgModule } from "@angular/core";

@NgModule({
    imports: [IonicModule, TrackAttachmentsModalModule],
    declarations: [ShowSavedTrackRecordingModalComponent],
    entryComponents: [ShowSavedTrackRecordingModalComponent],
    exports: [ShowSavedTrackRecordingModalComponent]
})
export class ShowSavedTrackRecordingModalModule { }