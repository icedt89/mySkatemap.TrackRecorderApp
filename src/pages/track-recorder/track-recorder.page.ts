import { LoadingController } from "ionic-angular/components/loading/loading";
import {
    AlertOptions,
    Events,
    LoadingOptions,
    Platform,
    Refresher,
    ToastOptions,
    ViewController
} from "ionic-angular";
import { Component, ViewChild } from "@angular/core";

import { AlertController } from "ionic-angular/components/alert/alert";
import { LatLng } from "@ionic-native/google-maps";
import { MapComponent } from "../../components/map/map.component";
import { ModalController } from "ionic-angular/components/modal/modal";
import { RecordedTrackUploader } from "./recorded-track-uploader";
import { ToastController } from "ionic-angular/components/toast/toast";
import { TrackRecorder } from "./track-recorder";
import { Storage } from "@ionic/storage";
import { TrackRecorderSettings } from "../../app/track-recorder-settings";
import { TrackRecorderSettingsComponent } from "../../components/track-recorder-settings/track-recorder-settings.component";

@Component({
    selector: "track-recorder",
    templateUrl: "track-recorder.page.html"
})
export class TrackRecorderPage {
    private lastRecordedLatitude: number | null;
    private lastRecordedLongitude: number | null;
    private recordedPositions: number | null;
    private trackingStartedAt: Date | null;
    private trackingIsStopped = true;

    @ViewChild("map") private map: MapComponent;

    public constructor(viewController: ViewController,
        platform: Platform,
        private alertController: AlertController,
        private trackRecorder: TrackRecorder,
        private modalController: ModalController,
        private toastController: ToastController,
        private recordedTrackUploader: RecordedTrackUploader,
        private loadingController: LoadingController,
        private storage: Storage,
        events: Events) {
        this.trackRecorder.debugging();

        viewController.didEnter.subscribe(() => this.map.ready.subscribe(() => storage.ready().then(() => this.loadCurrentState())));

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

    private saveCurrentState(): void {
        this.storage.set("TrackRecorderPage.lastRecordedLatitude", this.lastRecordedLatitude);
        this.storage.set("TrackRecorderPage.lastRecordedLongitude", this.lastRecordedLongitude);
        this.storage.set("TrackRecorderPage.recordedPositions", this.recordedPositions);
        this.storage.set("TrackRecorderPage.trackingStartedAt", this.trackingStartedAt);
        this.storage.set("TrackRecorderPage.trackedPath", this.map.getTrack());
    }

    private loadCurrentState(): void {
        this.storage.get("TrackRecorderPage.lastRecordedLatitude").then((value: number | null) => {
            this.lastRecordedLatitude = value;
        });
        this.storage.get("TrackRecorderPage.lastRecordedLongitude").then((value: number | null) => {
            this.lastRecordedLongitude = value;
        });
        this.storage.get("TrackRecorderPage.recordedPositions").then((value: number) => {
            this.recordedPositions = value;
        });
        this.storage.get("TrackRecorderPage.trackingStartedAt").then((value: Date | null) => {
            this.trackingStartedAt = value;
        });
        this.storage.get("TrackRecorderPage.trackedPath").then((value: LatLng[] | null) => {
            if (value) {
                this.setTrackedPathOnMap(value);
            }
        });
    }

    private setTrackedPathOnMap(trackedPath: LatLng[]): Promise<any> {
        if (trackedPath.length > 1) {
            return this.map.setTrack(trackedPath).then(() => this.map.panToTrack());
        }

        return Promise.resolve(null);
    }

    // tslint:disable-next-line:no-unused-variable Used inside template.
    private refreshLastLocationDisplay(refresher: Refresher | null = null): void {
        this.trackRecorder.getRecorderStateInfo().then(positions => {
            this.recordedPositions = positions.recordedPositions.length;
            this.lastRecordedLatitude = positions.lastLatitude;
            this.lastRecordedLongitude = positions.lastLongitude;

            const trackedPath = positions.recordedPositions.map(position => new LatLng(position.latitude, position.longitude));
            this.setTrackedPathOnMap(trackedPath).then(() => {
                this.saveCurrentState();
            });

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
                        this.resetView();

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
                        const uploadTrackRecordingLoading = this.loadingController.create(<LoadingOptions>{
                            content: "Wird hochgeladen...",
                        });
                        uploadTrackRecordingLoading.present();

                        this.trackRecorder.getRecorderStateInfo().then(recorderStateInfo => {
                            this.recordedTrackUploader.uploadRecordedTrack(recorderStateInfo.recordedPositions, this.trackingStartedAt).then(() => this.trackRecorder.deleteAllRecordings()).then(() => {
                                uploadTrackRecordingLoading.dismiss();
                                const uploadedSuccessfulToast = this.toastController.create(<ToastOptions>{
                                    closeButtonText: "Toll",
                                    message: "Strecke erfolgreich hochgeladen",
                                    position: "middle",
                                    duration: 3000
                                });
                                uploadedSuccessfulToast.present();
                                this.resetView();

                                this.refreshLastLocationDisplay();
                            });
                        });
                    }
                }
            ]
        });
        resetRecordingPrompt.present();
    }

    private resetView(): void {
        this.map.resetTrack();
        this.lastRecordedLatitude = null;
        this.lastRecordedLongitude = null;
        this.recordedPositions = 0;
        this.trackingStartedAt = null;

        this.saveCurrentState();
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
                this.trackRecorder.record().then(() => {
                    if (!this.trackingStartedAt) {
                        this.trackingStartedAt = new Date();

                        this.saveCurrentState();
                    }

                    this.trackingIsStopped = false;
                }, error => { });
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