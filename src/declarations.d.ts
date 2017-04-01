import { Observable } from 'rxjs/Rx';
/*
  Declaration files are how the Typescript compiler knows about the type information(or shape) of an object.
  They're what make intellisense work and make Typescript know all about your code.

  A wildcard module is declared below to allow third party libraries to be used in an app even if they don't
  provide their own type declarations.

  To learn more about using third party libraries in an Ionic app, check out the docs here:
  http://ionicframework.com/docs/v2/resources/third-party-libs/

  For more info on type definition files, check out the Typescript docs here:
  https://www.typescriptlang.org/docs/handbook/declaration-files/introduction.html
*/
declare module '*';

export declare namespace BackgroundGeolocation {

  export interface BackgroundGeolocationConfig {
    activitiesInterval: number;

    desiredAccuracy: number;

    distanceFilter: number;

    fastestInterval: number;

    interval: number;

    locationProvider: number;

    stationaryRadius: number;
  }

  export interface BackgroundGeolocation {
    start(success: () => void, error: (error: any) => void): void;

    stop(success: () => void, error: (error: any) => void): void;

    getValidLocations(success: (positions: BackgroundGeolocationResponse[]) => void, error: (error: any) => void): void;

    deleteAllLocations(success: () => void, error: (error: any) => void): void;

    getConfig(success: (config: BackgroundGeolocationConfig) => void, error: (error: any) => void): void;

    configure(success: (position: BackgroundGeolocationResponse) => void, error: any, config: BackgroundGeolocationConfig): void;

    isLocationEnabled(success: (enabled: boolean) => void, error: (error: any) => void): void;

    watchLocationMode(success: (enabled: boolean) => void, error: (error: any) => void): void;

    stopWatchingLocationMode(): void;

    showLocationSettings(): void;
  }

  export interface BackgroundGeolocationResponse {
    latitude: number;

    longitude: number;

    accuracy: number | null;

    time: number | null;

    speed: number | null;

    altitude: number | null;

    bearing: number | null;

    provider: string;
  }
}