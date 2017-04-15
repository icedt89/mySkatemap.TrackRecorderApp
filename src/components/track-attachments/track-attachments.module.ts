import { TrackAttachmentsComponent } from "./track-attachments.component";
import { IonicModule } from "ionic-angular";
import { NgModule } from "@angular/core";
import { MediaCapturer } from "../../infrastructure/media-capture/media-capturer";
import { Camera } from "@ionic-native/camera";

@NgModule({
    imports: [IonicModule],
    declarations: [TrackAttachmentsComponent],
    entryComponents: [TrackAttachmentsComponent],
    exports: [TrackAttachmentsComponent],
    providers: [Camera, MediaCapturer]
})
export class TrackAttachmentsModule { }