import { MapComponent } from "./map.component";
import { IMapComponentAccessor } from "./imap-component-accessor";
import { Injectable } from "@angular/core";
import { LatLng } from "@ionic-native/google-maps";

@Injectable()
export class MockedMapComponentAccessor implements IMapComponentAccessor {
    public bindMapComponent(mapComponent: MapComponent): void {
        console.log("MockedMapComponentAccessor: Map bound");
    }

    public get mapReady(): Promise<void> {
        console.log("MockedMapComponentAccessor: Map ready");

        return Promise.resolve();
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