export interface ILocalizationService {
    newWithContext(lookup: any): ILocalizationService;

    localize(key: string): Promise<string>;
}