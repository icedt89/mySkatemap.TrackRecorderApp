import { Injectable } from "@angular/core";

@Injectable()
export class StorageAccessor {
    private storage = window.localStorage;

    public clear(): void {
        this.storage.clear();
    }

    public get<T>(key: string): T | null {
        const value = this.storage.getItem(key);
        if (value) {
            return JSON.parse(value);
        }

        return null;
    }

    public hasKey(key: string): boolean {
        return !!this.get(key);
    }

    public remove(key: string): void {
        this.storage.removeItem(key);
    }

    public set(key: string, value: any): void {
        if (value) {
            value = JSON.stringify(value);
        }

        this.storage.setItem(key, value);
    }
}