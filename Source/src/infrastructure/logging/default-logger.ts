import { Injectable } from "@angular/core";
import { ILogger } from "./ilogger";

@Injectable()
export class DefaultLogger implements ILogger {
    public log(message: string): void {
        console.log(message);
    }

    public warn(message: string): void {
        console.warn(message);
    }

    public error(message: string): void {
        console.error(message);
    }
}