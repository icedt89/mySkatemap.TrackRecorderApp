import { LengthUnit } from "./length-unit";
import { UsefulUnitConversion } from "./useful-unit-conversion";
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
export { Length };
//# sourceMappingURL=length.js.map