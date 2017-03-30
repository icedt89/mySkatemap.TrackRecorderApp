import { ErrorHandler, NgModule } from "@angular/core";
import { IonicApp, IonicErrorHandler, IonicModule } from "ionic-angular";
import { IonicStorageModule } from "@ionic/storage";

import { MapComponent } from "../components/map/map.component";
import { MyApp } from "./app.component";
import { RecordedTrackUploader } from "../pages/track-recorder/recorded-track-uploader";
import { SplashScreen } from "@ionic-native/splash-screen";
import { StatusBar } from "@ionic-native/status-bar";
import { TrackRecorder } from "../pages/track-recorder/track-recorder";
import { TrackRecorderPage } from "../pages/track-recorder/track-recorder.page";
import {
    TrackRecorderSettingsComponent,
} from "../components/track-recorder-settings/track-recorder-settings.component";

@NgModule({
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
export class AppModule { }
