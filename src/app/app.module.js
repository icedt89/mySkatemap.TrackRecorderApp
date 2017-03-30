var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
import { ErrorHandler, NgModule } from "@angular/core";
import { IonicApp, IonicErrorHandler, IonicModule } from "ionic-angular";
import { IonicStorageModule } from '@ionic/storage';
import { MapComponent } from "../components/map/map.component";
import { MyApp } from "./app.component";
import { RecordedTrackUploader } from "../pages/track-recorder/recorded-track-uploader";
import { SplashScreen } from "@ionic-native/splash-screen";
import { StatusBar } from "@ionic-native/status-bar";
import { TrackRecorder } from "../pages/track-recorder/track-recorder";
import { TrackRecorderPage } from "../pages/track-recorder/track-recorder.page";
import { TrackRecorderSettingsComponent, } from "../components/track-recorder-settings/track-recorder-settings.component";
var AppModule = (function () {
    function AppModule() {
    }
    return AppModule;
}());
AppModule = __decorate([
    NgModule({
        imports: [
            IonicModule.forRoot(MyApp),
            IonicStorageModule.forRoot()
        ],
        declarations: [
            MyApp,
            TrackRecorderPage,
            MapComponent,
            TrackRecorderSettingsComponent
        ],
        bootstrap: [IonicApp],
        entryComponents: [
            MyApp,
            TrackRecorderPage,
            TrackRecorderSettingsComponent
        ],
        providers: [
            StatusBar,
            SplashScreen,
            TrackRecorder,
            RecordedTrackUploader,
            { provide: ErrorHandler, useClass: IonicErrorHandler }
        ]
    })
], AppModule);
export { AppModule };
//# sourceMappingURL=app.module.js.map