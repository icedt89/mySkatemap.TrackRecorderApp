var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
import { NavParams, ViewController } from "ionic-angular";
import { Component } from "@angular/core";
import { ToastController } from "ionic-angular/components/toast/toast";
var TrackRecorderSettingsComponent = (function () {
    function TrackRecorderSettingsComponent(viewController, navigationParameters, toastController) {
        this.viewController = viewController;
        this.toastController = toastController;
        this.recorderSettings = navigationParameters.get("settings");
    }
    // tslint:disable-next-line:no-unused-variable Used inside template
    TrackRecorderSettingsComponent.prototype.apply = function () {
        this.viewController.dismiss({
            settings: this.recorderSettings
        });
    };
    Object.defineProperty(TrackRecorderSettingsComponent.prototype, "settings", {
        get: function () {
            return this.recorderSettings;
        },
        enumerable: true,
        configurable: true
    });
    TrackRecorderSettingsComponent.prototype.showDistanceFilterToast = function () {
        var distanceFilterToast = this.toastController.create({
            duration: 5000,
            position: "middle",
            showCloseButton: true,
            closeButtonText: "Danke",
            message: "Mindestbewegung in Metern die erfolgen muss damit die Position erfasst wird"
        });
        distanceFilterToast.present();
    };
    TrackRecorderSettingsComponent.prototype.showStationaryRadiusToast = function () {
        var distanceFilterToast = this.toastController.create({
            duration: 5000,
            position: "middle",
            showCloseButton: true,
            closeButtonText: "Danke",
            message: "Mindestbewegung in Metern die erfolgen muss damit die Aufzeichnung wieder beginnt"
        });
        distanceFilterToast.present();
    };
    return TrackRecorderSettingsComponent;
}());
TrackRecorderSettingsComponent = __decorate([
    Component({
        templateUrl: "track-recorder-settings.component.html"
    }),
    __metadata("design:paramtypes", [ViewController, NavParams, ToastController])
], TrackRecorderSettingsComponent);
export { TrackRecorderSettingsComponent };
//# sourceMappingURL=track-recorder-settings.component.js.map