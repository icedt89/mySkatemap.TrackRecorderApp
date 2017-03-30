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

export declare class BackgroundGeolocationConfig {
  public activitiesInterval: number;

  public desiredAccuracy: number;

  public distanceFilter: number;

  public fastestInterval: number;

  public interval: number;

  public locationProvider: number;

  public stationaryRadius: number;
}

export declare class BackgroundGeolocation {
  public start(success: () => void, error: (error: any) => void): void;

  public stop(success: () => void, error: (error: any) => void): void;

  public getValidLocations(success: (positions: BackgroundGeolocationResponse[]) => void, error: (error: any) => void): void;

  public deleteAllLocations(success: () => void, error: (error: any) => void): void;

  public getConfig(success: (config: BackgroundGeolocationConfig) => void, error: (error: any) => void): void;

  public configure(success: (position: BackgroundGeolocationResponse) => void, error: any, config: BackgroundGeolocationConfig): void;

  public isLocationEnabled(success: (enabled: boolean) => void, error: (error: any) => void): void;

  public watchLocationMode(success: (enabled: boolean) => void, error: (error: any) => void): void;

  public stopWatchingLocationMode(): void;

  public showLocationSettings(): void;
}

export declare class BackgroundGeolocationResponse {
  public latitude: number;

  public longitude: number;

  public accuracy: number | null;

  public time: number | null;

  public speed: number | null;

  public altitude: number | null;

  public bearing: number | null;

  public provider: string;
}