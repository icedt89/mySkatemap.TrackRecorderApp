var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
import { GoogleMaps, GoogleMapsEvent, LatLng, LatLngBounds, } from "@ionic-native/google-maps";
import { Component, ElementRef, EventEmitter, Output, ViewChild } from "@angular/core";
import { Platform } from "ionic-angular";
var MapComponent = (function () {
    function MapComponent(platform) {
        var _this = this;
        this.ready = new EventEmitter();
        platform.ready().then(function () {
            var initialMapCenter = new LatLng(50.8333, 12.9167);
            var initialMapZoom = 13;
            _this.googleMaps = new GoogleMaps();
            _this.googleMap = _this.googleMaps.create(_this.mapElement.nativeElement);
            _this.googleMap.on(GoogleMapsEvent.MAP_READY).subscribe(function () {
                _this.googleMap.setAllGesturesEnabled(false);
                _this.googleMap.setClickable(false);
                _this.googleMap.setCompassEnabled(false);
                _this.googleMap.setIndoorEnabled(false);
                _this.googleMap.setMyLocationEnabled(false);
                _this.googleMap.setTrafficEnabled(false);
                _this.googleMap.setZoom(initialMapZoom);
                _this.googleMap.setCenter(initialMapCenter);
                _this.googleMap.moveCamera({
                    zoom: initialMapZoom,
                    target: initialMapCenter
                });
                _this.ready.emit(null);
            });
        });
    }
    MapComponent.prototype.setTrack = function (positions) {
        var _this = this;
        return new Promise(function (resolve, reject) {
            if (!_this.track) {
                var trackOptions = {
                    visible: true,
                    geodesic: true,
                    color: "#FF0000",
                    points: positions
                };
                _this.googleMap.addPolyline(trackOptions).then(function (polyline) {
                    _this.track = polyline;
                    resolve(null);
                });
            }
            else {
                _this.track.setPoints(positions);
                resolve(null);
            }
        });
    };
    MapComponent.prototype.getTrack = function () {
        if (this.track) {
            return this.track.getPoints();
        }
        return null;
    };
    MapComponent.prototype.resetTrack = function () {
        if (!this.track) {
            return;
        }
        this.track.remove();
        this.track = null;
    };
    MapComponent.prototype.panToTrack = function () {
        if (!this.track) {
            return;
        }
        var bounds = new LatLngBounds([]);
        this.track.getPoints().forEach(function (item) { return bounds.extend(item); });
        this.googleMap.animateCamera({
            target: bounds
        });
    };
    return MapComponent;
}());
__decorate([
    ViewChild("map"),
    __metadata("design:type", ElementRef)
], MapComponent.prototype, "mapElement", void 0);
__decorate([
    Output(),
    __metadata("design:type", Object)
], MapComponent.prototype, "ready", void 0);
MapComponent = __decorate([
    Component({
        selector: "map",
        templateUrl: "map.component.html"
    }),
    __metadata("design:paramtypes", [Platform])
], MapComponent);
export { MapComponent };
//# sourceMappingURL=map.component.js.map