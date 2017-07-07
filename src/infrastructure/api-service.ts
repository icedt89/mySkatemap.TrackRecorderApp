import { Http, Response, Headers, RequestOptionsArgs } from "@angular/http";
import { AuthenticationStore } from "./authentication-store";
import { Exception } from "./exception";

export abstract class ApiService {
    protected baseUri = "http://myskatemap-api.azurewebsites.net/api";

    public constructor(private http: Http, protected authenticationStore: AuthenticationStore) {
    }

    protected setAuthorizationHeader(requestOptionsArgs: RequestOptionsArgs) {
        const accessToken = this.authenticationStore.getAccessToken();
        if (!accessToken) {
            console.warn("Authentication not found. Please authenticate first with your prefered authentication provider. Request will be send without authorization header.");

            return;
        }

        if (requestOptionsArgs.headers) {
            requestOptionsArgs.headers.append("Authorization", `Bearer ${accessToken}`);
        }
    }

    protected buildFullUri(part: string): string {
        return `${this.baseUri}${part}`;
    }

    protected put(fullUri: string, body: any | null = null, sendCookies = false): Promise<Response> {
        return this.request(fullUri, "PUT", body, sendCookies);
    }

    protected patch(fullUri: string, body: any | null = null, sendCookies = false): Promise<Response> {
        return this.request(fullUri, "PATCH", body, sendCookies);
    }

    protected delete(fullUri: string, sendCookies = false): Promise<Response> {
        return this.request(fullUri, "DELETE", sendCookies);
    }

    protected post(fullUri: string, body: any | null = null, sendCookies = false): Promise<Response> {
        return this.request(fullUri, "POST", body, sendCookies);
    }

    protected get(fullUri: string, sendCookies = false): Promise<Response> {
        return this.request(fullUri, "GET", sendCookies);
    }

    protected setTimestampToUri(uri: string): string {
        const timeStamp = new Date().getUTCMilliseconds();

        return this.setQueryParameterToUri(uri, "_", timeStamp);
    }

    private setQueryParameterToUri(uri: string, name: string, value: any): string {
        if (uri.indexOf("?") !== -1) {
            return `${uri}&${name}=${value}`;
        }

        return `${uri}?${name}=${value}`;
    }

    protected setAccessTokenToUri(uri: string, accessToken: string = null): string {
        if (!accessToken) {
            accessToken = this.authenticationStore.getAccessToken();
            if (!accessToken) {
                throw new Exception("Authentication not found. Please authenticate first with your prefered authentication provider.");
            }
        }

        return this.setQueryParameterToUri(uri, "accessToken", accessToken);
    }

    private request(fullUri: string, method: string, body: any | null = null, sendCookies = false): Promise<Response> {
        const headers = new Headers();
        headers.append("Accept", "application/json");
        const requestOptions = <RequestOptionsArgs>{
            headers: headers,
            method: method,
            body: body,
            withCredentials: sendCookies
        };

        this.prepareRequest(requestOptions);

        return this.http.request(fullUri, requestOptions).toPromise().catch((error: Response) => {
            console.log(error);

            // Unauthorized (token invalidated!)
            if (error.status === 401) {
                // [Jan] TODO: ApplicationEvents.instance.fireUnauthorizedReceived();
            }

            throw error;
        });
    }

    protected getJson<T>(fullUri: string): Promise<T> {
        return this.get(fullUri).then(r => r.json());
    }

    protected prepareRequest(requestOptionsArgs: RequestOptionsArgs): void {
        this.setAuthorizationHeader(requestOptionsArgs);
    }
}