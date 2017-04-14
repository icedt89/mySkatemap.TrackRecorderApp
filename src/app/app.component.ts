import { SplashScreen } from "@ionic-native/splash-screen";
import { DummyPageComponent } from "../pages/dummy/dummy-page.component";
import { Component } from "@angular/core";
import { Platform } from "ionic-angular";
import { StatusBar } from "@ionic-native/status-bar";
import { TrackRecorderPageComponent } from "../pages/track-recorder/track-recorder-page.component";

@Component({
  templateUrl: "app.html"
})
export class MyApp {
  // tslint:disable-next-line:no-unused-variable Used inside template.
  private root = DummyPageComponent;

  public constructor(platform: Platform,
    statusBar: StatusBar,
    splashScreen: SplashScreen) {
    platform.ready().then(() => statusBar.styleDefault()).then(() => splashScreen.hide());
  }
}
