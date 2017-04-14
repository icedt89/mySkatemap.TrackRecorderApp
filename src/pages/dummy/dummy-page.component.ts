import { ViewController } from "ionic-angular";
import { Component } from "@angular/core";

@Component({
    selector: "dummy",
    templateUrl: "dummy.component.html"
})
export class DummyPageComponent {
    public constructor(viewController: ViewController) {
        viewController.willEnter.subscribe(() => {
            debugger;
        });
        viewController.didLeave.subscribe(() => {
            debugger;
        });
        viewController.willUnload.subscribe(() => {
            debugger;
        });
    }
}