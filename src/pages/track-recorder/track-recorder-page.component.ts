import { TrackRecorderSettings } from "../../infrastructure/track-recorder/track-recorder-settings";
import { TrackUploader } from "../../infrastructure/track-uploader";
import { TrackRecorder } from "../../infrastructure/track-recorder/track-recorder";
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
import { GeoHelper } from "../../infrastructure/geo-helper";
import { MapComponent } from "../../components/map/map.component";
import { Storage } from "@ionic/storage";
import { TrackRecorderSettingsComponent } from "../../components/track-recorder-settings/track-recorder-settings.component";
import { TrackRecording } from "./track-recording";

@Component({
    selector: "track-recorder",
    templateUrl: "track-recorder-page.component.html",
    styleUrls: ["/track-recorder-page.component.css"]
})
export class TrackRecorderPageComponent {
    private _lastUpdate: string | null;
    private _approximateTrackLength: string | null;
    private _currentTrackRecording: TrackRecording | null;
    private trackingIsStopped = true;

    @ViewChild("map") private map: MapComponent;

    public constructor(viewController: ViewController,
        platform: Platform,
        private alertController: AlertController,
        private trackRecorder: TrackRecorder,
        private modalController: ModalController,
        private toastController: ToastController,
        private trackUploader: TrackUploader,
        private loadingController: LoadingController,
        private storage: Storage,
        splashscreen: SplashScreen,
        events: Events) {
        viewController.didEnter.subscribe(() => this.map.ready.subscribe(
            () => storage.ready()
                .then(() => this.loadPageState())
                .then(() => this.loadCurrentTrackRecording())
                .then(trackRecording => {
                    if (!trackRecording) {
                        return;
                    }

                    this._currentTrackRecording = trackRecording;

                    this.setTrackedPathOnMap(this._currentTrackRecording.trackedPositions);
                })
                .then(() => splashscreen.hide())
                .then(() => this.loadTrackRecorderSettings())
                .then(settings => {
                    if (!settings) {
                        return;
                    }

                    this.trackRecorder.setSettings(settings);
                })
        ));

        events.subscribe("TrackRecorder-LocationMode", enabled => {
            if (!enabled && !this.trackingIsStopped) {
                this.stopTrackRecorder().then(() => {
                    const trackingStoppedToast = this.toastController.create(<ToastOptions>{
                        message: "Standort wurde deaktiviert. Aufnahme ist pausiert.",
                        duration: 3000,
                        position: "middle",
                        closeButtonText: "Ok",
                        showCloseButton: true
                    });
                    trackingStoppedToast.present();
                });
            }
        });
    }

    private savePageState(): void {
        if (!this._lastUpdate) {
            this.storage.remove("TrackRecorderPage.lastUpdate");
        } else {
            this.storage.set("TrackRecorderPage.lastUpdate", this._lastUpdate);
        }

        if (!this._lastUpdate) {
            this.storage.remove("TrackRecorderPage.approximateTrackLength");
        } else {
            this.storage.set("TrackRecorderPage.approximateTrackLength", this._approximateTrackLength);
        }
    }

    private loadPageState(): void {
        this.storage.get("TrackRecorderPage.lastUpdate").then((value: string | null) => {
            this._lastUpdate = value;
        });
        this.storage.get("TrackRecorderPage.approximateTrackLength").then((value: string | null) => {
            this._approximateTrackLength = value;
        });
    }

    private saveTrackRecorderSettings(settings: TrackRecorderSettings): void {
        if (!settings) {
            this.storage.remove("TrackRecorder.Settings")
        } else {
            this.storage.set("TrackRecorder.Settings", settings);
        }
    }

    private loadTrackRecorderSettings(): Promise<TrackRecorderSettings> {
        return this.storage.get("TrackRecorder.Settings").then(settings => {
            if (settings) {
                return Object.assign(new TrackRecorderSettings(), settings);
            }

            return null;
        });
    }

    private saveCurrentTrackRecording(): void {
        if (!this._currentTrackRecording) {
            this.storage.remove("TrackRecording.Current")
        } else {
            this.storage.set("TrackRecording.Current", this._currentTrackRecording);
        }
    }

    private loadCurrentTrackRecording(): Promise<TrackRecording> {
        return this.storage.get("TrackRecording.Current").then(trackRecording => {
            if (trackRecording) {
                return Object.assign(new TrackRecording(), trackRecording);
            }

            return null;
        });
    }

    private setTrackedPathOnMap(trackedPath: LatLng[]): Promise<any> {
        if (trackedPath.length > 1) {
            return this.map.setTrack(trackedPath).then(() => this.map.panToTrack());
        }

        return Promise.resolve();
    }

