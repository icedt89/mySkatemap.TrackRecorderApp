import { Component } from "@angular/core";
import { AlertController, Platform } from "ionic-angular";
import { StatusBar } from "@ionic-native/status-bar";
import { TrackRecorderPageComponent } from "../pages/track-recorder/track-recorder-page.component";

@Component({
  templateUrl: "app.html"
})
export class MyApp {
  // tslint:disable-next-line:no-unused-variable Used inside template.
  private trackRecorderPage = TrackRecorderPageComponent;

  public constructor(platform: Platform,
    statusBar: StatusBar,
    alertController: AlertController) {
    platform.ready().then(() => statusBar.styleDefault());
  }
}
