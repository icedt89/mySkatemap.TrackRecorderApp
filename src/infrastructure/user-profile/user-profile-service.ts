import { AuthenticationStore } from "../authentication-store";
import { Injectable } from "@angular/core";
import { Http } from "@angular/http";
import { Events } from "ionic-angular";
import { ApiService } from "../api-service";
import { UserProfileInfo } from "./user-profile-info";

@Injectable()
export class UserProfileService extends ApiService {
    private resource = "UserProfile";

    public constructor(http: Http, authenticationStore: AuthenticationStore, events: Events) {
        super(http, authenticationStore, events);
    }

    public async getUserProfileInfo(): Promise<UserProfileInfo> {
        const fullUri = super.buildFullUri(`/${this.resource}/Me/Info`);

        const userProfileInfo = await super.getJson<UserProfileInfo>(fullUri);

        userProfileInfo.AvatarUri = super.setTimestampToUri(super.setAccessTokenToUri(userProfileInfo.AvatarUri));

        return userProfileInfo;
    }
}