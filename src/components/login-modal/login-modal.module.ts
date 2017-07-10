import { IonicModule } from "ionic-angular";
import { NgModule } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { LoginModalComponent } from "./login-modal.component";

@NgModule({
    imports: [IonicModule, FormsModule],
    declarations: [LoginModalComponent],
    entryComponents: [LoginModalComponent],
    exports: [LoginModalComponent]
})
export class LoginModalModule { }