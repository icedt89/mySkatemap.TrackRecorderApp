import { IonicModule } from "ionic-angular";
import { TrackRecorderPopoverComponent } from "./track-recorder-popover.component";
import { NgModule } from "@angular/core";

@NgModule({
    imports: [IonicModule],
    declarations: [TrackRecorderPopoverComponent],
    entryComponents: [TrackRecorderPopoverComponent],
    exports: [TrackRecorderPopoverComponent]
})
export class TrackRecorderPopoverModule { }