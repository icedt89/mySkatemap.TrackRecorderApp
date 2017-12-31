import {
    ObservableLoggerViewerModalComponent
} from "../../components/observable-logger-viewer-modal/observable-logger-viewer-modal.component";
import { ObservableLogger } from "../../infrastructure/logging/observable-logger";
import { ILogger } from "../../infrastructure/logging/ilogger";
import {
    ShowSavedTrackRecordingModalModel
} from "./show-saved-track-recording-modal/show-saved-track-recording-modal-model";
import {
    ShowSavedTrackRecordingModalComponent
} from "./show-saved-track-recording-modal/show-saved-track-recording-modal.component";
import {
    TrackRecorderSettingsModalModel
} from "./track-recorder-settings-modal/track-recorder-settings-modal-model";
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
import { LengthUnitHelper } from "../../infrastructure/lenght-unit-helper";
import { TrackRecorderSettings } from "../../infrastructure/track-recorder/track-recorder-settings";
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
import { TrackRecorderSettingsModalComponent } from "./track-recorder-settings-modal/track-recorder-settings-modal.component";
import { LoginModalComponent } from "../../components/login-modal/login-modal.component";
import { UserProfileService } from "../../infrastructure/user-profile/user-profile-service";
import { AuthenticationHandler } from "../../infrastructure/authentication-handler";
import { UserProfileInfo } from "../../infrastructure/user-profile/user-profile-info";

