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
        viewController.willEnter.subscribe(() => platform.ready().then(() => {
            const initialMapCenter = new LatLng(50.8333, 12.9167);
            const initialMapZoom = 13;

            this.googleMaps = new GoogleMaps();
            this.googleMap = this.googleMaps.create(this.mapElement.nativeElement);
            this.googleMap.on(GoogleMapsEvent.MAP_READY).subscribe(() => {
                this.googleMap.setAllGesturesEnabled(false);
                this.googleMap.setClickable(false);
                this.googleMap.setCompassEnabled(false);
                this.googleMap.setIndoorEnabled(false);
                this.googleMap.setMyLocationEnabled(false);
                this.googleMap.setTrafficEnabled(false);
                this.googleMap.setZoom(initialMapZoom);
                this.googleMap.setCenter(initialMapCenter);
                this.googleMap.moveCamera(<CameraPosition>{
                    zoom: initialMapZoom,
                    target: initialMapCenter
                });

                this.mapReadyResolve();
            });
        }));

        viewController.didLeave.subscribe(() => this.googleMap.remove());
    }

    public setTrack(positions: LatLng[]): Promise<void> {
        return this._mapReady.then(() => {
            if (!this.track) {
                this.track = <Polyline>{};
                const trackOptions = <PolylineOptions>{
                    visible: true,
                    geodesic: true,
                    color: "#FF0000",
                    points: positions
                };

                return this.googleMap.addPolyline(trackOptions)
                    .then((polyline: Polyline) => {
                        this.track = polyline;
                    });
            }

            this.track.setPoints(positions);
        });
    }

    public resetTrack(): Promise<void> {
        return this._mapReady.then(() => {
            this.googleMap.clear();

            if (this.track) {
                this.track.remove();
            }

            this.track = null;
        });
    }

    public panToTrack(): Promise<void> {
        return this._mapReady.then(() => {
            if (!this.track) {
                return;
            }

            const bounds = new LatLngBounds([]);
            this.track.getPoints().forEach((item: LatLng) => bounds.extend(item));

            return this.googleMap.animateCamera(<AnimateCameraOptions>{
                target: bounds
            });
        });
    }
}