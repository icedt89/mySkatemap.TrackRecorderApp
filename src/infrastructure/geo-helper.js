import { Exception } from "./track-recorder-exception";
export var GeoHelper;
(function (GeoHelper) {
    var LengthUnit;
    (function (LengthUnit) {
        LengthUnit[LengthUnit["Meters"] = 0] = "Meters";
        LengthUnit[LengthUnit["Kilometers"] = 1] = "Kilometers";
    })(LengthUnit = GeoHelper.LengthUnit || (GeoHelper.LengthUnit = {}));
    var LengthUnitHelper = (function () {
        function LengthUnitHelper() {
        }
        LengthUnitHelper.getUnitTextFromUnit = function (unit) {
            switch (unit) {
                case LengthUnit.Meters:
                    return "m";
                case LengthUnit.Kilometers:
                    return "km";
                default:
                    throw new Exception("Invalid length unit " + unit + ".");
            }
        };
        LengthUnitHelper.getLengthUnitFromText = function (unit) {
            switch (unit) {
                case "km":
                    return LengthUnit.Kilometers;
                case "m":
                    return LengthUnit.Meters;
                default:
                    throw new Exception("Invalid length unit " + unit + ".");
            }
        };
        return LengthUnitHelper;
    }());
    GeoHelper.LengthUnitHelper = LengthUnitHelper;
    var UsefulUnitConversion = (function () {
        function UsefulUnitConversion(meters, usefulUnit, lengthUnit) {
            this.meters = meters;
            this.usefulUnit = usefulUnit;
            this.lengthUnit = lengthUnit;
        }
        return UsefulUnitConversion;
    }());
    GeoHelper.UsefulUnitConversion = UsefulUnitConversion;
    var Length = (function () {
        function Length() {
        }
        Length.convertToMoreUsefulUnit = function (meters) {
            if (meters > 1000) {
                return new UsefulUnitConversion(meters, +(meters / 1000).toFixed(3), LengthUnit.Kilometers);
            }
            return new UsefulUnitConversion(meters, +meters.toFixed(1), LengthUnit.Meters);
        };
        return Length;
    }());
    GeoHelper.Length = Length;
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
    GeoHelper.Haversine = Haversine;
})(GeoHelper || (GeoHelper = {}));
//# sourceMappingURL=geo-helper.js.map