@Component({
    selector: "track-recorder",
    templateUrl: "track-recorder-page.component.html"
})
export class TrackRecorderPageComponent {
    private _lastUpdate: string | null;
    private _approximateTrackLength: string | null;
    private _currentTrackRecording: TrackRecording | null;
    private _isPaused = true;
    private _archivedTrackRecordings: ArchivedTrackRecording[] = [];
    private _trackRecordings: TrackRecording[] = [];
    private _userProfileInfo: UserProfileInfo | null;

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
        private authenticationHandler: AuthenticationHandler,
        private userProfileService: UserProfileService,
        private storage: Storage,
        events: Events,
        @Inject("Logger") private logger: ILogger) {
        this.authenticationHandler.authenticationState.subscribe(async isLoggedIn => {
            if (isLoggedIn) {
                try {
                    this._userProfileInfo = await this.userProfileService.getUserProfileInfo();
                } catch (error) {
                    // TODO: Keine Internetverbindung abfangen!
                    this._userProfileInfo = null;
                }
            } else {
                this._userProfileInfo = null;
            }
        });
        archivedTrackRecordingStore.tracksChanged.subscribe(recordings => this._archivedTrackRecordings = recordings.sort((a, b) => <any>b.trackingStartedAt - <any>a.trackingStartedAt));
        trackRecordingStore.tracksChanged.subscribe(recordings => this._trackRecordings = recordings.sort((a, b) => <any>b.trackingStartedAt - <any>a.trackingStartedAt));
        events.subscribe("current-track-recording-attachments-changed", async (attachments: TrackAttachment[]) => {
            if (!this._currentTrackRecording) {
                throw new Exception("No current track recording.");
            }

            this._currentTrackRecording.trackAttachments = attachments;

            await this.saveCurrentTrackRecording();

            const attachmentsSavedToast = this.toastController.create(<ToastOptions>{
                message: "Anhänge gespeichert",
                position: "bottom",
                duration: 3000,
                showCloseButton: true,
                closeButtonText: "Toll"
            });
            attachmentsSavedToast.present();
        });
        events.subscribe("current-track-recording-discarded", async () => {
            await this.resetView();

            const allRecordingsDeletedToast = this.toastController.create(<ToastOptions>{
                message: "Strecke verworfen",
                duration: 3000,
                position: "bottom",
                showCloseButton: true,
                closeButtonText: "Ok"
            });
            allRecordingsDeletedToast.present();
        });
        events.subscribe("current-track-recording-finished", async () => {
            await this.trackRecorder.deleteAllRecordings();
            await this.resetView();

            const archivingSuccessfulToast = this.toastController.create(<ToastOptions>{
                message: "Strecke abgeschlossen",
                position: "bottom",
                duration: 3000,
                showCloseButton: true,
                closeButtonText: "Ok"
            });
            archivingSuccessfulToast.present();

            this.menuController.open();
        });
        viewController.willEnter.subscribe(async () => {
            try {
                this._userProfileInfo = await this.userProfileService.getUserProfileInfo();
            } catch (error) {
                // TODO: Keine Internetverbindung abfangen!
                this._userProfileInfo = null;
            }

            this.mapComponentAccessor.bindMapComponent(this.map);

            await Promise.all([
                this.archivedTrackRecordingStore.getTracks().then(_ => this._archivedTrackRecordings = _.sort((a, b) => <any>b.trackingStartedAt - <any>a.trackingStartedAt)),
                this.trackRecordingStore.getTracks().then(_ => this._trackRecordings = _.sort((a, b) => <any>b.trackingStartedAt - <any>a.trackingStartedAt))
            ]);

            const settings = await this.loadTrackRecorderSettings();
            if (settings) {
                await this.trackRecorder.setSettings(settings);
            }

            const trackRecording = await this.loadCurrentTrackRecording();
            if (trackRecording) {
                await this.loadPageState();

                this._currentTrackRecording = trackRecording;

                await this.setTrackedPathOnMap(trackRecording.trackedPositions.map(position => new LatLng(position.latitude, position.longitude)));
            } else {
                await this.clearPageState();
            }
        });

        /* TODO: Wahrscheinlich erst sinnvoll wenn es mehrere pages gibt.
        viewController.willLeave.subscribe(async () => {
            await Promise.all([this.savePageState(), this.saveCurrentTrackRecording()]);
        });
        */

        this.trackRecorder.locationModeChanged.subscribe(async enabled => {
            if (!enabled && !this._isPaused) {
                await this.pauseTrackRecorder();

                const trackingStoppedToast = this.toastController.create(<ToastOptions>{
                    message: "Standort wurde deaktiviert. Aufnahme ist pausiert.",
                    duration: 3000,
                    position: "bottom",
                    closeButtonText: "Ok",
                    showCloseButton: true
                });
                trackingStoppedToast.present();
            }
        });
    }

    // tslint:disable-next-line:no-unused-variable Used inside template.
    private async showLogin(): Promise<void> {
        this.menuController.close();

        const showLoginModal = this.modalController.create(LoginModalComponent);

        showLoginModal.present();
    }

    // tslint:disable-next-line:no-unused-variable Used inside template.
    private deleteTrackRecording(track: TrackRecording): void {
        const removeTrackPrompt = this.alertController.create(<AlertOptions>{
            title: "Strecke löschen",
            message: "Du hast die Strecke noch nicht hochgeladen!<br /><br />Alle aufgezeichneten Daten sowie Anhänge zur Strecke gehen verloren.",
            enableBackdropDismiss: true,
            buttons: [
                {
                    text: "Abbrechen",
                    role: "cancel"
                },
                {
                    text: "Ja",
                    handler: async () => {
                        await this.trackRecordingStore.deleteStoredTrack(track);

                        const trackDeletedToast = this.toastController.create({
                            message: "Strecke gelöscht",
                            duration: 3000,
                            position: "bottom",
                            closeButtonText: "Rückgängig",
                            showCloseButton: true,
                        });
                        trackDeletedToast.onDidDismiss(async (_, initiator) => {
                            if (initiator === "close") {
                                await this.trackRecordingStore.storeTrack(track);
                            }
                        });

                        trackDeletedToast.present();
                    }
                }
            ]
        });
        // Close popover (makes back button working again Oo, too)
        removeTrackPrompt.present();
    }

    // tslint:disable-next-line:no-unused-variable Used inside template.
    private async deleteArchivedTrackRecording(track: ArchivedTrackRecording): Promise<void> {
        await this.archivedTrackRecordingStore.deleteStoredTrack(track);

        const trackDeletedToast = this.toastController.create({
            message: "Strecke gelöscht",
            duration: 3000,
            position: "bottom",
            closeButtonText: "Rückgängig",
            showCloseButton: true
        });
        trackDeletedToast.onDidDismiss(async (_, initiator) => {
            if (initiator === "close") {
                await this.archivedTrackRecordingStore.storeTrack(track);
            }
        });

        trackDeletedToast.present();
    }

    private async uploadTrackRecordingWithRetry(trackRecording: TrackRecording): Promise<void> {
        const uploadTrackRecordingLoading = this.loadingController.create(<LoadingOptions>{
            content: "Wird hochgeladen...",
        });

        uploadTrackRecordingLoading.present();

        try {
            const trackUploadedAt = await this.trackUploader.uploadRecordedTrack(trackRecording);

            await this.trackRecordingStore.deleteStoredTrack(trackRecording);

            const archivedTrackRecording = ArchivedTrackRecording.fromTrackRecording(trackRecording, trackUploadedAt);

            await this.archivedTrackRecordingStore.storeTrack(archivedTrackRecording);

            const trackRecordingUploadedToast = this.toastController.create(<ToastOptions>{
                message: "Hochladen erfolgreich",
                duration: 3000,
                position: "bottom",
                closeButtonText: "Super",
                showCloseButton: true
            });

            await uploadTrackRecordingLoading.dismiss();
            trackRecordingUploadedToast.present();
        } catch (e) {
            this.logger.error(e);

            const trackRecordingUploadFailedToast = this.toastController.create(<ToastOptions>{
                message: "Hochladen fehlgeschlagen",
                duration: 3000,
                position: "bottom",
                closeButtonText: "Erneut versuchen",
                showCloseButton: true
            });
            trackRecordingUploadFailedToast.onDidDismiss(async (_, initiator) => {
                if (initiator === "close") {
                    // TODO: Use a loop to avoid potential deep call stack and recursion.
                    await this.uploadTrackRecordingWithRetry(trackRecording);
                }
            });

            await uploadTrackRecordingLoading.dismiss();
            trackRecordingUploadFailedToast.present();
        }
    }

    // tslint:disable-next-line:no-unused-variable Used inside template.
    private uploadTrackRecording(trackRecording: TrackRecording): void {
        const uploadRecordingPrompt = this.alertController.create(<AlertOptions>{
            title: "Strecke hochladen",
            message: "Möchtst du die aufgezeichnete Strecke hochladen?",
            enableBackdropDismiss: true,
            buttons: [
                {
                    text: "Abbrechen",
                    role: "cancel"
                },
                {
                    text: "Ja",
                    handler: async () => {
                        await this.uploadTrackRecordingWithRetry(trackRecording);
                    }
                }
            ]
        });
        uploadRecordingPrompt.present();
    }

    private async setTrackedPathOnMap(trackedPath: LatLng[]): Promise<void> {
        if (trackedPath.length > 1) {
            await this.mapComponentAccessor.setTrack(trackedPath);
            await this.mapComponentAccessor.panToTrack();
        }
    }

    private async refreshValues(): Promise<void> {
        if (!this._currentTrackRecording) {
            throw new Exception("No current track recording.");
        }

        const positions = await this.trackRecorder.getLocations();
        if (!positions.length) {
            throw new Exception("No positions available.");
        }

        this._lastUpdate = new Date().toLocaleString("de");

        if (positions.length !== this._currentTrackRecording.trackedPositions.length) {
            this._currentTrackRecording.trackedPositions = positions;

            const trackedPath = positions.map(position => new LatLng(position.latitude, position.longitude));
            this._approximateTrackLength = LengthUnitHelper.formatTrackLength(trackedPath);

            await Promise.all([this.savePageState(), this.saveCurrentTrackRecording(), this.setTrackedPathOnMap(trackedPath)]);
        }
    }

    // tslint:disable-next-line:no-unused-variable Used inside template.
    private async refreshLastLocationDisplay(refresher: Refresher): Promise<void> {
        await this.refreshValues();

        refresher.complete();
    }

    // tslint:disable-next-line:no-unused-variable Used inside template.
    private showTrackRecorderPopover(event: Event): void {
        const model = new TrackRecorderPopoverModel(this._currentTrackRecording, this._isPaused);

        const popover = this.popoverController.create(TrackRecorderPopoverComponent, {
            model: model
        });
        popover.present(<NavOptions>{
            ev: event
        });
    }

    // tslint:disable-next-line:no-unused-variable Used inside template.
    private showSavedTrackRecording(trackRecording: ArchivedTrackRecording | TrackRecording): void {
        this.menuController.close();

        const showSavedTrackRecordingModal = this.modalController.create(ShowSavedTrackRecordingModalComponent, {
            model: new ShowSavedTrackRecordingModalModel(trackRecording)
        });
        showSavedTrackRecordingModal.present();
    }

    // tslint:disable-next-line:no-unused-variable Used inside template.
    private showObservableLoggerViewer(): void {
        this.menuController.close();

        const observableLoggerViewModal = this.modalController.create(ObservableLoggerViewerModalComponent);

        observableLoggerViewModal.present();
    }

    // tslint:disable-next-line:no-unused-variable Used inside template.
    private showTrackRecorderSettings(): void {
        this.menuController.close();

        const recorderSettings = this.trackRecorder.settings;

        const trackRecorderSettingsModal = this.modalController.create(TrackRecorderSettingsModalComponent, {
            model: new TrackRecorderSettingsModalModel(recorderSettings)
        });
        trackRecorderSettingsModal.onDidDismiss(async (data: { model: TrackRecorderSettingsModalModel } | null) => {
            if (!data) {
                // Modal was not successfully dismissed (user, used back button...).
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

            const trackRecorderSettings = new TrackRecorderSettings();
            trackRecorderSettings.desiredAccuracy = data.model.desiredAccuracy;
            trackRecorderSettings.distanceFilter = data.model.distanceFilter;
            trackRecorderSettings.locationProvider = data.model.locationProvider;
            trackRecorderSettings.stationaryRadius = data.model.stationaryRadius;

            const settings = await this.trackRecorder.setSettings(trackRecorderSettings);
            await this.saveTrackRecorderSettings(settings);
        });
        trackRecorderSettingsModal.present();
    }

    private async resetView(): Promise<void> {
        this.mapComponentAccessor.resetTrack();

        await Promise.all([this.clearPageState(), this.clearCurrentTrackRecording()]);
    }

    private get currentTrackRecording(): TrackRecording | null {
        return this._currentTrackRecording;
    }

    private get trackRecordings(): TrackRecording[] {
        return this._trackRecordings;
    }

    private get userProfileInfo(): UserProfileInfo {
        return this._userProfileInfo;
    }

    private get archivedTrackRecordings(): ArchivedTrackRecording[] {
        return this._archivedTrackRecordings;
    }

    private get isLoggerObservable(): boolean {
        return this.logger instanceof ObservableLogger;
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

    private async pauseTrackRecorder(): Promise<void> {
        await this.trackRecorder.pause();

        this._isPaused = true;

        await this.refreshValues();
    }

    // tslint:disable-next-line:no-unused-variable Used inside template.
    private async pause(): Promise<void> {
        await this.pauseTrackRecorder();
    }

    // tslint:disable-next-line:no-unused-variable Used inside template.
    private async record(): Promise<void> {
        const enabled = await this.trackRecorder.isLocationEnabled();
        if (enabled) {
            await this.trackRecorder.record();
            try {
                if (!this._currentTrackRecording) {
                    this._currentTrackRecording = new TrackRecording();
                    this._currentTrackRecording.trackingStartedAt = new Date();

                    this._currentTrackRecording.trackName = `Strecke ${this._currentTrackRecording.trackingStartedAt.toLocaleString("de")}`;

                    await this.saveCurrentTrackRecording();
                }

                this._isPaused = false;
            } catch (any) { }
        } else {
            const pleaseEnableLocationAlert = this.alertController.create(<AlertOptions>{
                title: "Standort ist deaktiviert",
                message: "Möchtest du die Standorteinstellungen öffnen?",
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
            pleaseEnableLocationAlert.present();
        }
    }

    /* PAGE STATE */
    private async savePageState(): Promise<void> {
        const promises: Promise<void>[] = [];

        if (!this._lastUpdate) {
            promises.push(this.storage.remove("TrackRecorderPage.LastUpdate"));
        } else {
            promises.push(this.storage.set("TrackRecorderPage.LastUpdate", this._lastUpdate));
        }

        if (!this._approximateTrackLength) {
            promises.push(this.storage.remove("TrackRecorderPage.ApproximateTrackLength"));
        } else {
            promises.push(this.storage.set("TrackRecorderPage.ApproximateTrackLength", this._approximateTrackLength));
        }

        await Promise.all(promises);
    }

    private async clearPageState(): Promise<void> {
        this._approximateTrackLength = null;
        this._lastUpdate = null;

        await this.savePageState();
    }

    private async loadPageState(): Promise<void> {
        this._lastUpdate = await this.storage.get("TrackRecorderPage.LastUpdate");
        this._approximateTrackLength = await this.storage.get("TrackRecorderPage.ApproximateTrackLength");
    }

    /* CURRENT TRACK RECORDING */
    private async saveCurrentTrackRecording(): Promise<void> {
        if (!this._currentTrackRecording) {
            await this.storage.remove("TrackRecording.Current");
        } else {
            await this.storage.set("TrackRecording.Current", this._currentTrackRecording);
        }
    }

    private async loadCurrentTrackRecording(): Promise<TrackRecording> {
        const trackRecording = await this.storage.get("TrackRecording.Current");

        if (trackRecording) {
            return TrackRecording.fromLike(trackRecording);
        }

        return null;
    }

    private async clearCurrentTrackRecording(): Promise<void> {
        this._currentTrackRecording = null;

        await this.saveCurrentTrackRecording();
    }

    /* TRACK RECORDER SETTINGS */
    private async saveTrackRecorderSettings(settings: TrackRecorderSettings): Promise<void> {
        if (!settings) {
            await this.storage.remove("TrackRecorder.Settings");
        } else {
            await this.storage.set("TrackRecorder.Settings", settings);
        }
    }

    private async loadTrackRecorderSettings(): Promise<TrackRecorderSettings> {
        const settings = await this.storage.get("TrackRecorder.Settings");
        if (settings) {
            return TrackRecorderSettings.fromLike(settings);
        }

        return null;
    }
}