import { Exception } from "./track-recorder-exception";
import { LengthUnit } from "./length-unit";
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
export { LengthUnitHelper };
//# sourceMappingURL=lenght-unit-helper.js.map