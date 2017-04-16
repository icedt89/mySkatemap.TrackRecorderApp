import { Exception } from "../track-recorder-exception";
var CapturedMediaResult = (function () {
    function CapturedMediaResult(result, isBase64DataUrl) {
        if (isBase64DataUrl === void 0) { isBase64DataUrl = true; }
        this.result = result;
        this.isBase64DataUrl = isBase64DataUrl;
    }
    Object.defineProperty(CapturedMediaResult.prototype, "dataUrl", {
        get: function () {
            if (this.isBase64DataUrl) {
                return "data:image/jpeg;base64," + this.result;
            }
            throw new Exception("isBase64DataUrl needs to be true.");
        },
        enumerable: true,
        configurable: true
    });
    return CapturedMediaResult;
}());
export { CapturedMediaResult };
//# sourceMappingURL=captured-media-result.js.map