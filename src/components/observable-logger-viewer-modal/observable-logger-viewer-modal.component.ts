import { ObservableLogger } from "../../infrastructure/logging/observable-logger";
import { Inject } from "@angular/core";
import { ILogger } from "../../infrastructure/logging/ilogger";
import { Component } from "@angular/core";

@Component({
    templateUrl: "observable-logger-viewer-modal.component.html"
})
export class ObservableLoggerViewerModalComponent {
    private observableLogger: ObservableLogger;

    public constructor( @Inject("Logger") logger: ILogger) {
        if (logger instanceof ObservableLogger) {
            this.observableLogger = logger;
        }
    }

    public get logEntries(): string[] {
        return this.observableLogger.logEntries;
    }

    public get isObservable(): boolean {
        return !!this.observableLogger;
    }

    public clear(): void {
        this.observableLogger.clear();
    }
}