import { Http } from "@angular/http";
import { ILogger } from "./../../infrastructure/logging/ilogger";
import {
    ShowSavedTrackRecordingModalModule
} from "./show-saved-track-recording-modal/show-saved-track-recording-modal.module";
import { MapComponentAccessor } from "../../components/map/map-component-accessor";
import { MockedMapComponentAccessor } from "../../components/map/mocked-map-component-accessor";
import { TrackRecorder } from "../../infrastructure/track-recorder/track-recorder";
import { MockedTrackUploader } from "../../infrastructure/track-uploader/mocked-track-uploader";
import { TrackUploader } from "../../infrastructure/track-uploader/track-uploader";
import { TrackRecorderPopoverModule } from "./track-recorder-popover/track-recorder-popover.module";
import { TrackRecordingStore } from "../../infrastructure/track-store/track-recording-store";
import { ArchivedTrackRecordingStore } from "../../infrastructure/track-store/archived-track-recording-store";
import { TrackAttachmentsModalModule } from "../../components/track-attachments-modal/track-attachments-modal.module";
import { MapModule } from "../../components/map/map.module";
import { IonicModule, Events } from "ionic-angular";
import { TrackRecorderPageComponent } from "./track-recorder-page.component";
import { NgModule } from "@angular/core";
import { TrackRecorderSettingsModalModule } from "./track-recorder-settings-modal/track-recorder-settings-modal.module";
import { MockedTrackRecorder } from "../../infrastructure/track-recorder/mocked-track-recorder";
import { LoginModalModule } from "../../components/login-modal/login-modal.module";
import { Platform } from "ionic-angular";
import { AuthenticationStore } from "../../infrastructure/authentication-store";
import * as  BGL from "@ionic-native/background-geolocation";

const dependencyInjectionSettings = {
    useMockedMapComponentAccessor: false,
    useMockedTrackRecorder: false,
    useMockedTrackUploader: true
};

@NgModule({
    imports: [
        TrackRecorderSettingsModalModule,
        TrackAttachmentsModalModule,
        TrackRecorderPopoverModule,
        ShowSavedTrackRecordingModalModule,
        MapModule,
        IonicModule,
        LoginModalModule
    ],
    declarations: [
        TrackRecorderPageComponent,
    ],
    entryComponents: [
        TrackRecorderPageComponent
    ],
    providers: [
        ArchivedTrackRecordingStore,
        TrackRecordingStore,
        {
            provide: "TrackRecorder",
            deps: ["Logger", Platform],
            useFactory: (logger: ILogger, platform: Platform) => {
                if (dependencyInjectionSettings.useMockedTrackRecorder) {
                    return new MockedTrackRecorder(logger);
                }

                debugger;

                return new TrackRecorder(platform, new BGL.BackgroundGeolocation());
            }
        },
        {
            provide: "TrackUploader",
            deps: ["Logger", Http, Events, AuthenticationStore],
            useFactory: (logger: ILogger, http: Http, events: Events, authenticationStore: AuthenticationStore) => {
                if (dependencyInjectionSettings.useMockedTrackUploader) {
                    return new MockedTrackUploader(logger);
                }

                return new TrackUploader(http, authenticationStore, events);
            }
        },
        {
            provide: "MapComponentAccessor",
            deps: ["Logger"],
            useFactory: (logger: ILogger) => {
                if (dependencyInjectionSettings.useMockedMapComponentAccessor) {
                    return new MockedMapComponentAccessor(logger);
                }

                return new MapComponentAccessor();
            }
        }]
})
export class TrackRecorderPageModule { }