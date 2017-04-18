import { TrackAttachment } from "../../infrastructure/track-attachment";
import { TrackRecorderPopoverModel } from "./track-recorder-popover/track-recorder-popover-model";
import { TrackRecorderPopoverComponent } from "./track-recorder-popover/track-recorder-popover.component";
import { Events, NavOptions, PopoverController } from "ionic-angular";
import { LengthUnit } from "../../infrastructure/length-unit";
import { Length } from "../../infrastructure/length";
import { LengthUnitHelper } from "../../infrastructure/lenght-unit-helper";
import { Haversine } from "../../infrastructure/haversine";
import { TrackRecorderSettings } from "../../infrastructure/track-recorder/track-recorder-settings";
import { TrackUploader } from "../../infrastructure/track-uploader";
import { TrackRecorder } from "../../infrastructure/track-recorder/track-recorder";
import {
    AlertController,
    AlertOptions,
    ModalController,
    Refresher,
    ToastController,
    ToastOptions,
    ViewController
} from "ionic-angular";
import { Component, ViewChild } from "@angular/core";

import { LatLng } from "@ionic-native/google-maps";
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
    private _isPaused = true;

    @ViewChild("map") private map: MapComponent;

    public constructor(viewController: ViewController,
        private alertController: AlertController,
        private trackRecorder: TrackRecorder,
        private modalController: ModalController,
        private toastController: ToastController,
        private trackUploader: TrackUploader,
        private popoverController: PopoverController,
        private storage: Storage,
        events: Events) {
        events.subscribe("track-attachments-changed", (attachments: TrackAttachment[]) => {
            this._currentTrackRecording.trackAttachments = attachments;

            this.saveCurrentTrackRecording().then(() => {
                const attachmentsSavedToast = this.toastController.create(<ToastOptions>{
                    message: "Anhänge gespeichert",
                    position: "bottom",
                    duration: 3000,
                    showCloseButton: true,
                    closeButtonText: "Toll"
                });
                return attachmentsSavedToast.present();
            });
        });
        events.subscribe("track-recordings-reset", (attachments: TrackAttachment[]) => {
            this.resetView().then(() => {
                const allRecordingsDeletedToast = this.toastController.create(<ToastOptions>{
                    message: "Strecke gelöscht",
                    duration: 3000,
                    position: "bottom",
                    showCloseButton: true,
                    closeButtonText: "Ok"
                });
                return allRecordingsDeletedToast.present();
            });
        });
        events.subscribe("track-recordings-uploaded-success", (attachments: TrackAttachment[]) => {
            this.resetView().then(() => {
                const uploadedSuccessfulToast = this.toastController.create(<ToastOptions>{
                    message: "Strecke erfolgreich hochgeladen",
                    position: "bottom",
                    duration: 3000,
                    showCloseButton: true,
                    closeButtonText: "Toll"
                });
                return uploadedSuccessfulToast.present();
            });
        });
        events.subscribe("track-recordings-uploaded-failed", (attachments: TrackAttachment[]) => {
            const uploadedSuccessfulToast = this.toastController.create(<ToastOptions>{
                message: "Fehler beim hochladen",
                position: "bottom",
                duration: 3000,
                showCloseButton: true,
                closeButtonText: "Verdammt"
            });
            return uploadedSuccessfulToast.present();
        });
        viewController.willEnter.subscribe(() => {
            this.loadPageState();
            this.loadTrackRecorderSettings().then(settings => {
                if (!settings) {
                    return;
                }

                this.trackRecorder.ready.then(() => this.trackRecorder.setSettings(settings));
            });
            this.loadCurrentTrackRecording().then(trackRecording => {
                if (!trackRecording) {
                    return;
                }

                this._currentTrackRecording = trackRecording;

                this.map.mapReady.then(() => this.setTrackedPathOnMap(this._currentTrackRecording.trackedPositions));
            });
        });

        viewController.willLeave.subscribe(() => {
            this.savePageState();
            this.saveCurrentTrackRecording();
        });

        this.trackRecorder.locationModeChanged.subscribe(enabled => {
            if (!enabled && !this._isPaused) {
                this.pauseTrackRecorder().then(() => {
                    const trackingStoppedToast = this.toastController.create(<ToastOptions>{
                        message: "Standort wurde deaktiviert. Aufnahme ist pausiert.",
                        duration: 3000,
                        position: "bottom",
                        closeButtonText: "Ok",
                        showCloseButton: true
                    });
                    trackingStoppedToast.present();
                });
            }
        });
    }

    private savePageState(): Promise<any> {
        const results: Promise<any>[] = [];

        if (!this._lastUpdate) {
            results.push(this.storage.remove("TrackRecorderPage.LastUpdate"));
        } else {
            results.push(this.storage.set("TrackRecorderPage.LastUpdate", this._lastUpdate));
        }

        if (!this._approximateTrackLength) {
            results.push(this.storage.remove("TrackRecorderPage.ApproximateTrackLength"));
        } else {
            results.push(this.storage.set("TrackRecorderPage.ApproximateTrackLength", this._approximateTrackLength));
        }

        return Promise.all(results);
    }

    private loadPageState(): Promise<any> {
        const results: Promise<any>[] = [];

        results.push(this.storage.get("TrackRecorderPage.LastUpdate").then((value: string | null) => {
            this._lastUpdate = value;
        }));
        results.push(this.storage.get("TrackRecorderPage.ApproximateTrackLength").then((value: string | null) => {
            this._approximateTrackLength = value;
        }));

        return Promise.all(results);
    }

    private saveTrackRecorderSettings(settings: TrackRecorderSettings): void {
        if (!settings) {
            this.storage.remove("TrackRecorder.Settings");
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

    private saveCurrentTrackRecording(): Promise<any> {
        if (!this._currentTrackRecording) {
            return this.storage.remove("TrackRecording.Current");
        } else {
            return this.storage.set("TrackRecording.Current", this._currentTrackRecording);
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

    private setTrackedPathOnMap(trackedPath: LatLng[]): Promise<void> {
        if (trackedPath.length > 1) {
            return this.map.setTrack(trackedPath).then(() => this.map.panToTrack());
        }

        return Promise.resolve();
    }

    private refreshValues(): Promise<void> {
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

                const computedTrackLength = Haversine.computeDistance(trackedPath);

                const usefulTrackLength = Length.convertToMoreUsefulUnit(computedTrackLength);
                const lengthUnitText = LengthUnitHelper.getUnitTextFromUnit(usefulTrackLength.lengthUnit);
                switch (usefulTrackLength.lengthUnit) {
                    case LengthUnit.Kilometers:
                        this._approximateTrackLength = `${(+usefulTrackLength.usefulUnit.toFixed(3)).toLocaleString("de")} ${lengthUnitText}`;
                        break;
                    case LengthUnit.Meters:
                        this._approximateTrackLength = `${(+usefulTrackLength.usefulUnit.toFixed(1)).toLocaleString("de")} ${lengthUnitText}`;
                        break;
                }

                this.savePageState();

                return this.setTrackedPathOnMap(trackedPath).then(() => this.saveCurrentTrackRecording());
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
    private showTrackRecorderPopover(event: Event): void {
        const model = new TrackRecorderPopoverModel(this._currentTrackRecording, this._isPaused);

        let popover = this.popoverController.create(TrackRecorderPopoverComponent, {
            model: model
        });
        popover.present(<NavOptions>{
            ev: event
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
                position: "bottom",
                showCloseButton: true,
                closeButtonText: "Ok"
            });
            setTrackRecorderSettingsToast.present();

            this.trackRecorder.setSettings(data.settings).then(settings => this.saveTrackRecorderSettings(settings));
        });
        trackRecorderSettingsModal.present();
    }

    private resetView(): Promise<void> {
        this.map.resetTrack();
        this._currentTrackRecording = null;
        this._lastUpdate = null;
        this._approximateTrackLength = null;

        return this.savePageState().then(() => this.saveCurrentTrackRecording());
    }

    private get currentTrackRecording(): TrackRecording | null {
        return this._currentTrackRecording;
    }

    private get canShowTrackRecorderSettings(): boolean {
        return this._isPaused;
    }

    private get lastUpdate(): string | null {
        return this._lastUpdate;
    }

    private get approximateLength(): string | null {
        return this._approximateTrackLength;
    }

    private get isPaused(): boolean {
        return this._isPaused;
    }

    private pauseTrackRecorder(): Promise<void> {
        return this.trackRecorder.pause().then(() => {
            this._isPaused = true;

            this.refreshValues();
        });
    }

    // tslint:disable-next-line:no-unused-variable Used inside template.
    private pause(): void {
        this.pauseTrackRecorder();
    }

    // tslint:disable-next-line:no-unused-variable Used inside template.
    private record(): void {
        this.trackRecorder.isLocationEnabled().then(enabled => {
            if (enabled) {
                return this.trackRecorder.record().then(() => {
                    if (!this._currentTrackRecording) {
                        this._currentTrackRecording = new TrackRecording();
                        this._currentTrackRecording.trackingStartedAt = new Date();
                        this._currentTrackRecording.trackName = `Strecke vom ${this._currentTrackRecording.trackingStartedAt.toLocaleString()}`;

                        return this.saveCurrentTrackRecording();
                    }

                    this._isPaused = false;
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
                                    position: "bottom",
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
                return pleaseEnableLocationAlert.present();
            }
        });
    }
}