var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
import { Injectable } from "@angular/core";
import { Camera } from "@ionic-native/camera";
import { CapturedMediaResult } from "./captured-media-result";
var MediaCapturer = (function () {
    function MediaCapturer(camera) {
        this.camera = camera;
    }
    MediaCapturer.prototype.captureCameraImage = function () {
        return this.camera.getPicture({
            destinationType: 0,
            correctOrientation: true
        }).then(function (result) { return new CapturedMediaResult(result); });
    };
    MediaCapturer.prototype.selectLibraryImage = function () {
        return this.camera.getPicture({
            destinationType: 0,
            sourceType: 0,
            correctOrientation: true
        }).then(function (result) { return new CapturedMediaResult(result); });
    };
    return MediaCapturer;
}());
MediaCapturer = __decorate([
    Injectable(),
    __metadata("design:paramtypes", [Camera])
], MediaCapturer);
export { MediaCapturer };
//# sourceMappingURL=media-capturer.js.map