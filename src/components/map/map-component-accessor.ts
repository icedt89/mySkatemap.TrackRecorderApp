import { MapComponent } from "./map.component";
import { IMapComponentAccessor } from "./imap-component-accessor";
import { Injectable } from "@angular/core";
import { LatLng } from "@ionic-native/google-maps";

@Injectable()
export class MapComponentAccessor implements IMapComponentAccessor {
    private mapComponent: MapComponent;

    public bindMapComponent(mapComponent: MapComponent): void {
        this.mapComponent = mapComponent;
    }

    public get mapReady(): Promise<void> {
        return this.mapComponent.mapReady;
    }

    public setTrack(positions: LatLng[]): Promise<void> {
        return this.mapComponent.setTrack(positions);
    }

    public resetTrack(): void {
        this.mapComponent.resetTrack();
    }

    public panToTrack(): Promise<void> {
        return this.mapComponent.panToTrack();
    }
}