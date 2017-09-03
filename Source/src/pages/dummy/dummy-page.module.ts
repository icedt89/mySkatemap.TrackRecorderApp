import { IonicModule } from "ionic-angular";
import { DummyPageComponent } from "./dummy-page.component";
import { NgModule } from "@angular/core";

@NgModule({
    imports: [IonicModule],
    declarations: [DummyPageComponent],
    exports: [DummyPageComponent],
    entryComponents: [DummyPageComponent]
})
export class DummyPageModule { }