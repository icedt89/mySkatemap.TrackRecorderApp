import { Haversine } from "./haversine";
import { Length } from "./length";
import { LatLng } from "@ionic-native/google-maps";
import { Exception } from "./exception";
import { LengthUnit } from "./length-unit";

export class LengthUnitHelper {
    private static getUnitTextFromUnit(unit: LengthUnit): string {
        switch (unit) {
            case LengthUnit.Meters:
                return "m";
            case LengthUnit.Kilometers:
                return "km";
            default:
                throw new Exception(`Invalid length unit '${unit}'.`);
        }
    }

    private static getLengthUnitFromText(unit: string): LengthUnit {
        switch (unit) {
            case "km":
                return LengthUnit.Kilometers;
            case "m":
                return LengthUnit.Meters;
            default:
                throw new Exception(`Invalid length unit '${unit}'.`);
        }
    }

    public static formatTrackLength(trackedPath: LatLng[]): string {
        const computedTrackLength = Haversine.computeDistance(trackedPath);

        const usefulTrackLength = Length.convertToMoreUsefulUnit(computedTrackLength);
        const lengthUnitText = LengthUnitHelper.getUnitTextFromUnit(usefulTrackLength.lengthUnit);
        switch (usefulTrackLength.lengthUnit) {
            case LengthUnit.Kilometers:
                return `${(+usefulTrackLength.usefulUnit.toFixed(3)).toLocaleString("de")} ${lengthUnitText}`;
            case LengthUnit.Meters:
                return `${(+usefulTrackLength.usefulUnit.toFixed(1)).toLocaleString("de")} ${lengthUnitText}`;
            default:
                throw new Exception(`Invalid length unit '${LengthUnit}'.`);
        }
    }
}