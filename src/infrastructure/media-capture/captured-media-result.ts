import { Exception } from "../track-recorder-exception";

export class CapturedMediaResult {
    public constructor(public result: string, public isBase64DataUrl = true) {
    }

    public get dataUrl(): string {
        if (this.isBase64DataUrl) {
            return `data:image/jpeg;base64,${this.result}`;
        }

        throw new Exception("isBase64DataUrl needs to be true.");
    }
}