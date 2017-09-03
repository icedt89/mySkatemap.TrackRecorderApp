import { TrackRecorderSettingsModalModel } from "./track-recorder-settings-modal-model";
import { NavParams, ToastController, ToastOptions, ViewController } from "ionic-angular";
import { Component } from "@angular/core";

@Component({
    templateUrl: "track-recorder-settings-modal.component.html"
})
export class TrackRecorderSettingsModalComponent {
    private model: TrackRecorderSettingsModalModel;

    public constructor(private viewController: ViewController, navigationParameters: NavParams, private toastController: ToastController) {
        this.model = navigationParameters.get("model");
    }

    // tslint:disable-next-line:no-unused-variable Used inside template
    private apply(): void {
        this.viewController.dismiss({
            model: this.model
        });
    }

    // tslint:disable-next-line:no-unused-variable Used inside template
    private showDistanceFilterToast(): void {
        const distanceFilterToast = this.toastController.create(<ToastOptions>{
            duration: 5000,
            position: "middle",
            showCloseButton: true,
            closeButtonText: "Danke",
            message: "Mindestbewegung in Metern die erfolgen muss, damit die Position erfasst wird"
        });
        distanceFilterToast.present();
    }

    // tslint:disable-next-line:no-unused-variable Used inside template
    private showStationaryRadiusToast(): void {
        const distanceFilterToast = this.toastController.create(<ToastOptions>{
            duration: 5000,
            position: "middle",
            showCloseButton: true,
            closeButtonText: "Danke",
            message: "Mindestbewegung in Metern die erfolgen muss, damit die Aufzeichnung fortgesetzt wird"
        });
        distanceFilterToast.present();
    }
}