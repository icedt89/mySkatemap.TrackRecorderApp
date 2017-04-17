import { LengthUnit } from "./length-unit";
import { UsefulUnitConversion } from "./useful-unit-conversion";

export class Length {
    public static convertToMoreUsefulUnit(meters: number): UsefulUnitConversion {
        if (meters > 1000) {
            return new UsefulUnitConversion(meters, +(meters / 1000).toFixed(3), LengthUnit.Kilometers);
        }

        return new UsefulUnitConversion(meters, +meters.toFixed(1), LengthUnit.Meters);
    }
}