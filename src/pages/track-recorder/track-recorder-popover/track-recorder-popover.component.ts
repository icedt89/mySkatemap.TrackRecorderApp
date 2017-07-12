import { Inject } from "@angular/core";
import { ITrackRecorder } from "../../../infrastructure/track-recorder/itrack-recorder";
import { TrackRecordingStore } from "../../../infrastructure/track-store/track-recording-store";
import { Exception } from "../../../infrastructure/exception";
import { Events } from "ionic-angular";
import { TrackAttachmentsModalModel } from "../../../components/track-attachments-modal/track-attachments-modal-model";
import { TrackRecorderPopoverModel } from "./track-recorder-popover-model";
import {
    AlertController,
    AlertOptions,
    ModalController,
    NavParams,
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
        private trackRecordingStore: TrackRecordingStore,
        private events: Events) {
        this.model = navigationParameters.get("model");

        if (!this.model) {
            throw new Exception("No model supplied.");
        }
    }

    private get canDiscardCurrentTrackRecording(): boolean {
        return this.model.isPaused && !!this.model.trackRecording;
    }

    private get canShowCurrentTrackRecordingAttachments(): boolean {
        return this.model.isPaused && !!this.model.trackRecording;
    }

    private get canFinishCurrentTrackRecording(): boolean {
        return this.model.isPaused && !!this.model.trackRecording && this.model.trackRecording.trackedPositions.length > 1;
    }

    // tslint:disable-next-line:no-unused-variable Used inside template.
    private async showCurrentTrackRecordingAttachments(): Promise<void> {
        if (!this.model.trackRecording) {
            throw new Exception("No current track recording.");
        }

        const showCurrentTrackRecordingAttachmentsModal = this.modalController.create(TrackAttachmentsModalComponent, {
            model: new TrackAttachmentsModalModel(this.model.trackRecording.trackAttachments.map(_ => _))
        });

        showCurrentTrackRecordingAttachmentsModal.onDidDismiss((data: { model: TrackAttachmentsModalModel } | null) => {
            if (!data) {
                // Modal was not successfully dismissed (user, used back button...).
                return;
            }

            if (data.model.attachmentsChanged) {
                this.events.publish("current-track-recording-attachments-changed", data.model.attachments);
            }
        });
        showCurrentTrackRecordingAttachmentsModal.present();

        // Close popover (makes back button working again Oo, too)
        this.viewController.dismiss();
    }

    // tslint:disable-next-line:no-unused-variable Used inside template.
    private discardCurrentTrackRecording(): void {
        if (!this.model.trackRecording) {
            throw new Exception("No current track recording.");
        }

        const discardCurrentRecordingPrompt = this.alertController.create(<AlertOptions>{
            title: "Strecke verwerfen",
            message: "Möchtest du die aufgezeichnete Strecke wirklich verwerfen?",
            enableBackdropDismiss: true,
            buttons: [
                {
                    text: "Abbrechen",
                    role: "cancel"
                },
                {
                    text: "Ja",
                    handler: async () => {
                        await this.trackRecorder.deleteAllRecordings();

                        this.events.publish("current-track-recording-discarded");
                    }
                }
            ]
        });

        discardCurrentRecordingPrompt.present();
    }

    // tslint:disable-next-line:no-unused-variable Used inside template.
    private finishCurrentTrackRecording(): void {
        if (!this.model.trackRecording) {
            throw new Exception("No current track recording.");
        }

        if (this.model.trackRecording.isInvalid) {
            const informAboutInvalidTrackRecording = this.alertController.create(<AlertOptions>{
                title: "Strecke unvollständig",
                message: "Bitte gib der Strecke einen Namen.",
                enableBackdropDismiss: true,
                buttons: [
                    {
                        text: "Mach ich",
                        role: "cancel"
                    }
                ]
            });
            informAboutInvalidTrackRecording.present();
        } else {
            const finishCurrentRecordingPrompt = this.alertController.create(<AlertOptions>{
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
                        handler: async () => {
                            this.model.trackRecording.trackingFinishedAt = new Date();

                            await this.trackRecordingStore.storeTrack(this.model.trackRecording);

                            this.events.publish("current-track-recording-finished");
                        }
                    }
                ]
            });
            finishCurrentRecordingPrompt.present();
        }
    }
}