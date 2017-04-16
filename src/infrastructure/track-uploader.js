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
var TrackUploader = (function () {
    function TrackUploader(http) {
        this.http = http;
        this.apiEndpoint = "http://myskatemap-api.azurewebsites.net/api/TrackRecording";
    }
    TrackUploader.prototype.uploadRecordedTrack = function (positions, trackRecording) {
        var createdRecordedTrackModel = new CreateRecordedTrackModel(trackRecording.trackName, trackRecording.trackingStartedAt, new Date());
        createdRecordedTrackModel.TrackAttachments = trackRecording.trackAttachments.map(function (trackAttachment) { return trackAttachment.imageDataUrl; });
        createdRecordedTrackModel.RecordedPositions = positions.map(function (position, order) {
            var result = new RecordedTrackPositionModel(position.latitude, position.longitude, order);
            result.Accuracy = position.accuracy;
            result.Bearing = position.bearing;
            result.CapturedAt = position.time ? new Date(position.time).toISOString() : null;
            result.Speed = position.speed;
            result.Altitude = position.altitude;
            result.ProvidedBy = position.provider;
            return result;
        });
        return this.http.post(this.apiEndpoint, createdRecordedTrackModel).toPromise().catch(function () {
            return {
                ok: false
            };
        }).then(function (resolved) { return resolved.ok; });
    };
    return TrackUploader;
}());
TrackUploader = __decorate([
    Injectable(),
    __metadata("design:paramtypes", [Http])
], TrackUploader);
export { TrackUploader };
var CreateRecordedTrackModel = (function () {
    function CreateRecordedTrackModel(trackName, trackingStartedAt, uploadStartedAt) {
        this.RecordedPositions = [];
        this.TrackAttachments = [];
        this.TrackName = trackName;
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
//# sourceMappingURL=track-uploader.js.map