import { MediaCapturer } from "../../infrastructure/media-capturer";
import { IonicModule } from "ionic-angular";
import { DummyPageComponent } from "./dummy-page.component";
import { NgModule } from "@angular/core";
import { MediaCapture } from "@ionic-native/media-capture";
import { File } from "@ionic-native/file";

@NgModule({
    imports: [IonicModule],
    declarations: [DummyPageComponent],
    exports: [DummyPageComponent],
    entryComponents: [DummyPageComponent],
    providers: [MediaCapture, File, MediaCapturer]
})
export class DummyPageModule { }