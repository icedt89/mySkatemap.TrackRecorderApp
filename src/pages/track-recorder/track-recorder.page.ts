import { AlertOptions, Events, Platform, Refresher, ToastOptions, ViewController } from "ionic-angular";
import { Component, ViewChild } from "@angular/core";

import { AlertController } from "ionic-angular/components/alert/alert";
import { LatLng } from "@ionic-native/google-maps";
import { MapComponent } from "../../components/map/map.component";
import { ModalController } from "ionic-angular/components/modal/modal";
import { RecordedTrackUploader } from "./recorded-track-uploader";
import { ToastController } from "ionic-angular/components/toast/toast";
import { TrackRecorder } from "./track-recorder";
import { TrackRecorderSettings } from "../../app/track-recorder-settings";
import { TrackRecorderSettingsComponent } from "../../components/track-recorder-settings/track-recorder-settings.component";

@Component({
    selector: "track-recorder",
    templateUrl: "track-recorder.page.html"
})
export class TrackRecorderPage {
    // tslint:disable-next-line:no-unused-variable Used inside template.
    private canRefresh = true;

    private lastRecordedLatitude: number | null;
    private lastRecordedLongitude: number | null;
    private recordedPositions: number | null;
    private trackingIsStopped = true;

    @ViewChild("map") private map: MapComponent;

    public constructor(viewController: ViewController,
        platform: Platform,
        private alertController: AlertController,
        private trackRecorder: TrackRecorder,
        private modalController: ModalController,
        private toastController: ToastController,
        private recordedTrackUploader: RecordedTrackUploader,
        events: Events) {
        this.trackRecorder.debugging();

        viewController.willLeave.subscribe(() => {
            this.stop();
        });
        events.subscribe("TrackRecorder-LocationMode", enabled => {
            if (!enabled) {
                this.stopTrackRecorder().then(() => {
                    const trackingStoppedTaost = this.toastController.create(<ToastOptions>{
                        message: "Standort wurde deaktiviert. Aufnahme ist pausiert.",
                        duration: 3000,
                        position: "middle"
                    });
                    trackingStoppedTaost.present();
                });
            }
        });
    }

    // tslint:disable-next-line:no-unused-variable Used inside template.
    private refreshLastLocationDisplay(refresher: Refresher | null = null): void {
        this.trackRecorder.getPositions().then(positions => {
            this.recordedPositions = positions.length;
            this.lastRecordedLatitude = this.trackRecorder.lastRecordedLatitude;
            this.lastRecordedLongitude = this.trackRecorder.lastRecordedLongitude;

            const trackedPath = positions.map(position => new LatLng(position.latitude, position.longitude));
            if (trackedPath.length > 1) {
                this.map.setTrack(trackedPath).then(() => this.map.panToTrack());
            }

            if (refresher) {
                refresher.complete();
            }
        });
    }

    // tslint:disable-next-line:no-unused-variable Used inside template.
    private showTrackRecorderSettings(event: Event): void {
        const recorderSettings = this.trackRecorder.settings;

        const trackRecorderSettingsModal = this.modalController.create(TrackRecorderSettingsComponent, {
            settings: recorderSettings
        });
        trackRecorderSettingsModal.onDidDismiss((data: { settings: TrackRecorderSettings } | null) => {
            if (!data) {
                return;
            }

            const setTrackRecorderSettingsToast = this.toastController.create(<ToastOptions>{
                message: "Einstellungen akzeptiert",
                duration: 3000,
                position: "middle",
            });
            setTrackRecorderSettingsToast.present();

            this.trackRecorder.setSettings(data.settings);
        });
        trackRecorderSettingsModal.present();
    }

    // tslint:disable-next-line:no-unused-variable Used inside template.
    private resetTrackRecording(): void {
        const resetRecordingPrompt = this.alertController.create({
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
                    handler: () => this.trackRecorder.deleteAllRecordings().then(() => {
                        const allRecordingsDeletedToast = this.toastController.create(<ToastOptions>{
                            message: "Strecke gelöscht",
                            duration: 3000,
                            position: "middle"
                        });
                        allRecordingsDeletedToast.present();
                        this.map.resetTrack();

                        this.refreshLastLocationDisplay();
                    })
                }
            ]
        });
        resetRecordingPrompt.present();
    }

    // tslint:disable-next-line:no-unused-variable Used inside template.
    private uploadTrackRecording(): void {
        const resetRecordingPrompt = this.alertController.create({
            title: "Strecke übermitteln",
            message: "Möchten Sie die aufgezeichnete Strecke übermitteln?",
            enableBackdropDismiss: true,
            buttons: [
                {
                    text: "Abbrechen",
                    role: "cancel"
                },
                {
                    text: "Ja",
                    handler: () => {
                        this.trackRecorder.getPositions().then(positions => {
                            this.recordedTrackUploader.uploadRecordedTrack(positions, this.trackRecorder.startedAt).then(() => this.trackRecorder.deleteAllRecordings()).then(() => {
                                const uploadedSuccessfulToast = this.toastController.create(<ToastOptions>{
                                    closeButtonText: "Toll",
                                    message: "Strecke erfolgreich hochgeladen",
                                    position: "middle",
                                    duration: 3000
                                });
                                uploadedSuccessfulToast.present();

                                this.refreshLastLocationDisplay();
                            });
                        });
                    }
                }
            ]
        });
        resetRecordingPrompt.present();
    }

    private get canDeleteTrackRecording(): boolean {
        return this.trackingIsStopped && this.countOfCollectedPositions > 0;
    }

    private get canUploadTrackRecording(): boolean {
        return this.trackingIsStopped && this.countOfCollectedPositions > 0;
    }

    private get canShowTrackRecorderSettings(): boolean {
        return this.trackingIsStopped;
    }

    private get lastLatitude(): number {
        return this.lastRecordedLatitude;
    }

    private get lastLongitude(): number {
        return this.lastRecordedLongitude;
    }

    private get countOfCollectedPositions(): number | null {
        return this.recordedPositions;
    }

    private get isStopped(): boolean {
        return this.trackingIsStopped;
    }

    private stopTrackRecorder(): Promise<any> {
        return this.trackRecorder.stop().then(() => {
            this.trackingIsStopped = true;

            this.refreshLastLocationDisplay();
        });
    }

    // tslint:disable-next-line:no-unused-variable Used inside template.
    private stop(): void {
        this.stopTrackRecorder();
    }

    // tslint:disable-next-line:no-unused-variable Used inside template.
    private record(): void {
        this.trackRecorder.isLocationEnabled().then(enabled => {
            if (enabled) {
                this.trackRecorder.record().then(() => this.trackingIsStopped = false, error => { });
            } else {
                const pleaseEnableLocationAlert = this.alertController.create(<AlertOptions>{
                    title: "Standort ist deaktiviert",
                    message: "Möchten Sie die Standorteinstellungen öffnen?",
                    enableBackdropDismiss: true,
                    buttons: [
                        {
                            text: "Nein",
                            role: "cancel",
                            handler: () => {
                                const pleaseEnableLocationToast = this.toastController.create(<ToastOptions>{
                                    message: "Standort aktivieren um Strecke aufzunehmen",
                                    duration: 3000,
                                    position: "middle"
                                });
                                pleaseEnableLocationToast.present();
                            }
                        },
                        {
                            text: "Ja",
                            handler: () => this.trackRecorder.showLocationSettings()
                        }
                    ]
                });
                pleaseEnableLocationAlert.present();
            }
        });
    }
}