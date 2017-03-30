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
import { Component, ElementRef, EventEmitter, Output, ViewChild } from "@angular/core";

import { Platform } from "ionic-angular";

@Component({
    selector: "map",
    templateUrl: "map.component.html"
})
export class MapComponent {
    private googleMaps: GoogleMaps;
    private googleMap: GoogleMap;
    private track: Polyline | null;

    @ViewChild("map") private mapElement: ElementRef;

    @Output() public ready = new EventEmitter<any>();

    public constructor(platform: Platform) {
        platform.ready().then(() => {
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

                this.ready.emit(null);
            });
        });
    }

    public setTrack(positions: LatLng[]): Promise<any> {
        return new Promise((resolve, reject) => {
            if (!this.track) {
                const trackOptions = <PolylineOptions>{
                    visible: true,
                    geodesic: true,
                    color: "#FF0000",
                    points: positions
                };

                this.googleMap.addPolyline(trackOptions).then((polyline: Polyline) => {
                    this.track = polyline;

                    resolve(null);
                });
            } else {
                this.track.setPoints(positions);

                resolve(null);
            }
        });
    }

    public getTrack(): LatLng[] | null {
        if (this.track) {
            return this.track.getPoints();
        }

        return null;
    }

    public resetTrack(): void {
        if (!this.track) {
            return;
        }

        this.track.remove();
        this.track = null;
    }

    public panToTrack(): void {
        if (!this.track) {
            return;
        }

        const bounds = new LatLngBounds([]);
        this.track.getPoints().forEach((item: LatLng) => bounds.extend(item));

        this.googleMap.animateCamera(<AnimateCameraOptions>{
            target: bounds
        });
    }
}