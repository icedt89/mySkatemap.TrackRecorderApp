import { NavParams, ViewController } from "ionic-angular";
import { Component } from "@angular/core";
import { LoginModalModel } from "./login-modal-model";
import { LoginModel } from "../../infrastructure/identity/login-model";
import { AuthenticationHandler } from "../../infrastructure/authentication-handler";

@Component({
    templateUrl: "login-modal.component.html"
})
export class LoginModalComponent {
    private model = new LoginModalModel();

    public constructor(private viewController: ViewController,
        navigationParameters: NavParams,
        private authenticationHandler: AuthenticationHandler) {
    }

    private async login(): Promise<void> {
        debugger;

        const loginModel = new LoginModel();
        loginModel.EmailAddress = this.model.emailAddress;
        loginModel.Password = this.model.password;

        const accessToken = await this.authenticationHandler.login(loginModel);

    }
}