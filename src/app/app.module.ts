import {
  ObservableLoggerViewerModalModule
} from "../components/observable-logger-viewer-modal/observable-logger-viewer-modal.module";
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
import { IdentityService } from "../infrastructure/identity/identity-service";
import { AuthenticationHandler } from "../infrastructure/authentication-handler";
import { StorageAccessor } from "../infrastructure/storage-accessor";
import { UserProfileService } from "../infrastructure/user-profile/user-profile-service";
import { AuthenticationStore } from "../infrastructure/authentication-store";
import { ObservableLogger } from "../infrastructure/logging/observable-logger";
import { DefaultLogger } from "../infrastructure/logging/default-logger";
import { ObservableIonicErrorHandler } from "./observable-ionic-error-handler";

@NgModule({
  imports: [
    BrowserModule,
    HttpModule,
    TrackRecorderPageModule,
    ObservableLoggerViewerModalModule,
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
    IdentityService,
    AuthenticationHandler,
    UserProfileService,
    AuthenticationStore,
    StorageAccessor,
    {
      provide: ErrorHandler,
      // useClass: IonicErrorHandler
      useClass: ObservableIonicErrorHandler
    },
    {
      provide: "LocalizationService",
      useClass: MockedLocalizationService
      // useClass: LocalizationService
    },
    {
      provide: "Logger",
      useClass: ObservableLogger
      // useClass: DefaultLogger
    }
  ]
})
export class AppModule { }
