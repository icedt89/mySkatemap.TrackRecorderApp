var Haversine = (function () {
    function Haversine() {
    }
    Haversine.toRad = function (val) {
        return (val * Math.PI) / 180;
    };
    Haversine.computeDistanceBetween = function (pointOne, pointTwo) {
        var dLat = Haversine.toRad(pointTwo.lat - pointOne.lat);
        var dLon = Haversine.toRad(pointTwo.lng - pointOne.lng);
        var lat1 = Haversine.toRad(pointOne.lat);
        var lat2 = Haversine.toRad(pointTwo.lat);
        var a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371000 * c;
    };
    Haversine.computeDistance = function (polyLinePoints) {
        var result = 0;
        for (var i = 0; i < polyLinePoints.length - 1; i++) {
            var pointOne = polyLinePoints[i];
            var pointTwo = polyLinePoints[i + 1];
            result += Haversine.computeDistanceBetween(pointOne, pointTwo);
        }
        return result;
    };
    return Haversine;
}());
export { Haversine };
//# sourceMappingURL=haversine.js.map