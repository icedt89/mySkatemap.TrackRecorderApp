import { DatabindableAsyncPipe } from "../infrastructure/databindable-async.pipe";
import { TrackRecorderPageModule } from "../pages/track-recorder/track-recorder-page.module";
import { HttpModule } from "@angular/http";
import { Globalization } from "@ionic-native/globalization";
import { BrowserModule } from "@angular/platform-browser";
import { ErrorHandler, NgModule } from "@angular/core";
import { IonicApp, IonicErrorHandler, IonicModule } from "ionic-angular";
import { IonicStorageModule } from "@ionic/storage";
import { MyApp } from "./app.component";
import { SplashScreen } from "@ionic-native/splash-screen";
import { StatusBar } from "@ionic-native/status-bar";
// import { DependencyConfiguration } from "../infrastructure/dependency-configuration";

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
    Globalization,
    SplashScreen,
    {
      provide: ErrorHandler,
      useClass: IonicErrorHandler
    }/*,
    {
      provide: "LocalizationService",
      useClass: DependencyConfiguration.useLocalizationService
    }*/
  ]
})
export class AppModule { }
