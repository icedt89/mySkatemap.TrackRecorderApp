import { IMapComponentAccessor } from "../../components/map/imap-component-accessor";
import { ITrackUploader } from "../../infrastructure/track-uploader/itrack-uploader";
import { Inject } from "@angular/core";
import { ITrackRecorder } from "../../infrastructure/track-recorder/itrack-recorder";
import { ArchivedTrackRecording } from "../../infrastructure/archived-track-recording";
import { TrackRecordingStore } from "../../infrastructure/track-store/track-recording-store";
import { ArchivedTrackRecordingStore } from "../../infrastructure/track-store/archived-track-recording-store";
import { TrackRecording } from "../../infrastructure/track-recording";
import { Exception } from "../../infrastructure/exception";
import { TrackAttachment } from "../../infrastructure/track-attachment";
import { TrackRecorderPopoverModel } from "./track-recorder-popover/track-recorder-popover-model";
import { TrackRecorderPopoverComponent } from "./track-recorder-popover/track-recorder-popover.component";
import {
    Events,
    LoadingController,
    LoadingOptions,
    MenuController,
    NavOptions,
    PopoverController
} from "ionic-angular";
import { LengthUnit } from "../../infrastructure/length-unit";
import { Length } from "../../infrastructure/length";
import { LengthUnitHelper } from "../../infrastructure/lenght-unit-helper";
import { Haversine } from "../../infrastructure/haversine";
import { TrackRecorderSettings } from "../../infrastructure/track-recorder/track-recorder-settings";
import * as moment from "moment";
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
import { TrackRecorderSettingsModalComponent } from "../../components/track-recorder-settings-modal/track-recorder-settings-modal.component";

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
    private _archivedTrackRecordings: ArchivedTrackRecording[] = [];
    private _trackRecordings: TrackRecording[] = [];

    @ViewChild("map") private map: MapComponent;

    public constructor(viewController: ViewController,
        private alertController: AlertController,
        @Inject("TrackRecorder") private trackRecorder: ITrackRecorder,
        private modalController: ModalController,
        private toastController: ToastController,
        @Inject("TrackUploader") private trackUploader: ITrackUploader,
        @Inject("MapComponentAccessor") private mapComponentAccessor: IMapComponentAccessor,
        private popoverController: PopoverController,
        private menuController: MenuController,
        private loadingController: LoadingController,
        private archivedTrackRecordingStore: ArchivedTrackRecordingStore,
        private trackRecordingStore: TrackRecordingStore,
        private storage: Storage,
        events: Events) {
        archivedTrackRecordingStore.tracksChanged.subscribe(recordings => this._archivedTrackRecordings = recordings);
        trackRecordingStore.tracksChanged.subscribe(recordings => this._trackRecordings = recordings);
        events.subscribe("track-attachments-changed", (attachments: TrackAttachment[]) => {
            if (!this._currentTrackRecording) {
                return;
            }

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
        events.subscribe("track-recording-reset", () => {
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
        events.subscribe("track-recording-finished", () => {
            this.resetView().then(() => {
                const archivingSuccessfulToast = this.toastController.create(<ToastOptions>{
                    message: "Strecke abgeschlossen",
                    position: "bottom",
                    duration: 3000,
                    showCloseButton: true,
                    closeButtonText: "Ok"
                });
                return archivingSuccessfulToast.present();
            });
        });
        viewController.willEnter.subscribe(() => {
            this.mapComponentAccessor.bindMapComponent(this.map);

            this.archivedTrackRecordingStore.getTracks().then(tracks => this._archivedTrackRecordings = tracks);
            this.trackRecordingStore.getTracks().then(tracks => this._trackRecordings = tracks);

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

                this.mapComponentAccessor.mapReady.then(() => this.setTrackedPathOnMap(trackRecording.trackedPositions.map(position => new LatLng(position.latitude, position.longitude))));
            });
        });

        viewController.willLeave.subscribe(() => {
            debugger;

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
                TrackRecorderSettings.fromLike(settings);
            }

            return null;
        });
    }

    // tslint:disable-next-line:no-unused-variable Used inside template.
    private deleteTrackRecording(track: TrackRecording): void {
        const removeTrackPrompt = this.alertController.create(<AlertOptions>{
            title: "Strecke löschen",
            message: "Sie haben die Strecke noch nicht hochgeladen!<br /><br />Alle aufgezeichneten Daten sowie Anhänge zur Strecke gehen verloren.",
            enableBackdropDismiss: true,
            buttons: [
                {
                    text: "Abbrechen",
                    role: "cancel"
                },
                {
                    text: "Ja",
                    handler: () => this.trackRecordingStore.deleteStoredTrack(track).then(() => {
                        const trackDeletedToast = this.toastController.create({
                            message: "Strecke gelöscht",
                            duration: 3000,
                            position: "bottom",
                            closeButtonText: "Rückgängig",
                            showCloseButton: true,
                        });
                        trackDeletedToast.onDidDismiss((_, initiator) => {
                            if (initiator === "close") {
                                this.trackRecordingStore.storeTrack(track);
                            }
                        });

                        return trackDeletedToast.present();
                    })
                }
            ]
        });
        // Close popover (makes back button working again Oo, too)
        removeTrackPrompt.present();
    }

    // tslint:disable-next-line:no-unused-variable Used inside template.
    private deleteArchivedTrackRecording(track: ArchivedTrackRecording): void {
        this.archivedTrackRecordingStore.deleteStoredTrack(track).then(() => {
            const trackDeletedToast = this.toastController.create({
                message: "Strecke gelöscht",
                duration: 3000,
                position: "bottom",
                closeButtonText: "Rückgängig",
                showCloseButton: true
            });
            trackDeletedToast.onDidDismiss((_, initiator) => {
                if (initiator === "close") {
                    this.archivedTrackRecordingStore.storeTrack(track);
                }
            });

            return trackDeletedToast.present();
        });
        /*
        const removeTrackPrompt = this.alertController.create(<AlertOptions>{
            title: "Strecke löschen",
            message: "Alle aufgezeichneten Daten sowie Anhänge zur Strecke gehen verloren.",
            enableBackdropDismiss: true,
            buttons: [
                {
                    text: "Abbrechen",
                    role: "cancel"
                },
                {
                    text: "Ja",
                    handler: () => this.archivedTrackRecordingStore.deleteStoredTrack(track).then(() => {
                        const trackDeletedToast = this.toastController.create({
                            message: "Strecke gelöscht",
                            duration: 3000,
                            position: "bottom",
                            closeButtonText: "Rückgängig",
                            showCloseButton: true
                        });
                        trackDeletedToast.onDidDismiss((_, initiator) => {
                            if (initiator === "close") {
                                this.archivedTrackRecordingStore.storeTrack(track);
                            }
                        });

                        return trackDeletedToast.present();
                    })
                }
            ]
        });
        // Close popover (makes back button working again Oo, too)
        removeTrackPrompt.present();
        */
    }

    // tslint:disable-next-line:no-unused-variable Used inside template.
    private uploadTrackRecording(trackRecording: TrackRecording): void {
        const uploadRecordingPrompt = this.alertController.create({
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

                        uploadTrackRecordingLoading.present().then(() => this.trackUploader.uploadRecordedTrack(trackRecording).then(trackUploadedAt => {
                            return this.trackRecordingStore.deleteStoredTrack(trackRecording).then(() => {
                                const archivedTrackRecording = ArchivedTrackRecording.fromTrackRecording(trackRecording, trackUploadedAt);

                                return this.archivedTrackRecordingStore.storeTrack(archivedTrackRecording);
                            }).then(() => this.resetView().then(() => {
                                const trackRecordingUploadedToast = this.toastController.create(<ToastOptions>{
                                    message: "Hochladen erfolgreich",
                                    duration: 3000,
                                    position: "bottom",
                                    closeButtonText: "Super",
                                    showCloseButton: true
                                });

                                return uploadTrackRecordingLoading.dismiss().then(() => trackRecordingUploadedToast.present());
                            }));
                        }, () => {
                            const trackRecordingUploadFailedToast = this.toastController.create(<ToastOptions>{
                                message: "Hochladen fehlgeschlagen",
                                duration: 3000,
                                position: "bottom",
                                closeButtonText: "Erneut versuchen",
                                showCloseButton: true
                            });
                            trackRecordingUploadFailedToast.onDidDismiss((_, initiator) => {
                                if (initiator === "close") {
                                    debugger;
                                }
                            });

                            return uploadTrackRecordingLoading.dismiss().then(() => trackRecordingUploadFailedToast.present());
                        }));
                    }
                }
            ]
        });
        uploadRecordingPrompt.present();
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
                return TrackRecording.fromLike(trackRecording);
            }

            return null;
        });
    }

    private setTrackedPathOnMap(trackedPath: LatLng[]): Promise<void> {
        if (trackedPath.length > 1) {
            return this.mapComponentAccessor.setTrack(trackedPath).then(() => this.mapComponentAccessor.panToTrack());
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

            if (!this._currentTrackRecording) {
                throw new Exception("No current track recording.");
            }

            this._lastUpdate = new Date().toLocaleString("de");

            if (positions.length !== this._currentTrackRecording.trackedPositions.length) {
                this._currentTrackRecording.trackedPositions = positions;

                const trackedPath = positions.map(position => new LatLng(position.latitude, position.longitude));
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

                this.setTrackedPathOnMap(trackedPath);

                return <Promise<void>>this.savePageState().then(() => this.saveCurrentTrackRecording());
            }

            return Promise.resolve();
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
    private showTrackRecorderSettings(): void {
        this.menuController.close().then(() => {
            const recorderSettings = this.trackRecorder.settings;

            const trackRecorderSettingsModal = this.modalController.create(TrackRecorderSettingsModalComponent, {
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
        });
    }

    private resetView(): Promise<void> {
        this.mapComponentAccessor.resetTrack();
        this._currentTrackRecording = null;
        this._lastUpdate = null;
        this._approximateTrackLength = null;

        return this.savePageState().then(() => this.saveCurrentTrackRecording());
    }

    private get currentTrackRecording(): TrackRecording | null {
        return this._currentTrackRecording;
    }

    private get trackRecordings(): TrackRecording[] {
        return this._trackRecordings;
    }

    private get archivedTrackRecordings(): ArchivedTrackRecording[] {
        return this._archivedTrackRecordings;
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

                        this._currentTrackRecording.trackName = `Strecke ${this._currentTrackRecording.trackingStartedAt.toLocaleString("de")}`;

                        return this.saveCurrentTrackRecording().then(() => this._isPaused = false);
                    }

                    this._isPaused = false;
                }).catch(() => { });
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