    private refreshValues(): Promise<any> {
        if (!this._currentTrackRecording) {
            return Promise.resolve();
        }

        return this.trackRecorder.getLocations().then(positions => {
            if (!positions.length) {
                return Promise.resolve();
            }

            this._lastUpdate = new Date().toLocaleString("de");

            if (positions.length !== this._currentTrackRecording.trackedPositions.length) {
                const trackedPath = positions.map(position => new LatLng(position.latitude, position.longitude));
                this._currentTrackRecording.trackedPositions = trackedPath;

                const computedTrackLength = GeoHelper.Haversine.computeDistance(trackedPath);

                const usefulTrackLength = GeoHelper.Length.convertToMoreUsefulUnit(computedTrackLength);
                const lengthUnitText = GeoHelper.LengthUnitHelper.getUnitTextFromUnit(usefulTrackLength.lengthUnit);
                switch (usefulTrackLength.lengthUnit) {
                    case GeoHelper.LengthUnit.Kilometers:
                        this._approximateTrackLength = `${(+usefulTrackLength.usefulUnit.toFixed(3)).toLocaleString("de")} ${lengthUnitText}`;
                        break;
                    case GeoHelper.LengthUnit.Meters:
                        this._approximateTrackLength = `${(+usefulTrackLength.usefulUnit.toFixed(1)).toLocaleString("de")} ${lengthUnitText}`;
                        break;
                }

                return this.setTrackedPathOnMap(trackedPath).then(() => this.savePageState()).then(() => this.saveCurrentTrackRecording());
            }
        });
    }

    // tslint:disable-next-line:no-unused-variable Used inside template.
    private showTrackedPositionsInfo(): void {
        if (!this._currentTrackRecording || this._currentTrackRecording.trackedPositions.length < 2) {
            return;
        }

        const trackedPositionsInfoAlert = this.toastController.create(<ToastOptions>{
            message: `Basierend auf ${this._currentTrackRecording.trackedPositions.length} Positionen`,
            duration: 3000,
            position: "middle",
            closeButtonText: "Ok",
            showCloseButton: true
        });
        trackedPositionsInfoAlert.present();
    }

    // tslint:disable-next-line:no-unused-variable Used inside template.
    private refreshLastLocationDisplay(refresher: Refresher): void {
        this.refreshValues().then(() => refresher.complete());
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

            this.trackRecorder.setSettings(data.settings).then(settings => this.saveTrackRecorderSettings(settings));
        });
        trackRecorderSettingsModal.present();
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

                        this.refreshValues();
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
                            content: "Wird erstellt...",
                        });
                        uploadTrackRecordingLoading.present();

                        this.trackRecorder.getLocations().then(positions => this.trackUploader.uploadRecordedTrack(positions, this._currentTrackRecording.trackName, this._currentTrackRecording.trackingStartedAt).then(() => this.trackRecorder.deleteAllRecordings()).then(() => {
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

                            this.refreshValues();
                        }));
                    }
                }
            ]
        });
        resetRecordingPrompt.present();
    }

    private resetView(): void {
        this.map.resetTrack();
        this._currentTrackRecording = null;
        this._lastUpdate = null;
        this._approximateTrackLength = null;

        this.savePageState();
        this.saveCurrentTrackRecording();
    }

    private get currentTrackRecording(): TrackRecording | null {
        return this._currentTrackRecording;
    }

    private get canDeleteTrackRecording(): boolean {
        return this.trackingIsStopped && !!this._currentTrackRecording;
    }

    private get canUploadTrackRecording(): boolean {
        return this.trackingIsStopped && !!this._currentTrackRecording && this.currentTrackRecording.trackedPositions.length> 1;
    }

    private get canShowTrackRecorderSettings(): boolean {
        return this.trackingIsStopped;
    }

    private get lastUpdate(): string | null {
        return this._lastUpdate;
    }

    private get approximateLength(): string | null {
        return this._approximateTrackLength;
    }

    private get isStopped(): boolean {
        return this.trackingIsStopped;
    }

    private stopTrackRecorder(): Promise<any> {
        return this.trackRecorder.stop().then(() => {
            this.trackingIsStopped = true;

            this.refreshValues();
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
                    if (!this._currentTrackRecording) {
                        this._currentTrackRecording = new TrackRecording();
                        this._currentTrackRecording.trackingStartedAt = new Date();
                        this._currentTrackRecording.trackName = `Strecke vom ${this._currentTrackRecording.trackingStartedAt.toLocaleString()}`;

                        this.saveCurrentTrackRecording();
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