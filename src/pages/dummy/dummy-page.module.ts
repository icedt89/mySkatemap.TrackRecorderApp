import { DummyPageComponent } from "./dummy-page.component";
import { NgModule } from "@angular/core";

@NgModule({
    declarations: [DummyPageComponent],
    exports: [DummyPageComponent],
    entryComponents: [DummyPageComponent]
})
export class DummyPageModule { }