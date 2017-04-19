import { TrackAttachmentsModalComponent } from "./track-attachments-modal.component";
import { IonicModule } from "ionic-angular";
import { NgModule } from "@angular/core";
import { MediaCapturer } from "../../infrastructure/media-capture/media-capturer";
import { Camera } from "@ionic-native/camera";

@NgModule({
    imports: [IonicModule],
    declarations: [TrackAttachmentsModalComponent],
    entryComponents: [TrackAttachmentsModalComponent],
    exports: [TrackAttachmentsModalComponent],
    providers: [Camera, MediaCapturer]
})
export class TrackAttachmentsModalModule { }