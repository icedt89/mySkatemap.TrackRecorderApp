var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
import { AlertController } from "ionic-angular/components/alert/alert";
import { Component } from "@angular/core";
import { Platform } from "ionic-angular";
import { SplashScreen } from "@ionic-native/splash-screen";
import { StatusBar } from "@ionic-native/status-bar";
import { TrackRecorderPage } from "../pages/track-recorder/track-recorder.page";
var MyApp = (function () {
    function MyApp(platform, statusBar, splashScreen, alertController) {
        // tslint:disable-next-line:no-unused-variable Used inside template.
        this.trackRecorderPage = TrackRecorderPage;
        platform.ready().then(function () {
            if (platform.is("ios")) {
                var wrongPlatformAlert = alertController.create({
                    title: "Plattform nicht unterstützt",
                    message: "Die App unterstützt iOS nicht.",
                    subTitle: "Schade :(",
                    buttons: [{
                            text: "Beenden",
                            role: "cancel",
                            handler: function () { return platform.exitApp(); }
                        }]
                });
                wrongPlatformAlert.present();
            }
            else {
                statusBar.styleDefault();
                splashScreen.hide();
            }
        });
    }
    return MyApp;
}());
MyApp = __decorate([
    Component({
        templateUrl: "app.html"
    }),
    __metadata("design:paramtypes", [Platform,
        StatusBar,
        SplashScreen,
        AlertController])
], MyApp);
export { MyApp };
//# sourceMappingURL=app.component.js.map