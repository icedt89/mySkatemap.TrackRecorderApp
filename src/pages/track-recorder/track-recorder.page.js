var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
import { SplashScreen } from "@ionic-native/splash-screen";
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
    function TrackRecorderPage(viewController, platform, alertController, trackRecorder, modalController, toastController, recordedTrackUploader, loadingController, storage, splashscreen, events) {
        var _this = this;
        this.alertController = alertController;
        this.trackRecorder = trackRecorder;
        this.modalController = modalController;
        this.toastController = toastController;
        this.recordedTrackUploader = recordedTrackUploader;
        this.loadingController = loadingController;
        this.storage = storage;
        this.recordedPositions = 0;
        this.trackingIsStopped = true;
        this.trackRecorder.debugging();
        viewController.didEnter.subscribe(function () { return _this.map.ready.subscribe(function () { return storage.ready().then(function () { return _this.loadCurrentState(); }).then(function () { return splashscreen.hide(); }); }); });
        events.subscribe("TrackRecorder-LocationMode", function (enabled) {
            if (!enabled && !_this.trackingIsStopped) {
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
    TrackRecorderPage.prototype.saveCurrentState = function () {
        this.storage.set("TrackRecorderPage.lastRecordedLatitude", this.lastRecordedLatitude);
        this.storage.set("TrackRecorderPage.lastRecordedLongitude", this.lastRecordedLongitude);
        this.storage.set("TrackRecorderPage.recordedPositions", this.recordedPositions);
        this.storage.set("TrackRecorderPage.trackingStartedAt", this.trackingStartedAt);
        this.storage.set("TrackRecorderPage.trackedPath", this.map.getTrack());
    };
    TrackRecorderPage.prototype.loadCurrentState = function () {
        var _this = this;
        this.storage.get("TrackRecorderPage.lastRecordedLatitude").then(function (value) {
            _this.lastRecordedLatitude = value;
        });
        this.storage.get("TrackRecorderPage.lastRecordedLongitude").then(function (value) {
            _this.lastRecordedLongitude = value;
        });
        this.storage.get("TrackRecorderPage.recordedPositions").then(function (value) {
            _this.recordedPositions = value || 0;
        });
        this.storage.get("TrackRecorderPage.trackingStartedAt").then(function (value) {
            _this.trackingStartedAt = value;
        });
        this.storage.get("TrackRecorderPage.trackedPath").then(function (value) {
            if (value) {
                _this.setTrackedPathOnMap(value);
            }
        });
    };
    TrackRecorderPage.prototype.setTrackedPathOnMap = function (trackedPath) {
        var _this = this;
        if (trackedPath.length > 1) {
            return this.map.setTrack(trackedPath).then(function () { return _this.map.panToTrack(); });
        }
        return Promise.resolve(null);
    };
    // tslint:disable-next-line:no-unused-variable Used inside template.
    TrackRecorderPage.prototype.refreshLastLocationDisplay = function (refresher) {
        var _this = this;
        if (refresher === void 0) { refresher = null; }
        this.trackRecorder.getRecorderStateInfo().then(function (positions) {
            _this.recordedPositions = positions.recordedPositions.length;
            _this.lastRecordedLatitude = positions.lastLatitude;
            _this.lastRecordedLongitude = positions.lastLongitude;
            var trackedPath = positions.recordedPositions.map(function (position) { return new LatLng(position.latitude, position.longitude); });
            _this.setTrackedPathOnMap(trackedPath).then(function () {
                _this.saveCurrentState();
            });
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
                showCloseButton: true,
                closeButtonText: "Ok"
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
                            position: "middle",
                            showCloseButton: true,
                            closeButtonText: "Ok"
                        });
                        allRecordingsDeletedToast.present();
                        _this.resetView();
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
                        _this.trackRecorder.getRecorderStateInfo().then(function (recorderStateInfo) {
                            _this.recordedTrackUploader.uploadRecordedTrack(recorderStateInfo.recordedPositions, _this.trackingStartedAt).then(function () { return _this.trackRecorder.deleteAllRecordings(); }).then(function () {
                                uploadTrackRecordingLoading.dismiss();
                                var uploadedSuccessfulToast = _this.toastController.create({
                                    message: "Strecke erfolgreich hochgeladen",
                                    position: "middle",
                                    duration: 3000,
                                    showCloseButton: true,
                                    closeButtonText: "Toll"
                                });
                                uploadedSuccessfulToast.present();
                                _this.resetView();
                                _this.refreshLastLocationDisplay();
                            });
                        });
                    }
                }
            ]
        });
        resetRecordingPrompt.present();
    };
    TrackRecorderPage.prototype.resetView = function () {
        this.map.resetTrack();
        this.lastRecordedLatitude = null;
        this.lastRecordedLongitude = null;
        this.recordedPositions = 0;
        this.trackingStartedAt = null;
        this.saveCurrentState();
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
                _this.trackRecorder.record().then(function () {
                    if (!_this.trackingStartedAt) {
                        _this.trackingStartedAt = new Date();
                        _this.saveCurrentState();
                    }
                    _this.trackingIsStopped = false;
                }, function (error) { });
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
        SplashScreen,
        Events])
], TrackRecorderPage);
export { TrackRecorderPage };
//# sourceMappingURL=track-recorder.page.js.map