import { ITrackUploader } from "../../../infrastructure/track-uploader/itrack-uploader";
import { Inject } from "@angular/core";
import { ITrackRecorder } from "../../../infrastructure/track-recorder/itrack-recorder";
import { TrackRecordingStore } from "../../../infrastructure/track-store/track-recording-store";
import { Exception } from "../../../infrastructure/exception";
import { Events, LoadingController } from "ionic-angular";
import { TrackAttachmentsModalModel } from "../../../components/track-attachments-modal/track-attachments-modal-model";
import { TrackRecorderPopoverModel } from "./track-recorder-popover-model";
import {
    AlertController,
    AlertOptions,
    ModalController,
    NavParams,
    ToastController,
    ViewController
} from "ionic-angular";
import { Component } from "@angular/core";
import { TrackAttachmentsModalComponent } from "../../../components/track-attachments-modal/track-attachments-modal.component";

@Component({
    templateUrl: "track-recorder-popover.component.html"
})
export class TrackRecorderPopoverComponent {
    private model: TrackRecorderPopoverModel;

    public constructor(navigationParameters: NavParams,
        private modalController: ModalController,
        private viewController: ViewController,
        private alertController: AlertController,
        @Inject("TrackRecorder") private trackRecorder: ITrackRecorder,
        private loadingController: LoadingController,
        @Inject("TrackUploader") private trackUploader: ITrackUploader,
        private toastController: ToastController,
        private trackRecordingStore: TrackRecordingStore,
        private events: Events) {
        this.model = navigationParameters.get("model");
    }

    private get canDeleteTrackRecording(): boolean {
        return this.model.isPaused && !!this.model.trackRecording;
    }

    private get canShowTrackAttachments(): boolean {
        return this.model.isPaused && !!this.model.trackRecording;
    }

    private get canFinishTrackRecording(): boolean {
        return this.model.isPaused && !!this.model.trackRecording && this.model.trackRecording.trackedPositions.length > 1;
    }

    // tslint:disable-next-line:no-unused-variable Used inside template.
    private showTrackAttachments(): void {
        // Close popover (makes back button working again Oo, too)
        this.viewController.dismiss().then(() => {
            if (!this.model.trackRecording) {
                throw new Exception("No current track recording.");
            }

            const trackAttachmentsModal = this.modalController.create(TrackAttachmentsModalComponent, {
                trackAttachments: new TrackAttachmentsModalModel(this.model.trackRecording.trackAttachments.map(_ => _))
            });
            trackAttachmentsModal.onDidDismiss((data: { model: TrackAttachmentsModalModel } | null) => {
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
                    handler: () => this.trackRecorder.deleteAllRecordings().then(() => this.events.publish("track-recording-reset"))
                }
            ]
        });
        // Close popover (makes back button working again Oo, too)
        resetRecordingPrompt.present().then(() => this.viewController.dismiss());
    }

    // tslint:disable-next-line:no-unused-variable Used inside template.
    private finishTrackRecording(): void {
        if (!this.model.trackRecording) {
            throw new Exception("No current track recording.");
        }

        const archiveRecordingPrompt = this.alertController.create({
            title: "Strecke abschließen",
            message: "Die Strecke kann dann nur noch hochgeladen werden.",
            enableBackdropDismiss: true,
            buttons: [
                {
                    text: "Abbrechen",
                    role: "cancel"
                },
                {
                    text: "Ja",
                    handler: () => {
                        this.trackRecordingStore.storeTrack(this.model.trackRecording).then(() => this.events.publish("track-recording-finished"));
                    }
                }
            ]
        });
        // Close popover (makes back button working again Oo, too)
        archiveRecordingPrompt.present().then(() => this.viewController.dismiss());
    }
}