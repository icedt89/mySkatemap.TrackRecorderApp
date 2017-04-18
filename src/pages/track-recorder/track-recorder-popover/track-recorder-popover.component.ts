import { TrackUploader } from "../../../infrastructure/track-uploader";
import { Events, LoadingController } from "ionic-angular";
import { TrackRecorder } from "../../../infrastructure/track-recorder/track-recorder";
import { TrackAttachmentsModel } from "../../../components/track-attachments/track-attachments-model";
import { TrackRecorderPopoverModel } from "./track-recorder-popover-model";
import {
    AlertController,
    AlertOptions,
    LoadingOptions,
    ModalController,
    NavParams,
    ToastController,
    ViewController
} from "ionic-angular";
import { Component } from "@angular/core";
import { TrackAttachmentsComponent } from "../../../components/track-attachments/track-attachments.component";

@Component({
    templateUrl: "track-recorder-popover.component.html"
})
export class TrackRecorderPopoverComponent {
    private model: TrackRecorderPopoverModel;

    public constructor(navigationParameters: NavParams,
        private modalController: ModalController,
        private viewController: ViewController,
        private alertController: AlertController,
        private trackRecorder: TrackRecorder,
        private loadingController: LoadingController,
        private trackUploader: TrackUploader,
        private toastController: ToastController,
        private events: Events) {
        this.model = navigationParameters.get("model");
    }

    private get canDeleteTrackRecording(): boolean {
        return this.model.isPaused && !!this.model.trackRecording;
    }

    private get canUploadTrackRecording(): boolean {
        return this.model.isPaused && !!this.model.trackRecording && this.model.trackRecording.trackedPositions.length > 1;
    }

    private get canShowTrackAttachments(): boolean {
        return this.model.isPaused && !!this.model.trackRecording;
    }

    // tslint:disable-next-line:no-unused-variable Used inside template.
    private showTrackAttachments(): void {
        // Close popover (makes back button working again Oo, too)
        this.viewController.dismiss().then(() => {
            const trackAttachmentsModal = this.modalController.create(TrackAttachmentsComponent, {
                trackAttachments: new TrackAttachmentsModel(this.model.trackRecording.trackAttachments.map(_ => _))
            });
            trackAttachmentsModal.onDidDismiss((data: { model: TrackAttachmentsModel } | null) => {
                if (!data) {
                    return;
                }

                this.events.publish("track-attachments-changed", data.model.attachments);
            });
            trackAttachmentsModal.present();
        });
    }

    // tslint:disable-next-line:no-unused-variable Used inside template.
    private resetTrackRecording(): void {
        const resetRecordingPrompt = this.alertController.create(<AlertOptions>{
            title: "Strecke löschen",
            message: "Möchten Sie die aufgezeichnete Strecke wirklich löschen?",
            enableBackdropDismiss: true,
            buttons: [
                {
                    text: "Abbrechen",
                    role: "cancel"
                },
                {
                    text: "Ja",
                    handler: () => this.trackRecorder.deleteAllRecordings().then(() => this.events.publish("track-recordings-reset"))
                }
            ]
        });
        // Close popover (makes back button working again Oo, too)
        resetRecordingPrompt.present().then(() => this.viewController.dismiss());
    }

    // tslint:disable-next-line:no-unused-variable Used inside template.
    private uploadTrackRecording(): void {
        const resetRecordingPrompt = this.alertController.create({
            title: "Strecke hochladen",
            message: "Möchten Sie die aufgezeichnete Strecke hochladen?",
            enableBackdropDismiss: true,
            buttons: [
                {
                    text: "Abbrechen",
                    role: "cancel"
                },
                {
                    text: "Ja",
                    handler: () => {
                        const uploadTrackRecordingLoading = this.loadingController.create(<LoadingOptions>{
                            content: "Wird hochgeladen...",
                        });

                        uploadTrackRecordingLoading.present()
                            .then(() => this.trackRecorder.getLocations())
                            .then(positions => this.trackUploader.uploadRecordedTrack(positions, this.model.trackRecording))
                            .then(uploaded => {
                                if (uploaded) {
                                    return this.trackRecorder.deleteAllRecordings()
                                        .then(() => this.events.publish("track-recordings-uploaded-success"));
                                }

                                return this.events.publish("track-recordings-uploaded-failed");
                            })
                            .then(() => uploadTrackRecordingLoading.dismiss());
                    }
                }
            ]
        });
        // Close popover (makes back button working again Oo, too)
        resetRecordingPrompt.present().then(() => this.viewController.dismiss());
    }
}