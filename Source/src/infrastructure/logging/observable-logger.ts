import { Injectable } from "@angular/core";
import { ILogger } from "./ilogger";

@Injectable()
export class ObservableLogger implements ILogger {
    private _logEntries: string[] = [];

    public get logEntries(): string[] {
        return this._logEntries;
    }

    public clear(): void {
        this._logEntries = [];
    }

    public log(message: string): void {
        this.addObservedMessage(message);

        console.log(message);
    }

    public warn(message: string): void {
        this.addObservedMessage(message);

        console.warn(message);
    }

    public error(message: string): void {
        this.addObservedMessage(message);

        console.error(message);
    }

    private addObservedMessage(message: string): void {
        message = `${new Date().toLocaleString("de-de")}: ${message}`;

        this._logEntries.unshift(message);
    }
}