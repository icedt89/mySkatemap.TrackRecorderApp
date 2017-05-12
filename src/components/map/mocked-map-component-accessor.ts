import { MapComponent } from "./map.component";
import { IMapComponentAccessor } from "./imap-component-accessor";
import { Injectable } from "@angular/core";
import { LatLng } from "@ionic-native/google-maps";

@Injectable()
export class MockedMapComponentAccessor implements IMapComponentAccessor {
    public constructor() {
        console.warn("Using MockedMapComponentAccessor for IMapComponentAccessor");
    }

    public bindMapComponent(mapComponent: MapComponent): void {
        console.log("MockedMapComponentAccessor: Map bound");
    }

    public setTrack(positions: LatLng[]): Promise<void> {
        console.log(`MockedMapComponentAccessor: Track with ${positions.length} positions set`);

        return Promise.resolve();
    }

    public resetTrack(): void {
        console.log("MockedMapComponentAccessor: Track reset");
    }

    public panToTrack(): Promise<void> {
        console.log("MockedMapComponentAccessor: Track focused");

        return Promise.resolve();
    }
}