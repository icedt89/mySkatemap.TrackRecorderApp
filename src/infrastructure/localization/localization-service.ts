import { ILocalizationService } from "./ilocalization-service";
import { Injectable } from "@angular/core";
import { Platform } from "ionic-angular";
import { Globalization } from "@ionic-native/globalization";
import { Exception } from "../exception";

@Injectable()
export class LocalizationService implements ILocalizationService {
    private ultimateFallbackLanguage = "de-de";
    private lookup: any;
    private language = "de-de";

    public constructor(private platform: Platform, private globalization: Globalization) {
        this.initialize();
    }

    private async initialize(): Promise<void> {
        await this.platform.ready();

        const _ = await this.globalization.getPreferredLanguage();
        this.language = _.value;
    }

    public newWithContext(lookup: any): ILocalizationService {
        const result = new LocalizationService(this.platform, this.globalization);
        result.lookup = lookup;

        return result;
    }

    public async localize(key: string): Promise<string> {
        await this.platform.ready();

        const keyPair = this.lookup[key];
        if (!keyPair) {
            throw new Exception(`Key '${key}' not found.`);
        }

        let value = keyPair[this.language];
        if (!value) {
            console.warn(`Language '${this.language}' not found on key '${key}'.`);

            value = keyPair[this.ultimateFallbackLanguage];
            if (!value) {
                throw new Exception(`Fallback language '${this.language}' not found on key '${key}'.`);
            }
        }

        return value;
    }
}