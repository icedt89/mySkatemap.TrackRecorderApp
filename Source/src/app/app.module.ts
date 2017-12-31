import { ILogger } from "./../infrastructure/logging/ilogger";
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
import { IonicApp, IonicErrorHandler, IonicModule, Platform } from "ionic-angular";
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

const dependencyInjectionSettings = {
  useObservableIonicErrorHandler: true,
  useMockedLocalizationService: true,
  useObservableLogger: true
};

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
    SplashScreen,
    IdentityService,
    AuthenticationHandler,
    UserProfileService,
    AuthenticationStore,
    StorageAccessor,
    {
      provide: ErrorHandler,
      deps: ["Logger"],
      useFactory: (logger: ILogger) => {
        if (dependencyInjectionSettings.useObservableIonicErrorHandler) {
            return new ObservableIonicErrorHandler(logger);
        }

        return new IonicErrorHandler();
      }
    },
    {
      provide: "LocalizationService",
      deps: [Platform, "Logger"],
      useFactory: (platform: Platform, logger: ILogger) => {
        if (dependencyInjectionSettings.useMockedLocalizationService) {
          return new MockedLocalizationService();
        }

        return new LocalizationService(platform, {
          getPreferredLanguage: () => {
            return {
              value: "de"
            }
          }
        }, logger);
      }
    },
    {
      provide: "Logger",
      useFactory: () => {
        if (dependencyInjectionSettings.useObservableLogger) {
          return new ObservableLogger();
        }

        return new DefaultLogger();
      }
    }
  ]
})
export class AppModule { }
