var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
import "rxjs/Rx";
import { Http } from "@angular/http";
import { Injectable } from "@angular/core";
var RecordedTrackUploader = (function () {
    function RecordedTrackUploader(http) {
        this.http = http;
        this.apiEndpoint = "http://myskatemap-api.azurewebsites.net/api/RecordedTrack";
    }
    RecordedTrackUploader.prototype.uploadRecordedTrack = function (positions, startedAt) {
        var createdRecordedTrackModel = new CreateRecordedTrackModel(startedAt, new Date());
        createdRecordedTrackModel.RecordedPositions = positions.map(function (position, order) {
            var result = new RecordedTrackPositionModel(position.latitude, position.longitude, order);
            result.Accuracy = position.accuracy;
            result.Bearing = position.bearing;
            result.CapturedAt = position.time;
            result.Speed = position.time;
            return result;
        });
        return this.http.post(this.apiEndpoint, createdRecordedTrackModel).toPromise().catch(function () {
            return {
                ok: false
            };
        }).then(function (resolved) { return resolved.ok; });
    };
    return RecordedTrackUploader;
}());
RecordedTrackUploader = __decorate([
    Injectable(),
    __metadata("design:paramtypes", [Http])
], RecordedTrackUploader);
export { RecordedTrackUploader };
var CreateRecordedTrackModel = (function () {
    function CreateRecordedTrackModel(trackingStartedAt, uploadStartedAt) {
        this.RecordedPositions = [];
        this.TrackingStartedAt = trackingStartedAt.toISOString();
        this.UploadStartedAt = uploadStartedAt.toISOString();
    }
    return CreateRecordedTrackModel;
}());
var RecordedTrackPositionModel = (function () {
    function RecordedTrackPositionModel(Latitude, Longitude, Order) {
        this.Latitude = Latitude;
        this.Longitude = Longitude;
        this.Order = Order;
    }
    return RecordedTrackPositionModel;
}());
//# sourceMappingURL=recorded-track-uploader.js.map