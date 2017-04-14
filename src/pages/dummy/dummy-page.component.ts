import { MediaCapturer } from "../../infrastructure/media-capturer";
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
        this.imageCapturer.captureImage().then(dataUrls => {
            if (dataUrls.length) {
                this.mediaFilePath = dataUrls[0];
            }
        });
    }
}