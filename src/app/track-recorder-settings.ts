export class TrackRecorderSettings {
    private recorderDesiredAccuracy: string;
    private recorderStationaryRadius: number;
    private recorderDistanceFilter: number;
    private recorderLocationProvider: string;
    private recorderInterval: number;
    private recorderFastestInterval: number;
    private recorderActivitiesInterval: number;

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

    public get interval(): number {
        return this.recorderInterval;
    }

    public set interval(interval: number) {
        this.recorderInterval = interval;
    }

    public get fastestInterval(): number {
        return this.recorderFastestInterval;
    }

    public set fastestInterval(fastestInterval: number) {
        this.recorderFastestInterval = fastestInterval;
    }

    public get activitiesInterval(): number {
        return this.recorderActivitiesInterval;
    }

    public set activitiesInterval(activitiesInterval: number) {
        this.recorderActivitiesInterval = activitiesInterval;
    }
}