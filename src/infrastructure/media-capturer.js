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
import { File } from "@ionic-native/file";
import { MediaCapture } from "@ionic-native/media-capture";
var MediaCapturer = (function () {
    function MediaCapturer(mediaCapture, file) {
        this.mediaCapture = mediaCapture;
        this.file = file;
    }
    MediaCapturer.prototype.captureImage = function (limit) {
        var _this = this;
        if (limit === void 0) { limit = 1; }
        var mediaCaptureUserCancelled = 3;
        return this.mediaCapture.captureImage({
            limit: limit
        }).then(function (mediaFiles) {
            var promises = mediaFiles.map(function (mediaFile) {
                var mediaFilePath = mediaFile.fullPath.replace(mediaFile.name, "");
                return _this.file.readAsDataURL(mediaFilePath, mediaFile.name).then(function (dataUrl) { return _this.file.removeFile(mediaFilePath, mediaFile.name).then(function () { return dataUrl; }); });
            });
            return Promise.all(promises);
        }, function (error) {
            if (+error.code === mediaCaptureUserCancelled) {
                return [];
            }
        });
    };
    return MediaCapturer;
}());
MediaCapturer = __decorate([
    Injectable(),
    __metadata("design:paramtypes", [MediaCapture,
        File])
], MediaCapturer);
export { MediaCapturer };
//# sourceMappingURL=media-capturer.js.map