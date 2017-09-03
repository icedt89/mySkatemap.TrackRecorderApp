import { Injectable } from "@angular/core";
import { StorageAccessor } from "../infrastructure/storage-accessor";

@Injectable()
export class AuthenticationStore {
    public constructor(private storageAccessor: StorageAccessor) {
    }

    public getAccessToken(): string | null {
        return this.storageAccessor.get<string>("accessToken");
    }

    public storeAccessToken(accessToken: string): void {
        this.storageAccessor.set("accessToken", accessToken);
    }

    public clearAccessToken(): void {
        this.storageAccessor.remove("accessToken");
    }
}