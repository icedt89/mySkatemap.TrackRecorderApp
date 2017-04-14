import { TrackRecorderSettings } from "../../infrastructure/track-recorder/track-recorder-settings";
import { NavParams, ToastController, ToastOptions, ViewController } from "ionic-angular";
import { Component } from "@angular/core";

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

    private showDistanceFilterToast(): void {
        const distanceFilterToast = this.toastController.create(<ToastOptions>{
            duration: 5000,
            position: "middle",
            showCloseButton: true,
            closeButtonText: "Danke",
            message: "Mindestbewegung in Metern die erfolgen muss damit die Position erfasst wird"
        });
        distanceFilterToast.present();
    }

    private showStationaryRadiusToast(): void {
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