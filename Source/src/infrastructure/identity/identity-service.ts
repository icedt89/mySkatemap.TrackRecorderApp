import { Injectable } from "@angular/core";
import { Http } from "@angular/http";
import { Events } from "ionic-angular";
import { AuthenticationStore } from "../authentication-store";
import { LoginModel } from "./login-model";
import { ApiService } from "../api-service";

@Injectable()
export class IdentityService extends ApiService {
    private resource = "Identity";

    public constructor(http: Http, authenticationStore: AuthenticationStore, events: Events) {
        super(http, authenticationStore, events);
    }

    public login(loginModel: LoginModel): Promise<string> {
        const fullUri = super.buildFullUri(`/${this.resource}/Login`);

        return super.post(fullUri, loginModel).then(r => r.json());
    }
}