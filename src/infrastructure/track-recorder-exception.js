var Exception = (function () {
    function Exception(message) {
        this.message = message;
    }
    Exception.prototype.toString = function () {
        return this.message;
    };
    return Exception;
}());
export { Exception };
//# sourceMappingURL=track-recorder-exception.js.map