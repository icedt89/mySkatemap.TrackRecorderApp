import { Inject } from "@angular/core";
import { IonicErrorHandler } from "ionic-angular";
import { ILogger } from "../infrastructure/logging/ilogger";

export class ObservableIonicErrorHandler implements IonicErrorHandler {
    public constructor( @Inject("Logger") private logger: ILogger) {
    }

    public handleError(error: any): void {
        this.logger.error(error);
    }
}