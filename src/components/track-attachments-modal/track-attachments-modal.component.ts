import { CapturedMediaResult } from "../../infrastructure/media-capture/captured-media-result";
import { TrackAttachment } from "../../infrastructure/track-attachment";
import { TrackAttachmentsModalModel } from "./track-attachments-modal-model";
import { MediaCapturer } from "../../infrastructure/media-capture/media-capturer";
import { NavParams, ViewController } from "ionic-angular";
import { Component } from "@angular/core";

@Component({
    templateUrl: "track-attachments-modal.component.html"
})
export class TrackAttachmentsModalComponent {
    private model: TrackAttachmentsModalModel;

    public constructor(private viewController: ViewController,
        navigationParameters: NavParams,
        private mediaCapturer: MediaCapturer) {
        this.model = navigationParameters.get("model");
    }

    // tslint:disable-next-line:no-unused-variable Used inside template
    private apply(): void {
        this.viewController.dismiss({
            model: this.model
        });
    }

    // tslint:disable-next-line:no-unused-variable Used inside template
    private async captureCameraImage(): Promise<void> {
        const result = await this.mediaCapturer.captureCameraImage();

        try {
            this.handleCapturedMediaResult(result);
        } catch (any) { }
    }

    // tslint:disable-next-line:no-unused-variable Used inside template
    private async selectLibraryImage(): Promise<void> {
        const result = await this.mediaCapturer.selectLibraryImage();

        try {
            this.handleCapturedMediaResult(result);
        } catch (any) { }
    }

    private handleCapturedMediaResult(result: CapturedMediaResult): void {
        if (result.isBase64DataUrl) {
            const trackAttachment = new TrackAttachment(result.dataUrl);
            trackAttachment.comment = new Date().toLocaleString();

            this.model.addAttachment(trackAttachment);
        }
    }
}