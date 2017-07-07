import { AuthenticationStore } from "./authentication-store";
import { IdentityService } from "./identity/identity-service";
import { Injectable } from "@angular/core";
import { Observable, Subject, Subscription } from "rxjs/Rx";
import { LoginModel } from "./identity/login-model";

@Injectable()
export class AuthenticationHandler {
    private authenticationStateSubject = new Subject<boolean>();
    private routerEventsSubscription: Subscription;
    private _currentAuthenticationState = false;

    public constructor(
        private authenticationStore: AuthenticationStore,
        private identityService: IdentityService) {
        this.updateCurrentAuthenticationState(!!this.authenticationStore.getAccessToken());
    }

    public get authenticationState(): Observable<boolean> {
        return this.authenticationStateSubject;
    }

    public get currentAuthenticationState(): boolean {
        return this._currentAuthenticationState;
    }

    public signOut(): void {
        this.authenticationStore.clearAccessToken();

        this.updateCurrentAuthenticationState(false);
    }

    public async login(loginModel: LoginModel): Promise<string> {
        const accessToken = await this.identityService.login(loginModel);

        this.authenticationStore.storeAccessToken(accessToken);
        this.updateCurrentAuthenticationState(!!accessToken);

        return accessToken;
    }

    private updateCurrentAuthenticationState(newValue: boolean): void {
        this._currentAuthenticationState = newValue;

        this.authenticationStateSubject.next(newValue);
    }
}