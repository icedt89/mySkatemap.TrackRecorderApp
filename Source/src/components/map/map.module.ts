import { MapComponent } from "./map.component";
import { NgModule } from "@angular/core";

@NgModule({
    declarations: [MapComponent],
    entryComponents: [MapComponent],
    exports: [MapComponent]
})
export class MapModule { }