import { ViewChild } from "@angular/core";
import { MapComponent } from "../../../components/map/map.component";
import { ShowSavedTrackRecordingModalModel } from "./show-saved-track-recording-modal-model";
import { NavParams, ViewController } from "ionic-angular";
import { Component } from "@angular/core";

@Component({
    templateUrl: "show-saved-track-recording-modal.component.html"
})
export class ShowSavedTrackRecordingModalComponent {
    private model: ShowSavedTrackRecordingModalModel;

    @ViewChild("map") private map: MapComponent;

    public constructor(viewController: ViewController, navigationParameters: NavParams) {
        this.model = navigationParameters.get("model");

        viewController.willEnter.subscribe(async () => {
            await this.map.setTrack(this.model.trackedPositions);
            await this.map.panToTrack();
        });
    }
}
