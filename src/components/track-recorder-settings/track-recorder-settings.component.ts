import { NavParams, ToastOptions, ViewController } from "ionic-angular";

import { Component } from "@angular/core";
import { ToastController } from "ionic-angular/components/toast/toast";
import { TrackRecorderSettings } from "../../app/track-recorder-settings";

@Component({
    templateUrl: "track-recorder-settings.component.html"
})
export class TrackRecorderSettingsComponent {
    private recorderSettings: TrackRecorderSettings;

    public constructor(private viewController: ViewController, navigationParameters: NavParams, private toastController: ToastController) {
        this.recorderSettings = navigationParameters.get("settings");
    }

    // tslint:disable-next-line:no-unused-variable Used inside template
    private apply(): void {
        this.viewController.dismiss({
            settings: this.recorderSettings
        });
    }

    private get settings(): TrackRecorderSettings {
        return this.recorderSettings;
    }

    public showDistanceFilterToast(): void {

        const distanceFilterToast = this.toastController.create(<ToastOptions>{
            duration: 5000,
            position: "middle",
            showCloseButton: true,
            closeButtonText: "Danke",
            message: "Mindestbewegung in Metern die erfolgen muss damit die Position erfasst wird"
        });
        distanceFilterToast.present();
    }

    public showStationaryRadiusToast(): void {
        const distanceFilterToast = this.toastController.create(<ToastOptions>{
            duration: 5000,
            position: "middle",
            showCloseButton: true,
            closeButtonText: "Danke",
            message: "Mindestbewegung in Metern die erfolgen muss damit die Aufzeichnung wieder beginnt"
        });
        distanceFilterToast.present();
    }
}