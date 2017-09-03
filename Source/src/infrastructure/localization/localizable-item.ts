export class LocalizableItem {
    public constructor(
        private _de: string,
        private _en: string) {
    }

    public get de(): string {
        return this._de;
    }

    public get en(): string {
        return this._en;
    }
}