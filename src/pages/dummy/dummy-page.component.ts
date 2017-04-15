import { MediaCapturer } from "../../infrastructure/media-capture/media-capturer";
import { TrackRecorderPageComponent } from "../track-recorder/track-recorder-page.component";
import { NavController } from "ionic-angular";
import { ViewController } from "ionic-angular";
import { Component } from "@angular/core";

@Component({
    selector: "dummy",
    templateUrl: "dummy-page.component.html"
})
export class DummyPageComponent {
    private mediaFilePath: string;

    public constructor(private imageCapturer: MediaCapturer,
        viewController: ViewController, private navController: NavController) {
    }

    private goto(): void {
        this.navController.push(TrackRecorderPageComponent);
    }

    private takePicture(): void {
        this.imageCapturer.selectLibraryImage().then(result => {
             this.mediaFilePath = result.dataUrl;
        });
    }
}