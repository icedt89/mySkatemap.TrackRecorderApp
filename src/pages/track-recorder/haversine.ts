import { LatLng } from "@ionic-native/google-maps";

const toRad = (val: number): number => (val * Math.PI) / 180;

const haversine = (pointOne: LatLng, pointTwo: LatLng): number => {
    const dLat = toRad(pointTwo.lat - pointOne.lat);
    const dLon = toRad(pointTwo.lng - pointOne.lng);
    const lat1 = toRad(pointOne.lat);
    const lat2 = toRad(pointTwo.lat);

    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return 6371000 * c;
};

export const haversineForPolyline = (track: LatLng[]): number => {
    let result = 0;

    for (let i = 0; i < track.length - 1; i++) {
        const pointOne = track[i];
        const pointTwo = track[i + 1];

        result += haversine(pointOne, pointTwo);
    }

    return result;
};