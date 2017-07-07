import { IonicModule } from "ionic-angular";
import { NgModule } from "@angular/core";
import { LoginModalComponent } from "./login-modal.component";

@NgModule({
    imports: [IonicModule],
    declarations: [LoginModalComponent],
    entryComponents: [LoginModalComponent],
    exports: [LoginModalComponent]
})
export class LoginModalModule { }