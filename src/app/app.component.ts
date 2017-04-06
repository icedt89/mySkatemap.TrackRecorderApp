import { Component } from "@angular/core";
import { AlertController, Platform } from "ionic-angular";
import { StatusBar } from "@ionic-native/status-bar";
import { TrackRecorderPage } from "../pages/track-recorder/track-recorder.page";

@Component({
  templateUrl: "app.html"
})
export class MyApp {
  // tslint:disable-next-line:no-unused-variable Used inside template.
  private trackRecorderPage = TrackRecorderPage;

  public constructor(platform: Platform,
    statusBar: StatusBar,
    alertController: AlertController) {
    platform.ready().then(() => {
      if (platform.is("ios")) {
        const wrongPlatformAlert = alertController.create({
          title: "Plattform nicht unterstützt",
          message: "Die App unterstützt iOS nicht.",
          subTitle: "Schade :(",
          buttons: [{
            text: "Beenden",
            role: "cancel",
            handler: () => platform.exitApp()
          }]
        });

        wrongPlatformAlert.present();
      } else {
        statusBar.styleDefault();
      }
    });
  }
}
