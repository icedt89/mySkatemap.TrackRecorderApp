import { haversineForPolyline } from "./haversine";
import { SplashScreen } from "@ionic-native/splash-screen";
import {
    AlertController,
    AlertOptions,
    Events,
    LoadingController,
    LoadingOptions,
    ModalController,
    Platform,
    Refresher,
    ToastController,
    ToastOptions,
    ViewController
} from "ionic-angular";
import { Component, ViewChild } from "@angular/core";

import { LatLng } from "@ionic-native/google-maps";
import { MapComponent } from "../../components/map/map.component";
import { RecordedTrackUploader } from "./recorded-track-uploader";
import { TrackRecorder } from "./track-recorder";
import { Storage } from "@ionic/storage";
import { TrackRecorderSettings } from "../../app/track-recorder-settings";
import { TrackRecorderSettingsComponent } from "../../components/track-recorder-settings/track-recorder-settings.component";

@Component({
    selector: "track-recorder",
    templateUrl: "track-recorder.page.html"
})
export class TrackRecorderPage {
    private lastUserUpdate: string | null;
    private approximateTrackLength: string | null;
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
        splashscreen: SplashScreen,
        events: Events) {
        this.trackRecorder.debugging();

        viewController.didEnter.subscribe(() => this.map.ready.subscribe(() => storage.ready().then(() => this.loadCurrentState()).then(() => splashscreen.hide())));

        events.subscribe("TrackRecorder-LocationMode", enabled => {
            if (!enabled && !this.trackingIsStopped) {
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
        this.storage.set("TrackRecorderPage.lastUserUpdate", this.lastUserUpdate);
        this.storage.set("TrackRecorderPage.approximateTrackLength", this.approximateTrackLength);
        this.storage.set("TrackRecorderPage.recordedPositions", this.recordedPositions);
        this.storage.set("TrackRecorderPage.trackingStartedAt", this.trackingStartedAt);
        this.storage.set("TrackRecorderPage.trackedPath", this.map.getTrack());
    }

    private loadCurrentState(): void {
        this.storage.get("TrackRecorderPage.lastUserUpdate").then((value: string | null) => {
            this.lastUserUpdate = value;
        });
        this.storage.get("TrackRecorderPage.approximateTrackLength").then((value: string | null) => {
            this.approximateTrackLength = value;
        });
        this.storage.get("TrackRecorderPage.recordedPositions").then((value: number | null) => {
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
        this.trackRecorder.getLocations().then(positions => {
            if (positions.length) {
                if (positions.length !== this.recordedPositions) {
                    this.recordedPositions = positions.length;
                    this.lastUserUpdate = new Date().toLocaleString("de");

                    const trackedPath = positions.map(position => new LatLng(position.latitude, position.longitude));
                    const computedTracklength = haversineForPolyline(trackedPath);
                    if (computedTracklength > 1000) {
                        this.approximateTrackLength = `${(+(computedTracklength / 1000).toFixed(3)).toLocaleString("de")} km`;
                    } else {
                        this.approximateTrackLength = `${(+computedTracklength.toFixed(1)).toLocaleString("de")} m`;
                    }

                    this.setTrackedPathOnMap(trackedPath).then(() => this.saveCurrentState());
                }
            } else {
                this.resetView();
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
                showCloseButton: true,
                closeButtonText: "Ok"
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
                            position: "middle",
                            showCloseButton: true,
                            closeButtonText: "Ok"
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
            title: "Strecke erstellen",
            message: "Möchten Sie die aufgezeichnete Strecke übermitteln? Sie können optional einen Namen vergeben.",
            enableBackdropDismiss: true,
            inputs: [
                {
                    name: "trackName",
                    value: `Strecke vom ${this.trackingStartedAt}`,
                    placeholder: "Name der Strecke",
                    type: "text"
                }
            ],
            buttons: [
                {
                    text: "Abbrechen",
                    role: "cancel"
                },
                {
                    text: "Ja",
                    handler: data => {
                        const uploadTrackRecordingLoading = this.loadingController.create(<LoadingOptions>{
                            content: "Wird erstellt...",
                        });
                        uploadTrackRecordingLoading.present();

                        this.trackRecorder.getLocations().then(positions => {
                            this.recordedTrackUploader.uploadRecordedTrack(positions, data.trackName, this.trackingStartedAt).then(() => this.trackRecorder.deleteAllRecordings()).then(() => {
                                uploadTrackRecordingLoading.dismiss();
                                const uploadedSuccessfulToast = this.toastController.create(<ToastOptions>{
                                    message: "Strecke erfolgreich erstellt",
                                    position: "middle",
                                    duration: 3000,
                                    showCloseButton: true,
                                    closeButtonText: "Toll"
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
        this.lastUserUpdate = null;
        this.approximateTrackLength = null;
        this.recordedPositions = null;
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

    private get lastUpdate(): string | null {
        return this.lastUserUpdate;
    }

    private get approximateLength(): string | null {
        return this.approximateTrackLength;
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
                                    message: "Bitte Standort aktivieren um Strecke aufzunehmen",
                                    duration: 3000,
                                    position: "middle",
                                    showCloseButton: true,
                                    closeButtonText: "Ok"
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