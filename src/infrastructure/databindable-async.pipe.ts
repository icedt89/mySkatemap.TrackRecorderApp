import { Observable } from "rxjs/Rx";
import { Pipe } from "@angular/core";
import { DatabindablePromise } from "./databindable-promise";
import { Exception } from "./exception";
import { AsyncPipe } from "@angular/common";

@Pipe({
    name: "databindableasync"
})
export class DatabindableAsyncPipe extends AsyncPipe {
    public transform<T>(obj: Promise<T> | Observable<T> | null | undefined, ...args: string[]): T {
        if (!args || !args.length) {
            return super.transform(<any>obj);
        }

        const key = args[0];

        if (obj instanceof DatabindablePromise) {
            return super.transform(obj);
        }

        if (!(obj instanceof Promise)) {
            throw new Exception("Value must be of type Promise or DatabindablePromise");
        }

        const databindablePromise = DatabindablePromise.databindable(key, obj);

        return super.transform(databindablePromise);
    }
}