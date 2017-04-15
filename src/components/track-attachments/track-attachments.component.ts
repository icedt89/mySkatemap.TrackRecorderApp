import { CapturedMediaResult } from "../../infrastructure/media-capture/captured-media-result";
import { TrackAttachment } from "../../infrastructure/track-attachment";
import { TrackAttachmentsModel } from "./track-attachments-model";
import { MediaCapturer } from "../../infrastructure/media-capture/media-capturer";
import { NavParams, ViewController } from "ionic-angular";
import { Component } from "@angular/core";

@Component({
    templateUrl: "track-attachments.component.html"
})
export class TrackAttachmentsComponent {
    private trackAttachmentsModel: TrackAttachmentsModel;

    public constructor(private viewController: ViewController,
        navigationParameters: NavParams,
        private mediaCapturer: MediaCapturer) {
        this.trackAttachmentsModel = navigationParameters.get("trackAttachments");
    }

    // tslint:disable-next-line:no-unused-variable Used inside template
    private apply(): void {
        this.viewController.dismiss({
            model: this.trackAttachmentsModel
        });
    }

    // tslint:disable-next-line:no-unused-variable Used inside template
    private captureCameraImage(): void {
        this.mediaCapturer.captureCameraImage().then(result => this.handleCapturedMediaResult(result), error => { });
    }

    // tslint:disable-next-line:no-unused-variable Used inside template
    private selectLibraryImage(): void {
        this.mediaCapturer.selectLibraryImage().then(result => this.handleCapturedMediaResult(result), error => { });
    }

    private handleCapturedMediaResult(result: CapturedMediaResult): void {
        if (result.isBase64DataUrl) {
            const trackAttachment = new TrackAttachment(result.dataUrl);
            trackAttachment.comment = new Date().toLocaleString();

            this.trackAttachmentsModel.attachments.push(trackAttachment);
        }
    }
}