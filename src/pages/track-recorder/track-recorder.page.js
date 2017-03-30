var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
import { LoadingController } from "ionic-angular/components/loading/loading";
import { Events, Platform, ViewController } from "ionic-angular";
import { Component, ViewChild } from "@angular/core";
import { AlertController } from "ionic-angular/components/alert/alert";
import { LatLng } from "@ionic-native/google-maps";
import { MapComponent } from "../../components/map/map.component";
import { ModalController } from "ionic-angular/components/modal/modal";
import { RecordedTrackUploader } from "./recorded-track-uploader";
import { ToastController } from "ionic-angular/components/toast/toast";
import { TrackRecorder } from "./track-recorder";
import { Storage } from "@ionic/storage";
import { TrackRecorderSettingsComponent } from "../../components/track-recorder-settings/track-recorder-settings.component";
var TrackRecorderPage = (function () {
    function TrackRecorderPage(viewController, platform, alertController, trackRecorder, modalController, toastController, recordedTrackUploader, loadingController, storage, events) {
        var _this = this;
        this.alertController = alertController;
        this.trackRecorder = trackRecorder;
        this.modalController = modalController;
        this.toastController = toastController;
        this.recordedTrackUploader = recordedTrackUploader;
        this.loadingController = loadingController;
        this.storage = storage;
        this.trackingIsStopped = true;
        this.trackRecorder.debugging();
        viewController.willLeave.subscribe(function () {
            _this.stop();
        });
        events.subscribe("TrackRecorder-LocationMode", function (enabled) {
            if (!enabled) {
                _this.stopTrackRecorder().then(function () {
                    var trackingStoppedTaost = _this.toastController.create({
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
    TrackRecorderPage.prototype.refreshLastLocationDisplay = function (refresher) {
        var _this = this;
        if (refresher === void 0) { refresher = null; }
        this.trackRecorder.getPositions().then(function (positions) {
            _this.recordedPositions = positions.length;
            _this.lastRecordedLatitude = _this.trackRecorder.lastRecordedLatitude;
            _this.lastRecordedLongitude = _this.trackRecorder.lastRecordedLongitude;
            var trackedPath = positions.map(function (position) { return new LatLng(position.latitude, position.longitude); });
            if (trackedPath.length > 1) {
                _this.map.setTrack(trackedPath).then(function () { return _this.map.panToTrack(); });
            }
            if (refresher) {
                refresher.complete();
            }
        });
    };
    // tslint:disable-next-line:no-unused-variable Used inside template.
    TrackRecorderPage.prototype.showTrackRecorderSettings = function (event) {
        var _this = this;
        var recorderSettings = this.trackRecorder.settings;
        var trackRecorderSettingsModal = this.modalController.create(TrackRecorderSettingsComponent, {
            settings: recorderSettings
        });
        trackRecorderSettingsModal.onDidDismiss(function (data) {
            if (!data) {
                return;
            }
            var setTrackRecorderSettingsToast = _this.toastController.create({
                message: "Einstellungen akzeptiert",
                duration: 3000,
                position: "middle",
            });
            setTrackRecorderSettingsToast.present();
            _this.trackRecorder.setSettings(data.settings);
        });
        trackRecorderSettingsModal.present();
    };
    // tslint:disable-next-line:no-unused-variable Used inside template.
    TrackRecorderPage.prototype.resetTrackRecording = function () {
        var _this = this;
        var resetRecordingPrompt = this.alertController.create({
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
                    handler: function () { return _this.trackRecorder.deleteAllRecordings().then(function () {
                        var allRecordingsDeletedToast = _this.toastController.create({
                            message: "Strecke gelöscht",
                            duration: 3000,
                            position: "middle"
                        });
                        allRecordingsDeletedToast.present();
                        _this.map.resetTrack();
                        _this.refreshLastLocationDisplay();
                    }); }
                }
            ]
        });
        resetRecordingPrompt.present();
    };
    // tslint:disable-next-line:no-unused-variable Used inside template.
    TrackRecorderPage.prototype.uploadTrackRecording = function () {
        var _this = this;
        var resetRecordingPrompt = this.alertController.create({
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
                    handler: function () {
                        var uploadTrackRecordingLoading = _this.loadingController.create({
                            content: "Wird hochgeladen...",
                        });
                        uploadTrackRecordingLoading.present();
                        _this.trackRecorder.getPositions().then(function (positions) {
                            _this.recordedTrackUploader.uploadRecordedTrack(positions, _this.trackRecorder.startedAt).then(function () { return _this.trackRecorder.deleteAllRecordings(); }).then(function () {
                                uploadTrackRecordingLoading.dismiss();
                                var uploadedSuccessfulToast = _this.toastController.create({
                                    closeButtonText: "Toll",
                                    message: "Strecke erfolgreich hochgeladen",
                                    position: "middle",
                                    duration: 3000
                                });
                                uploadedSuccessfulToast.present();
                                _this.map.resetTrack();
                                _this.refreshLastLocationDisplay();
                            });
                        });
                    }
                }
            ]
        });
        resetRecordingPrompt.present();
    };
    Object.defineProperty(TrackRecorderPage.prototype, "canDeleteTrackRecording", {
        get: function () {
            return this.trackingIsStopped && this.countOfCollectedPositions > 0;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TrackRecorderPage.prototype, "canUploadTrackRecording", {
        get: function () {
            return this.trackingIsStopped && this.countOfCollectedPositions > 0;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TrackRecorderPage.prototype, "canShowTrackRecorderSettings", {
        get: function () {
            return this.trackingIsStopped;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TrackRecorderPage.prototype, "lastLatitude", {
        get: function () {
            return this.lastRecordedLatitude;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TrackRecorderPage.prototype, "lastLongitude", {
        get: function () {
            return this.lastRecordedLongitude;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TrackRecorderPage.prototype, "countOfCollectedPositions", {
        get: function () {
            return this.recordedPositions;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(TrackRecorderPage.prototype, "isStopped", {
        get: function () {
            return this.trackingIsStopped;
        },
        enumerable: true,
        configurable: true
    });
    TrackRecorderPage.prototype.stopTrackRecorder = function () {
        var _this = this;
        return this.trackRecorder.stop().then(function () {
            _this.trackingIsStopped = true;
            _this.refreshLastLocationDisplay();
        });
    };
    // tslint:disable-next-line:no-unused-variable Used inside template.
    TrackRecorderPage.prototype.stop = function () {
        this.stopTrackRecorder();
    };
    // tslint:disable-next-line:no-unused-variable Used inside template.
    TrackRecorderPage.prototype.record = function () {
        var _this = this;
        this.trackRecorder.isLocationEnabled().then(function (enabled) {
            if (enabled) {
                _this.trackRecorder.record().then(function () { return _this.trackingIsStopped = false; }, function (error) { });
            }
            else {
                var pleaseEnableLocationAlert = _this.alertController.create({
                    title: "Standort ist deaktiviert",
                    message: "Möchten Sie die Standorteinstellungen öffnen?",
                    enableBackdropDismiss: true,
                    buttons: [
                        {
                            text: "Nein",
                            role: "cancel",
                            handler: function () {
                                var pleaseEnableLocationToast = _this.toastController.create({
                                    message: "Standort aktivieren um Strecke aufzunehmen",
                                    duration: 3000,
                                    position: "middle"
                                });
                                pleaseEnableLocationToast.present();
                            }
                        },
                        {
                            text: "Ja",
                            handler: function () { return _this.trackRecorder.showLocationSettings(); }
                        }
                    ]
                });
                pleaseEnableLocationAlert.present();
            }
        });
    };
    return TrackRecorderPage;
}());
__decorate([
    ViewChild("map"),
    __metadata("design:type", MapComponent)
], TrackRecorderPage.prototype, "map", void 0);
TrackRecorderPage = __decorate([
    Component({
        selector: "track-recorder",
        templateUrl: "track-recorder.page.html"
    }),
    __metadata("design:paramtypes", [ViewController,
        Platform,
        AlertController,
        TrackRecorder,
        ModalController,
        ToastController,
        RecordedTrackUploader,
        LoadingController,
        Storage,
        Events])
], TrackRecorderPage);
export { TrackRecorderPage };
//# sourceMappingURL=track-recorder.page.js.map