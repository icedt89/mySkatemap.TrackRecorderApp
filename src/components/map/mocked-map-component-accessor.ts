import { MapComponent } from "./map.component";
import { IMapComponentAccessor } from "./imap-component-accessor";
import { Injectable } from "@angular/core";
import { LatLng } from "@ionic-native/google-maps";

@Injectable()
export class MockedMapComponentAccessor implements IMapComponentAccessor {
    public bindMapComponent(mapComponent: MapComponent): void {
    }

    public get mapReady(): Promise<void> {
        return Promise.resolve();
    }

    public setTrack(positions: LatLng[]): Promise<void> {
        return Promise.resolve();
    }

    public resetTrack(): void {
    }

    public panToTrack(): Promise<void> {
        return Promise.resolve();
    }
}