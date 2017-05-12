import { DatabindablePromise } from "../databindable-promise";
import { Exception } from "../exception";
import { Injectable } from "@angular/core";
import { ILocalizationService } from "./ilocalization-service";

@Injectable()
export class MockedLocalizationService implements ILocalizationService {
    private lookup: any;
    private language = "de-de";

    public newWithContext(lookup: any): ILocalizationService {
        const result = new MockedLocalizationService();
        result.lookup = lookup;

        return result;
    }

    public localize(key: string): Promise<string> {
        const keyPair = this.lookup[key];
        if (!keyPair) {
            throw new Exception(`Key '${key}' not found.`);
        }

        let value = keyPair[this.language];
        if (!value) {
            throw new Exception(`Language '${this.language}' not found on key '${key}'.`);
        }

        return Promise.resolve(value);
    }
}