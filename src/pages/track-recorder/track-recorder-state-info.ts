import { BackgroundGeolocation } from "../../declarations";

export class TrackRecorderStateInfo {
    private trackRecorderStateLastLatitude: number | null;
    private trackRecorderStateLastLongitude: number | null;
    private trackRecorderStateRecordedPositions: BackgroundGeolocation.BackgroundGeolocationResponse[] = [];

    public get lastLatitude(): number | null {
        return this.trackRecorderStateLastLatitude;
    }

    public get lastLongitude(): number | null {
        return this.trackRecorderStateLastLongitude;
    }

    public get recordedPositions(): BackgroundGeolocation.BackgroundGeolocationResponse[] {
        return this.trackRecorderStateRecordedPositions;
    }

    public set lastLatitude(value: number | null) {
        this.trackRecorderStateLastLatitude = value;
    }

    public set lastLongitude(value: number | null) {
        this.trackRecorderStateLastLongitude = value;
    }

    public set recordedPositions(value: BackgroundGeolocation.BackgroundGeolocationResponse[]) {
        this.trackRecorderStateRecordedPositions = value;
    }
}