import { TrackAttachmentsModalModel } from "../../../components/track-attachments-modal/track-attachments-modal-model";
import {
    TrackAttachmentsModalComponent
} from "../../../components/track-attachments-modal/track-attachments-modal.component";
import { ShowSavedTrackRecordingModalModel } from "./show-saved-track-recording-modal-model";
import { ModalController, NavParams, ViewController } from "ionic-angular";
import { Component } from "@angular/core";
import { Exception } from "../../../infrastructure/exception";

@Component({
    templateUrl: "show-saved-track-recording-modal.component.html"
})
export class ShowSavedTrackRecordingModalComponent {
    private model: ShowSavedTrackRecordingModalModel;

    public constructor(viewController: ViewController, navigationParameters: NavParams, private modalController: ModalController) {
        this.model = navigationParameters.get("model");
    }

    // tslint:disable-next-line:no-unused-variable Used inside template.
    private showTrackRecordingAttachments(): void {
        if (!this.model.attachments || !this.model.attachments.length) {
            throw new Exception("No attachments to display.")
        }

        const showCurrentTrackRecordingAttachmentsModal = this.modalController.create(TrackAttachmentsModalComponent, {
            model: new TrackAttachmentsModalModel(this.model.attachments.map(_ => _), true)
        });

        showCurrentTrackRecordingAttachmentsModal.present();
    }
}
