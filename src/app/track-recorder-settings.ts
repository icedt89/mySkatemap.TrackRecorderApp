export class TrackRecorderSettings {
    private recorderDesiredAccuracy: string;
    private recorderLocationProvider: string;
    private recorderStationaryRadius: number;
    private recorderDistanceFilter: number;

    public get desiredAccuracy(): string {
        return this.recorderDesiredAccuracy;
    }

    public set desiredAccuracy(desiredAccuracy: string) {
        this.recorderDesiredAccuracy = desiredAccuracy;
    }

    public get stationaryRadius(): number {
        return this.recorderStationaryRadius;
    }

    public set stationaryRadius(stationaryRedius: number) {
        this.recorderStationaryRadius = stationaryRedius;
    }

    public get distanceFilter(): number {
        return this.recorderDistanceFilter;
    }

    public set distanceFilter(distanceFilter: number) {
        this.recorderDistanceFilter = distanceFilter;
    }

    public get locationProvider(): string {
        return this.recorderLocationProvider;
    }

    public set locationProvider(locationProvider: string) {
        this.recorderLocationProvider = locationProvider;
    }
}