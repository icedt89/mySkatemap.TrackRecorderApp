import { MockedLocalizationService } from "../infrastructure/localization/mocked-localization-service";
import { LocalizationService } from "../infrastructure/localization/localization-service";
import { DatabindableAsyncPipe } from "../infrastructure/databindable-async.pipe";
import { TrackRecorderPageModule } from "../pages/track-recorder/track-recorder-page.module";
import { HttpModule } from "@angular/http";
import { BrowserModule } from "@angular/platform-browser";
import { ErrorHandler, NgModule } from "@angular/core";
import { IonicApp, IonicErrorHandler, IonicModule } from "ionic-angular";
import { IonicStorageModule } from "@ionic/storage";
import { MyApp } from "./app.component";
import { SplashScreen } from "@ionic-native/splash-screen";
import { StatusBar } from "@ionic-native/status-bar";

@NgModule({
  imports: [
    BrowserModule,
    HttpModule,
    TrackRecorderPageModule,
    IonicModule.forRoot(MyApp),
    IonicStorageModule.forRoot(),
  ],
  declarations: [
    MyApp,
    DatabindableAsyncPipe
  ],
  bootstrap: [IonicApp],
  entryComponents: [
    MyApp
  ],
  providers: [
    StatusBar,
    // Globalization,
    SplashScreen,
    {
      provide: ErrorHandler,
      useClass: IonicErrorHandler
    },
    {
      provide: "LocalizationService",
      useClass: MockedLocalizationService
      // useClass: LocalizationService
    }
  ]
})
export class AppModule { }
