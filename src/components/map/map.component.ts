import { Exception } from "../../infrastructure/exception";
import {
    AnimateCameraOptions,
    CameraPosition,
    GoogleMap,
    GoogleMaps,
    GoogleMapsEvent,
    LatLng,
    LatLngBounds,
    Polyline,
    PolylineOptions,
} from "@ionic-native/google-maps";
import { Component, ElementRef, ViewChild } from "@angular/core";

import { Platform, ViewController } from "ionic-angular";

@Component({
    selector: "map",
    templateUrl: "map.component.html"
})
export class MapComponent {
    private googleMaps: GoogleMaps;
    private googleMap: GoogleMap;
    private track: Polyline | null;

    @ViewChild("map") private mapElement: ElementRef;

    private mapReadyResolve: () => void;
    private _mapReady = new Promise<void>(resolve => this.mapReadyResolve = resolve);

    public constructor(platform: Platform,
        viewController: ViewController) {
        viewController.willEnter.subscribe(async () => {
            await platform.ready();

            const initialMapCenter = new LatLng(50.8333, 12.9167);
            const initialMapZoom = 13;

            this.googleMaps = new GoogleMaps();
            this.googleMap = this.googleMaps.create(this.mapElement.nativeElement);

            await this.googleMap.one(GoogleMapsEvent.MAP_READY);

            this.googleMap.setAllGesturesEnabled(false);
            this.googleMap.setClickable(false);
            this.googleMap.setCompassEnabled(false);
            this.googleMap.setIndoorEnabled(false);
            this.googleMap.setMyLocationEnabled(false);
            this.googleMap.setTrafficEnabled(false);
            this.googleMap.setCameraZoom(initialMapZoom);
            this.googleMap.setCameraTarget(initialMapCenter);
            this.googleMap.moveCamera(<CameraPosition>{
                zoom: initialMapZoom,
                target: initialMapCenter
            });

            this.mapReadyResolve();
        });

        viewController.didLeave.subscribe(() => this.googleMap.remove());
    }

    public async setTrack(positions: LatLng[]): Promise<void> {
        await this._mapReady;

        if (!this.track) {
            this.track = <Polyline>{};
            const trackOptions = <PolylineOptions>{
                visible: true,
                geodesic: true,
                color: "#FF0000",
                points: positions
            };

            const polyline = await this.googleMap.addPolyline(trackOptions);
            this.track = polyline;
        }

        this.track.setPoints(positions);
    }

    public async resetTrack(): Promise<void> {
        await this._mapReady;

        this.googleMap.clear();

        if (this.track) {
            this.track.remove();
        }

        this.track = null;
    }

    public async panToTrack(): Promise<void> {
        await this._mapReady;

        if (!this.track) {
            throw new Exception("No track set.");
        }

        const bounds = new LatLngBounds([]);
        this.track.getPoints().forEach((item: LatLng) => bounds.extend(item));

        this.googleMap.animateCamera(<AnimateCameraOptions>{
            target: bounds
        });
    }
}