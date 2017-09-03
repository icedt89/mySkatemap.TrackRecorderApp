import { NgModule } from "@angular/core";
import { IonicModule } from "ionic-angular";
import { ObservableLoggerViewerModalComponent } from "./observable-logger-viewer-modal.component";

@NgModule({
    imports: [IonicModule],
    declarations: [ObservableLoggerViewerModalComponent],
    entryComponents: [ObservableLoggerViewerModalComponent],
    exports: [ObservableLoggerViewerModalComponent]
})
export class ObservableLoggerViewerModalModule { }