import { MediaCapturer } from "../../infrastructure/media-capture/media-capturer";
import { IonicModule } from "ionic-angular";
import { DummyPageComponent } from "./dummy-page.component";
import { NgModule } from "@angular/core";
import { Camera } from "@ionic-native/camera";

@NgModule({
    imports: [IonicModule],
    declarations: [DummyPageComponent],
    exports: [DummyPageComponent],
    entryComponents: [DummyPageComponent],
    providers: [ MediaCapturer, Camera]
})
export class DummyPageModule { }