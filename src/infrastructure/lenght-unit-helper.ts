import { Exception } from "./exception";
import { LengthUnit } from "./length-unit";

export class LengthUnitHelper {
    public static getUnitTextFromUnit(unit: LengthUnit): string {
        switch (unit) {
            case LengthUnit.Meters:
                return "m";
            case LengthUnit.Kilometers:
                return "km";
            default:
                throw new Exception(`Invalid length unit '${unit}'.`);
        }
    }

    public static getLengthUnitFromText(unit: string): LengthUnit {
        switch (unit) {
            case "km":
                return LengthUnit.Kilometers;
            case "m":
                return LengthUnit.Meters;
            default:
                throw new Exception(`Invalid length unit '${unit}'.`);
        }
    }
}