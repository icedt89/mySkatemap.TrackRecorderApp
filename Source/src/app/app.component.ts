import { TrackRecorderPageComponent } from "../pages/track-recorder/track-recorder-page.component";
import { SplashScreen } from "@ionic-native/splash-screen";
import { Component } from "@angular/core";
import { Platform } from "ionic-angular";
import { StatusBar } from "@ionic-native/status-bar";

@Component({
  templateUrl: "app.html"
})
export class MyApp {
  // tslint:disable-next-line:no-unused-variable Used inside template.
  private root = TrackRecorderPageComponent;

  public constructor(private platform: Platform, private statusBar: StatusBar, private splashScreen: SplashScreen) {
    this.initialize();
  }

  private async initialize(): Promise<void> {
    await this.platform.ready();

    this.statusBar.styleDefault();
    this.splashScreen.hide();
  }
}
