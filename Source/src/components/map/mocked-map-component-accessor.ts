import { Inject } from "@angular/core";
import { ILogger } from "../../infrastructure/logging/ilogger";
import { MapComponent } from "./map.component";
import { IMapComponentAccessor } from "./imap-component-accessor";
import { Injectable } from "@angular/core";
import { LatLng } from "@ionic-native/google-maps";

@Injectable()
export class MockedMapComponentAccessor implements IMapComponentAccessor {
    public constructor(@Inject("Logger") private logger: ILogger) {
        this.logger.warn("Using MockedMapComponentAccessor for IMapComponentAccessor");
    }

    public bindMapComponent(mapComponent: MapComponent): void {
        this.logger.log("MockedMapComponentAccessor: Map bound");
    }

    public async setTrack(positions: LatLng[]): Promise<void> {
        this.logger.log(`MockedMapComponentAccessor: Track with ${positions.length} positions set`);
    }

    public resetTrack(): void {
        this.logger.log("MockedMapComponentAccessor: Track reset");
    }

    public async panToTrack(): Promise<void> {
        this.logger.log("MockedMapComponentAccessor: Track focused");
    }
}