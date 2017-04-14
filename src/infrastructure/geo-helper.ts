import { LatLng } from "@ionic-native/google-maps";

export namespace GeoHelper {
    export class Haversine {
        public static toRad(val: number): number {
            return (val * Math.PI) / 180;
        }

        public static computeDistanceBetween(pointOne: LatLng, pointTwo: LatLng): number {
            const dLat = Haversine.toRad(pointTwo.lat - pointOne.lat);
            const dLon = Haversine.toRad(pointTwo.lng - pointOne.lng);
            const lat1 = Haversine.toRad(pointOne.lat);
            const lat2 = Haversine.toRad(pointTwo.lat);

            const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
            const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

            return 6371000 * c;
        }

        public static computeDistance(polyLinePoints: LatLng[]): number {
            let result = 0;

            for (let i = 0; i < polyLinePoints.length - 1; i++) {
                const pointOne = polyLinePoints[i];
                const pointTwo = polyLinePoints[i + 1];

                result += Haversine.computeDistanceBetween(pointOne, pointTwo);
            }

            return result;
        }
    }
}
