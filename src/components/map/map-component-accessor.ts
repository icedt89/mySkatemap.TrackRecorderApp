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

    public async setTrack(positions: LatLng[]): Promise<void> {
        await this.mapComponent.setTrack(positions);
    }

    public resetTrack(): void {
        this.mapComponent.resetTrack();
    }

    public async panToTrack(): Promise<void> {
        await this.mapComponent.panToTrack();
    }
}