import { MapComponent } from "./map.component";
import { LatLng } from "@ionic-native/google-maps";

export interface IMapComponentAccessor {
    bindMapComponent(mapComponent: MapComponent): void;

    setTrack(positions: LatLng[]): Promise<void>;

    resetTrack(): void;

    panToTrack(): Promise<void>;
}